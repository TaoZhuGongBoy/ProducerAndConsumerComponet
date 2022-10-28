package com.taozhugongboy.concurrentprogramming.componet;


import com.taozhugongboy.concurrentprogramming.ProcessorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class ComponetConfiguration {

    @Autowired
    private ConfigProperties dubboProperties;

    @Bean
    public ProducerAndConsumerComponet registBean() {
        return new ProducerAndConsumerComponet(dubboProperties.getThreadNum(), dubboProperties.getQueueSizeLimit(), dubboProperties.getPeriod(), dubboProperties.getCapacity(), new ProcessorImpl());
    }


}
