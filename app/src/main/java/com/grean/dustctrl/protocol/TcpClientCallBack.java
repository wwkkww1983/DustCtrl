package com.grean.dustctrl.protocol;

/**
 * Created by weifeng on 2017/9/20.
 */

public interface TcpClientCallBack {
    /**
     * 将一帧数据插入发送队列
     * @param data
     * @return
     */
    boolean addOneFrame(byte [] data);
}
