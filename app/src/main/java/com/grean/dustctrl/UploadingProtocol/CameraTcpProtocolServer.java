package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.grean.dustctrl.CtrlCommunication;
import com.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by weifeng on 2019/11/5.
 */

public class CameraTcpProtocolServer implements CameraControl{
    private static String tag = "CameraTcpProtocolServer";
    private ReceiverThread receiverThread;
    private ConnectThread connectThread;
    private Socket socketClient;
    private InputStream receive;
    private OutputStream send;
    private boolean connected =false,run = false;
    private int directionOffset;
    private String ip;
    private int port;
    private int windDirection;

    private static CameraTcpProtocolServer instance =  new CameraTcpProtocolServer();


    public static CameraTcpProtocolServer getInstance() {
        return instance;
    }

    private CameraTcpProtocolServer(){

    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public void setDirectionOffset(int directionOffset) {
        this.directionOffset = directionOffset;
    }

    public void connectServer(String ip,int port){
        if(!run){
            this.ip = ip;
            this.port = port;
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
                socketClient.connect(new InetSocketAddress(ip, port), 500);
                socketClient.setTcpNoDelay(true);
                socketClient.setSoLinger(true,30);
                socketClient.setSendBufferSize(10240);
                socketClient.setKeepAlive(true);
                receive = socketClient.getInputStream();
                send = socketClient.getOutputStream();
                socketClient.setOOBInline(true);

                int count;
                //byte[] readBuff = new byte[4096];

                connected = true;
                //setChanged();
                //Log.d(tag,"已连接服务器");
                //notifyObservers(new LogFormat("已连接服务器"));
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

                /*while (connected){
                    if (socketClient.isConnected()&&(!socketClient.isClosed())){
                        while ((count = receive.read(readBuff))!=-1 && connected){
                            Log.d(tag, tools.bytesToHexString(readBuff,count));

                        }
                        connected = false;
                        break;
                    }else {
                        connected = false;
                    }
                    Log.d(tag,"one turn");
                }*/
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
            //setChanged();
            //notifyObservers(new LogFormat("中断网络链接"));
        }
    }
}
