package com.taozhugongboy.concurrentprogramming;

import com.taozhugongboy.concurrentprogramming.componet.ProducerAndConsumerComponet;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConcurrentProgrammingApplication.class)
class ConcurrentProgrammingApplicationTests {

	@Autowired
	private ProducerAndConsumerComponet producerAndConsumerComponet;


	@Test
	void add() {
		for(int i=0;i<10;i++){

			producerAndConsumerComponet.add("1");
		}
	}

	@Test
	void timeout() {
		for(int i=0;i<10;i++){

			producerAndConsumerComponet.add("1");
		}
	}
}
