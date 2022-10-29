package com.taozhugongboy.concurrentprogramming;

import com.taozhugongboy.concurrentprogramming.componet.ProducerAndConsumerComponet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConcurrentProgrammingApplication.class)
class ConcurrentProgrammingApplicationTests {

	@Autowired
	private ProducerAndConsumerComponet producerAndConsumerComponet;


	/**
	 * 前置条件：
	 *     #主件初始化默认新增两个工作线程
	 *     config.threadNum=2
	 *     config.period=12000
	 *     config.queueSizeLimit=3
	 *     config.capacity=10
	 */
	@DisplayName("用例一:队列大小超出指定阈值")
	@Test
	void add() {
		for(int i=0;i<10;i++){

			producerAndConsumerComponet.add("1");
		}
		try {
			TimeUnit.SECONDS.sleep(10);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	void timeout() {
		for(int i=0;i<10;i++){

			producerAndConsumerComponet.add("1");
		}
	}
}
