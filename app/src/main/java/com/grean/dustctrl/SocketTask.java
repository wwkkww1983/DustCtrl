package com.grean.dustctrl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP客户端任务类，单例化
 * Created by Administrator on 2017/9/4.
 */

public class SocketTask {
    private static final String tag = "SocketTask";
    private static SocketTask instance = new SocketTask();
    private static boolean heartRun = false,connected = false;
    private Context context;
    private SocketClientCtrl clientCtrl;
    private HeartThread heartThread;
    private ReceiverThread receiverThread;
    private Socket socketClient;
    private String serverIp;
    private int serverPort;
    private InputStream receive;
    private OutputStream send;
    private String heartString = "Grean.com.cn";
    NotifyProcessDialogInfo notifyProcessDialogInfo;
    NotifyOperateInfo notifyOperateInfo;

    public static SocketTask getInstance() {
        return instance;
    }

    private SocketTask(){

    }

    public void resetSocketClient(String ip,int port,NotifyOperateInfo notifyOperateInfo,NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.notifyProcessDialogInfo = notifyProcessDialogInfo;
        this.notifyOperateInfo = notifyOperateInfo;
        this.serverIp = ip;
        this.serverPort= port;
        if(heartRun){
            if(socketClient!=null){
                if(socketClient.isConnected()){
                    try {
                        socketClient.shutdownInput();
                        socketClient.shutdownOutput();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            /*heartThread = new HeartThread();
            heartThread.start();*/

        }else{
            heartThread = new HeartThread();
            heartThread.start();

        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void startSocketHeart(String ip, int port, Context context, SocketClientCtrl clientCtrl){
        this.context = context;
        this.clientCtrl = clientCtrl;
        this.serverIp = ip;
        this.serverPort = port;
        if (!heartRun){
            heartThread = new HeartThread();
            heartThread.start();
        }
    }

    private class ReceiverThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("新建链接");
                }
                Log.d(tag,"IP:"+serverIp+" Port:"+String.valueOf(serverPort));
                socketClient.connect(new InetSocketAddress(serverIp,serverPort),5000);
                socketClient.setTcpNoDelay(true);
                socketClient.setSoLinger(true,30);
                socketClient.setSendBufferSize(10240);
                socketClient.setKeepAlive(true);
                receive = socketClient.getInputStream();
                send = socketClient.getOutputStream();
                socketClient.setOOBInline(true);
                connected = true;
                int count;
                byte[] readBuff = new byte[4096];
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("已链接");
                }
                if(notifyOperateInfo!=null){
                    notifyOperateInfo.cancelDialog();
                }
                while (connected){
                    if (socketClient.isConnected()){
                        while ((count = receive.read(readBuff))!=-1 && connected){
                            String content = new String(readBuff,0,count);
                            Log.d(tag,"TCP Content:"+content);
                        }
                        connected = false;
                        break;
                    }else {
                        connected = false;
                    }
                    Log.d(tag,"one turn");
                }
            } catch (IOException e) {
                Log.d(tag,"找不到服务器");
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("服务器未开启");
                }
                if(notifyOperateInfo!=null){
                    notifyOperateInfo.cancelDialog();
                }
                e.printStackTrace();
            }

        }
    }

    private class HeartThread extends Thread{
        @Override
        public void run() {
            heartRun = true;
            super.run();
            int times=0;
            if(notifyProcessDialogInfo!=null){
                notifyProcessDialogInfo.showInfo("正在联网");
            }
            while ((heartRun)&&(!interrupted())){
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                if(info!=null && info.isAvailable()){
                    Log.d(tag,"is online");
                    if(notifyProcessDialogInfo!=null){
                        notifyProcessDialogInfo.showInfo("已联网");
                    }
                    break;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(times == 6){
                    if(notifyOperateInfo!=null){
                        notifyOperateInfo.cancelDialog();
                    }
                }else{
                    times++;
                }



            }

            while ((!interrupted())&&(heartRun)){
                if (connected){//已连接服务器
                    try {
                        send.write(heartString.getBytes());
                        send.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    socketClient = new Socket();
                    receiverThread = new ReceiverThread();
                    receiverThread.start();
                }


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clientCtrl.endHeartThread();
        }
    }
}