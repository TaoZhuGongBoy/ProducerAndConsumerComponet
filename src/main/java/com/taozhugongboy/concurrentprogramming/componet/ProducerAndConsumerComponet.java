package com.taozhugongboy.concurrentprogramming.componet;

import com.taozhugongboy.concurrentprogramming.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 生产者消费者组件
 * @author taozhugongBoy
 */
public class ProducerAndConsumerComponet<T> {

    //组件本身持有一个工作者线程对象数组
    private final WorkThread<T>[] workThreads;
    private AtomicInteger index;
    private static final Random r = new Random();
    private static ScheduledExecutorService TIMER = new ScheduledThreadPoolExecutor(1);

    private static ExecutorService POOL = Executors.newCachedThreadPool();

    /**
     * 组件构造器
     * @param threadNum 默认新建的消费者线程个数
     * @param limitSize 队列长度阈值;超过将唤醒阻塞的线程
     * @param limitInterval 当前时间距离上一次任务处理时间间隔阈值;超过将唤醒阻塞的线程
     * @param capacity 工作线程内部的有界阻塞队列的初始容量大小
     * @param processor 回调接口(初始化组价实例的时候需要传递)
     */
    public ProducerAndConsumerComponet(int threadNum,int limitSize, int limitInterval, int capacity,  Processor<T> processor) {
        this.workThreads = new WorkThread[threadNum];
        if (threadNum > 1) {
            this.index = new AtomicInteger();
        }
        for(int i = 0; i < threadNum; ++i) {
            WorkThread<T> workThread = new WorkThread("workThread"+ "_" + i, limitSize, limitInterval, capacity, processor);
            this.workThreads[i] = workThread;

            POOL.submit(workThread);
            TIMER.scheduleAtFixedRate(workThread::timeout, r.nextInt(50), limitInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 将对象路由到对应消费者线程对象的阻塞队列中去
     * @param item 添加的对象
     * @return true:添加成功 false:添加失败
     */
    public boolean add(T item) {
        int len = this.workThreads.length;
        if (len == 1) {
            return this.workThreads[0].add(item);
        } else {
            int mod = this.index.incrementAndGet() % len;
            return this.workThreads[mod].add(item);
        }
    }

    /**
     * 消费者线程
     */
    private static class WorkThread<T> implements Runnable {
        private final String name;
        private final int bufferSize;
        private int flushInterval;
        private volatile long lastFlushTime;
        private volatile Thread writer;
        private final BlockingQueue<T> queue;
        private final Processor<T> processor;

        /**
         * 消费者线程构造器
         * @param name 线程名
         * @param bufferSize 指定队列阈值
         * @param flushInterval 指定距离时间阈值
         * @param capacity 阻塞队列初始化大小
         * @param processor 回调接口
         */
        public WorkThread(String name, int bufferSize, int flushInterval, int capacity, Processor<T> processor) {
            this.name = name;
            this.bufferSize = bufferSize;
            this.flushInterval = flushInterval;
            this.lastFlushTime = System.currentTimeMillis();
            this.processor = processor;
            this.queue = new ArrayBlockingQueue(capacity);
        }

        /**
         * 往阻塞队列中添加元素
         * @param item 添加的对象
         * @return true:添加成功 false:添加失败
         */
        public boolean add(T item) {
            boolean result = this.queue.offer(item);
            this.checkQueueSize();
            return result;
        }

        /**
         * 距离上次的任务时间是否超过指定阈值
         */
        public void timeout() {
            if (System.currentTimeMillis() - this.lastFlushTime >= (long)this.flushInterval) {
                this.start();
            }

        }

        /**
         * 颁与许可证(使其他线程可以执行线程调度任务、分配CPU时间片资源)
         */
        private void start() {
            LockSupport.unpark(this.writer);
        }

        /**
         * 判断队列长度是否超过指定阈值
         */
        private void checkQueueSize() {
            if (this.queue.size() > this.bufferSize) {
                this.start();
            }

        }

        /**
         * 一旦线程被解除阻塞<br>
         * 就会将队列中的元素通过调用<code>drainTo</code>方法，一次性转成List对象，最后调用回调函数传递List对象
         */
        public void flush() {
            this.lastFlushTime = System.currentTimeMillis();
            List<T> temp = new ArrayList(this.bufferSize);
            int size = this.queue.drainTo(temp, this.bufferSize);
            if (size > 0) {
                try {
                    //回调
                    this.processor.process(temp);
                } catch (Throwable var4) {
                    System.out.println("process error");
                }
            }

        }

        /**
         * 判断队列长度是否超过指定阈值或距离上次的任务时间是否超过指定阈值
         * @return true:满足 false:不满足
         */
        private boolean canFlush() {
            return this.queue.size() > this.bufferSize || System.currentTimeMillis() - this.lastFlushTime > (long)this.flushInterval;
        }

        @Override
        public void run() {
            this.writer = Thread.currentThread();
            this.writer.setName(this.name);

            while(!this.writer.isInterrupted()) {
                //工作线程死循环的判断(队列长度是否超过指定阈值或距离上次的任务时间是否超过指定阈值，如果不满足，阻塞自己)
                while(!this.canFlush()) {
                    LockSupport.park(this);
                }
                //一旦线程被解除阻塞，就会触发此方法，将队列元素转成List对象，传递给回调函数
                this.flush();
            }

        }
    }
}
