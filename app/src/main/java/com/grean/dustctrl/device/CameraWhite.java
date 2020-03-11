package com.grean.dustctrl.device;

import android.util.Log;

import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * 固定 192.168.1.6 端口 10086
 * 每2s发送风向信息，发送后摄像头断开连接
 * 摄像头->设置->网络->高级配置->SNMP->SNMP端口其他配置->SNMP端口 ：10086
 * Created by weifeng on 2020/3/3.
 */

public class CameraWhite implements CameraControl{
    private static String tag = "CameraWhite";
    private ReceiverThread receiverThread;
    private ConnectThread connectThread;
    private Socket socketClient;
    private InputStream receive;
    private OutputStream send;
    private boolean connected =false,run = false;
    private int directionOffset;
    private int windDirection;
    private SensorData data;

    public CameraWhite(SensorData data){
        this.data = data;
    }

    @Override
    public void setDirectionOffset(int directionOffset) {
        this.directionOffset = directionOffset;
    }

    @Override
    public int getDirectionOffset() {
        return directionOffset;
    }


    @Override
    public void startServer() {
        if(!run){
            connectThread = new ConnectThread();
            connectThread.start();
        }
    }

    private class ConnectThread extends Thread{
        public ConnectThread(){

        }

        @Override
        public void run() {
            run = true;

            while ((!interrupted())&&run){
                if (connected){//已连接服务器
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    socketClient = new Socket();
                    receiverThread = new ReceiverThread();
                    receiverThread.start();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }


    }

    private class ReceiverThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                socketClient.connect(new InetSocketAddress("192.168.1.64",10086), 500);
                socketClient.setTcpNoDelay(true);
                socketClient.setSoLinger(true,30);
                socketClient.setSendBufferSize(10240);
                socketClient.setKeepAlive(true);
                receive = socketClient.getInputStream();
                send = socketClient.getOutputStream();
                socketClient.setOOBInline(true);

                connected = true;
                windDirection = (int) data.getWindDirection();
                int direction = windDirection+directionOffset;
                if(direction >=360){
                    direction -= 360;
                }else if(direction <0){
                    direction += 360;
                }else{

                }
                byte [] currentFrame = new byte[7];
                currentFrame[0] = 0x01;
                currentFrame[1] = 0x03;
                currentFrame[2] = 0x02;
                byte [] tempBuff = tools.int2byte(direction);
                currentFrame[3] = tempBuff[0];
                currentFrame[4] = tempBuff[1];
                tools.addCrc16(currentFrame,0,5);
                //Log.d(tag,"send = "+tools.bytesToHexString(cmd,cmd.length));
                send.write(currentFrame);
                //Log.d(tag, tools.bytesToHexString(currentFrame, currentFrame.length));
                send.flush();


            } catch (IOException e) {
                connected = false;
                Log.d(tag,"找不到服务器");
                e.printStackTrace();
            }
            finally {
                connected = false;
                try {
                    socketClient.close();
                    //Log.d(tag,"关闭链接");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
