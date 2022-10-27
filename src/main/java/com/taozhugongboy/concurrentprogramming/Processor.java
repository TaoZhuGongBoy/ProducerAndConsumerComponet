package com.taozhugongboy.concurrentprogramming;

import java.util.List;

/**
 * 回调接口
 * @author taozhugongBoy
 */
public interface Processor<T> {
    void process(List<T> list);
}
