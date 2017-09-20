package com.grean.dustctrl.protocol;

import android.os.StrictMode;
import android.util.Log;

/**
 * Created by Administrator on 2017/9/1.
 */

public class TcpClient implements GeneralClientProtocol{
    private static final String tag = "TcpClient";
    private String heartString = "Grean.com.cn";
    private TcpClientCallBack callBack;
    private String mnCode = "88888";
    private boolean run;
    private HeartThread thread;

    public TcpClient(TcpClientCallBack callBack){
       this.callBack =  callBack;
    }
    @Override
    public void handleProtocol(byte[] rec, int count) {
        Log.d(tag,new String(rec,0,count));

    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
    }

    @Override
    public boolean addSendBuff(String string) {
        return callBack.addOneFrame(string.getBytes());
    }

    @Override
    public void startHeartBeatPacket() {
        thread = new HeartThread();
        thread.start();
    }

    @Override
    public void stopHeartBeatPacket() {
        run = false;
    }

    private class HeartThread extends Thread{

        @Override
        public void run() {
            run = true;
            while (run&&!interrupted()) {
                addSendBuff(heartString+mnCode);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
