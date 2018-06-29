package com.grean.dustctrl.UploadingProtocol;

/**
 * Created by weifeng on 2018/6/29.
 */

public class HJT212_2017ProtocolState implements ProtocolState{
    private UploadingConfigFormat format;
    private ProtocolCommand command;
    private Hjt212FrameBuilder frameBuilder;
    private String qnSend,qnReceived;
    private byte[] receiveBuff = new byte[1];

    public HJT212_2017ProtocolState(ProtocolCommand command){
        this.command = command;
        frameBuilder = new Hjt212FrameBuilder();
    }

    private boolean checkFrameLength(byte[] buff,int length){
        if(length < 12){
            return false;
        }
        String string = new String(buff,2,4);
        int len = Integer.parseInt(string);
        if(len!=(length-6)){//帧长
            return false;
        }
        return true;
    }

    private boolean checkFrameHead(byte[] buff,int length){
        String string = new String(buff,0,2);
        if(!string.equals("##")){//帧头
            return false;
        }else{
            return true;
        }
    }

    private boolean checkFrameTail(byte[] buff,int length){
        String string = new String(buff,length-2,2);
        if(!string.equals("\r\n")){//帧尾
            return false;
        }else {
            return true;
        }
    }

    private int getFrameProtocolLength(byte[] buff,int index){
        return Integer.parseInt(new String(buff,index+2,4));
    }

    private boolean checkFrame(byte[] buff,int length){
        if(length < 12){
            return false;
        }
        String string = new String(buff,2,4);
        int len = Integer.parseInt(string);
        if(len!=(length-6)){//帧长
            return false;
        }

        string = new String(buff,0,2);
        if(!string.equals("##")){//帧头
            return false;
        }
        string = new String(buff,length-2,2);
        if(!string.equals("\r\n")){//帧尾
            return false;
        }
        return true;
    }

    private String getContent(byte[] buff,int length){
        return new String(buff,6,length-12);
    }

    private void handleProtocol(String content){

    }

    private void partitionFrame(byte[] buff,int length,int frameLength){
        int restLength = length-frameLength;//余下数据
        int firstLength = frameLength;
        int index=0;
        byte[] tempBuff = new byte[firstLength];
        while (restLength>0){
            System.arraycopy(buff,index,tempBuff,0,firstLength);
            if(checkFrame(tempBuff,firstLength)){
                handleProtocol(getContent(buff,firstLength));
                index+=firstLength;//迁移地址
                if((length - index)<6){
                    break;
                }else{
                    firstLength = getFrameProtocolLength(buff,index);
                    restLength = length - firstLength;//小于则退出循环，大于等则继续
                }
            }else{

            }

        }
    }

    @Override
    public void handleReceiveBuff(byte[] buff, int length) {
        if(checkFrame(buff,length)){//合规帧
            handleProtocol(getContent(buff,length));
        }else{//处理异常帧



            receiveBuff=new byte[1];//清空
        }
    }

    @Override
    public void uploadSystemTime(long now, long lastMinDate, long lastHourDate) {

    }

    @Override
    public void uploadMinDate(long now, long date) {

    }

    @Override
    public void uploadSecondDate(long now) {

    }

    @Override
    public void uploadHourDate(long now, long date) {

    }

    @Override
    public void setConfig(UploadingConfigFormat format) {
        this.format = format;

    }
}
