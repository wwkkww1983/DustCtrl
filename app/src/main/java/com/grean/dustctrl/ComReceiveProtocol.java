package com.grean.dustctrl;

/**
 * Created by weifeng on 2020/2/28.
 */

public interface ComReceiveProtocol {
    /**
     * 同步接收方法
     * @param rec
     * @param size
     * @param state
     */
    void receiveProtocol(byte[] rec, int size, int state);

    /**
     * 异步接收方法
     * @param rec
     * @param size
     */

    void receiveAsyncProtocol(byte[] rec, int size);
}
