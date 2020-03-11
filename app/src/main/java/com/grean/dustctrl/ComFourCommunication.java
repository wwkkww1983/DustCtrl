package com.grean.dustctrl;

import com.SerialCommunication;

/**
 * Created by weifeng on 2020/2/28.
 */

public class ComFourCommunication extends SerialCommunication implements SerialCommunicationController{
    private final String tag = "ComFourCommunication";
    private static ComFourCommunication instance = new ComFourCommunication();
    private ComReceiveProtocol comReceiveProtocol;

    private ComFourCommunication(){
        super(3,9600,0);
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

    public static ComFourCommunication getInstance() {
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
