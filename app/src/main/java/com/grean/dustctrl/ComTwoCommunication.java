package com.grean.dustctrl;

import com.SerialCommunication;

/**
 * Created by weifeng on 2020/3/2.
 */

public class ComTwoCommunication extends SerialCommunication implements SerialCommunicationController{
    private static final String tag = "ComTwoCommunication";
    private static ComTwoCommunication instance = new ComTwoCommunication();
    private ComReceiveProtocol comReceiveProtocol;

    private ComTwoCommunication(){
        super(1,9600,0);
    }
    @Override
    protected boolean checkRecBuff() {
        return true;
    }

    public void send(byte[] buff,int state){
        addSendBuff(buff,state);
    }

    public void setComReceiveProtocol(ComReceiveProtocol comReceiveProtocol) {
        this.comReceiveProtocol = comReceiveProtocol;
    }

    public static ComTwoCommunication getInstance() {
        return instance;
    }

    @Override
    protected void communicationProtocol(byte[] rec, int size, int state) {
        if(comReceiveProtocol!=null){
            comReceiveProtocol.receiveProtocol(rec,size,state);
        }
    }

    @Override
    protected void asyncCommunicationProtocol(byte[] rec, int size) {
        if(comReceiveProtocol!=null){
            comReceiveProtocol.receiveAsyncProtocol(rec,size);
        }
    }
}
