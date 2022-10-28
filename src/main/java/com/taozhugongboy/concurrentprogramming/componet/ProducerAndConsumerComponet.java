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

    //组件持有一个工作线程对象数组
    private final WorkThread<T>[] workThreads;
    private AtomicInteger index;
    private static final Random r = new Random();
    //任务定时器
    private static ScheduledExecutorService TIMER = new ScheduledThreadPoolExecutor(1);

    private static ExecutorService POOL = Executors.newCachedThreadPool();

    /**
     * 组件构造器
     * @param threadNum 默认新建的消费者线程个数
     * @param limitSize 队列长度阈值;超过将唤醒阻塞的线程
     * @param period 前后两个任务的执行周期 (for example :200ms 代表前面一次任务执行完毕后，200毫秒后下一个任务继续执行)
     * @param capacity 工作线程内部的有界阻塞队列的初始容量大小
     * @param processor 回调接口(初始化组价实例的时候需要传递)
     */
    public ProducerAndConsumerComponet(int threadNum,int limitSize, int period, int capacity,  Processor<T> processor) {
        this.workThreads = new WorkThread[threadNum];
        if (threadNum > 1) {
            this.index = new AtomicInteger();
        }
        for(int i = 0; i < threadNum; ++i) {
            WorkThread<T> workThread = new WorkThread("workThread"+ "_" + i, limitSize, period, capacity, processor);
            this.workThreads[i] = workThread;

            POOL.submit(workThread);
            TIMER.scheduleAtFixedRate(workThread::timeout, r.nextInt(50), period, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 生产者将对象添加到对应消费者线程对象的阻塞队列中去<br>
     * 采用HASH取模算法进行动态路由
     * @param item 待添加的对象
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
        /**
         * 工作线程名字
         */
        private final String threadName;
        /**
         * 队列中指定元素个数限制
         */
        private final int queueSizeLimit;
        /**
         * 距离上次任务处理时间的间隔限制
         */
        private int flushInterval;
        /**
         * 用来记录任务处理的最新时间
         */
        private volatile long lastFlushTime;

        /**
         * 当前线程对象
         */
        private volatile Thread currentThread;

        /**
         * 工作线程对象内部的阻塞队列
         */
        private final BlockingQueue<T> queue;
        /**
         * 回调函数
         */
        private final Processor<T> processor;

        /**
         * 消费者线程构造器
         * @param threadName 线程名
         * @param queueSizeLimit 指定队列阈值
         * @param flushInterval 指定距离时间阈值
         * @param capacity 阻塞队列初始化大小
         * @param processor 回调接口
         */
        public WorkThread(String threadName, int queueSizeLimit, int flushInterval, int capacity, Processor<T> processor) {
            this.threadName = threadName;
            this.queueSizeLimit = queueSizeLimit;
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
         * 当前时间与上次的任务处理时间差是否超过指定阈值;如果超过触发start方法
         */
        public void timeout() {
            if (System.currentTimeMillis() - this.lastFlushTime >= (long)this.flushInterval) {
                this.start();
            }

        }

        /**
         * 唤醒被阻塞的工作线程
         */
        private void start() {
            LockSupport.unpark(this.currentThread);
        }

        /**
         * 判断队列实际长度是否超过指定阈值;如果超过触发start方法
         */
        private void checkQueueSize() {
            if (this.queue.size() > this.queueSizeLimit) {
                this.start();
            }

        }

        /**
         * 将队列中的元素通过调用<code>drainTo</code>方法，转成List对象(容量受queueSizeLimit限制)，最后调用回调函数传递List对象
         */
        public void flush() {
            if(queue.isEmpty()){
                return;
            }
            this.lastFlushTime = System.currentTimeMillis();
            List<T> temp = new ArrayList(this.queueSizeLimit);
            int size = this.queue.drainTo(temp, this.queueSizeLimit);
            if (size > 0) {
                try {
                    //执行回调函数
                    this.processor.process(temp);
                } catch (Throwable var4) {
                    System.out.println("process error");
                }
            }

        }

        /**
         * 判断队列实际大小是否超过指定阈值亦或距离上次任务处理时间差是否超过指定阈值
         * @return true:满足触发条件 false:不满足触发条件
         */
        private boolean canFlush() {
            return this.queue.size() > this.queueSizeLimit || System.currentTimeMillis() - this.lastFlushTime > (long)this.flushInterval;
        }

        @Override
        public void run() {
            this.currentThread = Thread.currentThread();
            this.currentThread.setName(this.threadName);
            //当前线程没有被其他线程打断
            while(!this.currentThread.isInterrupted()) {
                //死循环的判断是否满足触发条件(队列实际大小是否超出指定阈值或距离上次任务时间是否超出指定阈值)，如果未满足将阻塞当前线程，避免死循环给系统带来性能影响
                while(!this.canFlush()) {
                    //当前线程被阻塞
                    LockSupport.park(this);
                }
                //一旦add执行的时候判断实际工作线程对象内部的阻塞队列大小超出自定制阈值亦或距离上次任务处理时间差超出指定阈值，就会调用LockSupport.unpark方法解除阻塞的线程
                //一旦线程被解除阻塞，就会触发此方法，将队列元素转成List对象且调用已经注册的回调函数
                this.flush();
            }

        }
    }
}
