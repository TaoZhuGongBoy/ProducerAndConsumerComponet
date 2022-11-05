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
    private ConfigProperties configProperties;

    @Bean
    public ProducerAndConsumerComponet registBean() {
        return new ProducerAndConsumerComponet(configProperties.getThreadNum(), configProperties.getQueueSizeLimit(), configProperties.getPeriod(), configProperties.getCapacity(), new ProcessorImpl());
    }


}
