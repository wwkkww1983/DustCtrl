package com.grean.dustctrl;

import com.SerialCommunication;

/**
 * 噪声仪通讯
 * Created by Administrator on 2017/8/24.
 */

public class NoiseCommunication extends SerialCommunication{
    private static NoiseCommunication instance = new NoiseCommunication();

    private NoiseCommunication(){
        super(1,9600,0);
    }

    public static NoiseCommunication getInstance() {
        return instance;
    }

    @Override
    protected boolean checkRecBuff() {
        return true;
    }

    @Override
    protected void communicationProtocol(byte[] rec, int size,int state) {

    }

    @Override
    protected void asyncCommunicationProtocol(byte[] rec, int size) {

    }

}
