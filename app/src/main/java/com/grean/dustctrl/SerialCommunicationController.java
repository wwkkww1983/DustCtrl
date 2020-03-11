package com.grean.dustctrl;

/**
 * Created by weifeng on 2018/5/2.
 */

public interface SerialCommunicationController {
    void send(byte[] buff,int state);
    void setComReceiveProtocol(ComReceiveProtocol comReceiveProtocol);
}
