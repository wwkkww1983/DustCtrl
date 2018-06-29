package com.grean.dustctrl.UploadingProtocol;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by weifeng on 2018/6/28.
 */

public class ProtocolTcpServer extends Observable implements  ProtocolCommand{
    private static final String tag = "ProtocolTcpServer";
    private ProtocolState state;
    private UploadingConfigFormat format;
    private static ProtocolTcpServer instance = new ProtocolTcpServer();
    private boolean connected =false,run = false;
    private ConnectThread connectThread;
    private ReceiverThread receiverThread;
    private Socket socketClient;
    private InputStream receive;
    private OutputStream send;
    private NotifyOperateInfo notifyOperateInfo;
    private NotifyProcessDialogInfo notifyProcessDialogInfo;
    private ConcurrentLinkedQueue<byte[]> sendBuff = new ConcurrentLinkedQueue<>();

    private ProtocolTcpServer(){

    }

    public UploadingConfigFormat getFormat() {
        return format;
    }

    public static ProtocolTcpServer getInstance() {
        return instance;
    }

    public void setConfig(UploadingConfigFormat configFormat){
        try {
            format = (UploadingConfigFormat) configFormat.clone();
            if(state!=null){
                state.setConfig(format);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    public void setState(ProtocolState state) {
        this.state = state;
        if(format!=null){
            state.setConfig(format);
        }
    }

    public void connectServer(Context context){
        if((format!=null)&&(!run)){
            connectThread = new ConnectThread(context);
            connectThread.start();

        }
    }

    public void reconnectServer(Context context, NotifyOperateInfo notifyOperateInfo, NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.notifyOperateInfo = notifyOperateInfo;
        this.notifyProcessDialogInfo = notifyProcessDialogInfo;
        if(run){
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

        }else{
            connectThread = new ConnectThread(context);
            connectThread.start();

        }
    }


    @Override
    public boolean executeSendTask(byte[] buff) {
        if(connected) {
            sendBuff.add(buff);
        }
        return connected;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private class ConnectThread extends Thread{
        private Context context;
        public ConnectThread(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            run = true;
            int times  =0;
            while (!interrupted()&&run){
                if(isOnline()){
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

            while ((!interrupted())&&run){
                if (connected){//已连接服务器
                    try {
                        if(!sendBuff.isEmpty()){
                            socketClient.sendUrgentData(0xFF);
                            send.write(sendBuff.poll());
                            send.flush();
                        }
                    } catch (IOException e) {
                        Log.d(tag,"发送失败");
                        try {
                            socketClient.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }else{
                    socketClient = new Socket();
                    receiverThread = new ReceiverThread();
                    receiverThread.start();

                    try {
                        Thread.sleep(19000);
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

        private boolean isOnline(){
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if(info!=null && info.isAvailable()){
                Log.d(tag,"is online");
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("已联网");
                }
                return true;
            }else{
                return false;
            }
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
                Log.d(tag,"IP:"+format.getServerAddress()+" Port:"+String.valueOf(format.getServerPort()));
                socketClient.connect(new InetSocketAddress(format.getServerAddress(),format.getServerPort()),5000);
                socketClient.setTcpNoDelay(true);
                socketClient.setSoLinger(true,30);
                socketClient.setSendBufferSize(10240);
                socketClient.setKeepAlive(true);
                receive = socketClient.getInputStream();
                send = socketClient.getOutputStream();
                socketClient.setOOBInline(true);

                int count;
                byte[] readBuff = new byte[4096];
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("已链接");
                }
                if(notifyOperateInfo!=null){
                    notifyOperateInfo.cancelDialog();
                }

                connected = true;
                setChanged();
                Log.d(tag,"已连接服务器");
                notifyObservers(new LogFormat("已连接服务器"));
                while (connected){
                    if (socketClient.isConnected()&&(!socketClient.isClosed())){
                        while ((count = receive.read(readBuff))!=-1 && connected){
                            state.handleReceiveBuff(readBuff,count);
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
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("服务器未开启");
                }
                if(notifyOperateInfo!=null){
                    notifyOperateInfo.cancelDialog();
                }
                e.printStackTrace();
            }
            setChanged();
            notifyObservers(new LogFormat("中断网络链接"));
        }
    }

    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }


}
