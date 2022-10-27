package com.taozhugongboy.concurrentprogramming.componet;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhangfengshi
 */
@ConfigurationProperties(prefix = "config")
public class ConfigProperties {
    private Integer threadNum;
    private Integer intervalTimeLimit;
    private Integer queueSizeLimit;

    private Integer capacity;


    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Integer getIntervalTimeLimit() {
        return intervalTimeLimit;
    }

    public void setIntervalTimeLimit(Integer intervalTimeLimit) {
        this.intervalTimeLimit = intervalTimeLimit;
    }

    public Integer getQueueSizeLimit() {
        return queueSizeLimit;
    }

    public void setQueueSizeLimit(Integer queueSizeLimit) {
        this.queueSizeLimit = queueSizeLimit;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
