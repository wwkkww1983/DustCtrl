package com.grean.dustctrl.UploadingProtocol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.SystemConfig;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by weifeng on 2019/5/29.
 */

public class LedTcpProtocolServer extends Observable implements NotifyScanSensorOnLedDisplay{
    private static String tag = "LedTcpProtocolServer";
    private static LedTcpProtocolServer instance = new LedTcpProtocolServer();
    private boolean connected =false,run = false;
    private ConnectThread connectThread;
    private ReceiverThread receiverThread;
    private Socket socketClient;
    private InputStream receive;
    private OutputStream send;
    private String ip;
    private int port;
    private byte [] currentFrame = new byte[0];

    //private ConcurrentLinkedQueue<byte[]> sendBuff = new ConcurrentLinkedQueue<>();

    public static LedTcpProtocolServer getInstance() {
        return instance;
    }

    private LedTcpProtocolServer (){

    }

    private static byte[] addFrame(byte[] region1,byte[] region2){
        byte [] head = {0x55, (byte) 0xaa,0x00,0x00,
                0x01,
                0x01,
                0x00, (byte) 0xda,
                0x00,0x00,
                0x00,0x00,0x00,0x00,
                (byte) 0x81,0x00,0x00,0x00,//总长 14~17
                (byte) 0x81,0x00,//帧长 18-19
                //显示数据
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,//目标控制器型号 20-35
                0x40,0x00,0x20,0x00,//坐标 36-39
                0x01,//规格，单色 40
                0x01,//节目数，1  41
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,//保留 42-49
                //节目数据
                0x00,// 50
                0x63,0x00,0x00,0x00,//节目数据大小 51-54
                0x02,//节目中区域个数 55
                0x00,0x00,//播放时长 56-57
                0x00,//循环次数 58
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,//保留
        };
        byte [] end = {0x00,0x00,0x0d,0x0a};
        byte[] regionSize = tools.int2bytes(region1.length+region2.length+25);
        head[51] = regionSize[3];
        head[52] = regionSize[2];
        head[53] = regionSize[1];
        head[54] = regionSize[0];
        regionSize = tools.int2bytes(region1.length+region2.length+25 + 30);
        head[14] = regionSize[3];
        head[15] = regionSize[2];
        head[16] = regionSize[1];
        head[17] = regionSize[0];
        head[18] = regionSize[3];
        head[19] = regionSize[2];

        byte [] frame = new byte[region1.length+region2.length+head.length+end.length];
        System.arraycopy(head,0,frame,0,head.length);
        int length = head.length;
        System.arraycopy(region1,0,frame,length,region1.length);
        length += region1.length;
        System.arraycopy(region2,0,frame,length,region2.length);
        length += region2.length;
        System.arraycopy(end,0,frame,length,end.length);
        length += end.length;
        //Log.d(tag,"send = "+ tools.bytesToHexString(frame,length));
        return frame;
    }

    public void connectServer(String ip,int port){
        if(!run){
            this.ip = ip;
            this.port = port;
            connectThread = new ConnectThread();
            connectThread.start();

        }
    }

    private static byte[] addRegionFrame(int regionNum,String string){
        byte [] content = new byte[0];
        try {
            content = string.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte [] head = {
                0x01,
                0x22,0x00,0x00,0x00,//区域大小
                0x0e,
                0x00,0x00,0x00,0x00,
                0x3f,0x00,0x0f,0x00,
                0x01,0x00,0x00,
                0x61,
                0x14,
                0x01,0x00,
                0x10,
                0x08,0x00,0x00,0x00,
        };
        if(regionNum!=1){
            head[8] = 0x10;
            head[12] = 0x1f;
        }
        byte[] contentSize = tools.int2bytes(content.length);
        head[22] = contentSize[3];
        head[23] = contentSize[2];
        head[24] = contentSize[1];
        head[25] = contentSize[0];
        byte[] frameSize = tools.int2bytes(head.length+content.length);
        head[1] = frameSize[3];
        head[2] = frameSize[2];
        head[3] = frameSize[1];
        head[4] = frameSize[0];
        byte [] frame = new byte[content.length+head.length];
        System.arraycopy(head,0,frame,0,head.length);
        System.arraycopy(content,0,frame,head.length,content.length);
        return frame;
    }

    private void showDataOnLedDisplay(SensorData data){
        showContentOnLedDisplay(" 扬  尘 ",
                tools.float2String0(data.getDust())+"μg/m3");
    }

    /**
     * 显示区域
     * @param region1 区域数据1
     * @param region2 区域数据2
     */
    private void showContentOnLedDisplay(String region1,String region2){
        /*if(connected){
            sendBuff.add(addFrame(addRegionFrame(1,region1),addRegionFrame(2,region2)));
        }*/
        currentFrame = addFrame(addRegionFrame(1,region1),addRegionFrame(2,region2));
    }

    @Override
    public void onResult(SensorData data) {
        showDataOnLedDisplay(data);
    }

    private class ConnectThread extends Thread{

        public ConnectThread(){

        }

        @Override
        public void run() {
            run = true;

            while ((!interrupted())&&run){
                if (connected){//已连接服务器
                    /*try {
                        if(!sendBuff.isEmpty()){
                            socketClient.sendUrgentData(0xFF);
                            send.write(sendBuff.poll());
                            send.flush();
                            //Log.d(tag,"send buff rest size "+String.valueOf(sendBuff.size()));
                        }
                    } catch (IOException e) {
                        Log.d(tag,"发送失败");
                        try {
                            socketClient.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }*/
                }else{
                    socketClient = new Socket();
                    receiverThread = new ReceiverThread();
                    receiverThread.start();

                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void handleBuffer(byte[] buff,int count){

    }

    private class ReceiverThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                socketClient.connect(new InetSocketAddress(ip, port), 5000);
                socketClient.setTcpNoDelay(true);
                socketClient.setSoLinger(true,30);
                socketClient.setSendBufferSize(10240);
                socketClient.setKeepAlive(true);
                receive = socketClient.getInputStream();
                send = socketClient.getOutputStream();
                socketClient.setOOBInline(true);

                int count;
                byte[] readBuff = new byte[4096];

                connected = true;
                //setChanged();
                //Log.d(tag,"已连接服务器");
                //notifyObservers(new LogFormat("已连接服务器"));
                send.write(currentFrame);
                send.flush();
                while (connected){
                    if (socketClient.isConnected()&&(!socketClient.isClosed())){
                        while ((count = receive.read(readBuff))!=-1 && connected){
                            Log.d(tag, tools.bytesToHexString(readBuff,count));
                            handleBuffer(readBuff,count);
                        }
                        connected = false;
                        break;
                    }else {
                        connected = false;
                    }
                    Log.d(tag,"one turn");
                }
            } catch (IOException e) {
                connected = false;
                Log.d(tag,"找不到服务器");
                e.printStackTrace();
            }
            finally {
                connected = false;
                try {
                    socketClient.close();
                    Log.d(tag,"关闭链接");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //setChanged();
            //notifyObservers(new LogFormat("中断网络链接"));
        }
    }
}
