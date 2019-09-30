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
    //private ProtocolState state,backupState;
    private UploadingConfigFormat format,backupFormat;
    private static ProtocolTcpServer instance = new ProtocolTcpServer();
    private ConnectThread connectThread,backupConnectThread;
    /*private ReceiverThread receiverThread,backupReceiveThread;
    private Socket socketClient,backupSocketClient;
    private InputStream receive,backupReceive;
    private OutputStream send,backupSend;
    private NotifyOperateInfo notifyOperateInfo;
    private NotifyProcessDialogInfo notifyProcessDialogInfo;*/
    private TcpServerInfo mainServerInfo,backupServerInfo;
    private ProtocolCommand backupProtocolCommand = new BackupTcpServer();
    private ConcurrentLinkedQueue<byte[]> sendBuff = new ConcurrentLinkedQueue<>(),
            backupSendBuff = new ConcurrentLinkedQueue<>();

    private ProtocolTcpServer(){

    }

    public UploadingConfigFormat getFormat() {
        return format;
    }

    public static ProtocolTcpServer getInstance() {
        return instance;
    }

    public void setConfig(UploadingConfigFormat configFormat){

        format = (UploadingConfigFormat) configFormat.clone();

        backupFormat  = (UploadingConfigFormat) configFormat.clone();

        if(mainServerInfo!=null){
            mainServerInfo.getProtocolState().setConfig(format);
        }
        if(backupServerInfo!=null){
            backupServerInfo.getProtocolState().setConfig(backupFormat);
        }

        mainServerInfo = new TcpServerInfo("main",format.getServerAddress(),
                format.getServerPort(),sendBuff);
        backupServerInfo = new TcpServerInfo("backup",backupFormat.getBackupServerAddress(),
                backupFormat.getBackupServerPort(),backupSendBuff);
    }

    public void setState(ProtocolState state) {
        mainServerInfo.setProtocolState(state);
        if(format!=null){
            mainServerInfo.getProtocolState().setConfig(format);
        }
    }

    public void setBackupState(ProtocolState state){
        backupServerInfo.setProtocolState(state);
        if(backupFormat!=null){
            backupServerInfo.getProtocolState().setConfig(backupFormat);
        }

    }

    synchronized public void connectServer(Context context){
        if(mainServerInfo!=null) {
            Log.d(tag,"main server start");
            if ((format != null) && (!mainServerInfo.isRun())) {
                new ConnectThread(context, mainServerInfo).start();

            }
        }
    }

    synchronized public void connectBackupServer(Context context){
        if(backupServerInfo!=null){
            Log.d(tag,"backup server start");
            if ((backupFormat != null) && (!backupServerInfo.isRun())) {

                new ConnectThread(context, backupServerInfo).start();

            }
        }
    }

    public void reconnectServer(Context context, NotifyOperateInfo notifyOperateInfo, NotifyProcessDialogInfo notifyProcessDialogInfo){
        mainServerInfo.setNotifyOperateInfo(notifyOperateInfo);
        mainServerInfo.setNotifyProcessDialogInfo( notifyProcessDialogInfo);
        if(mainServerInfo.isRun()){
            if(mainServerInfo.getSocket()!=null){
                if(mainServerInfo.getSocket().isConnected()){
                    try {
                        mainServerInfo.getSocket().shutdownInput();
                        mainServerInfo.getSocket().shutdownOutput();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            if(mainServerInfo!=null) {
                new ConnectThread(context, mainServerInfo).start();
            }
        }
    }

    public ProtocolCommand getBackupProtocolCommand() {
        return backupProtocolCommand;
    }

    private class TcpServerInfo {
        private boolean connected = false,run = false;
        private String ip,name;
        private int port;
        private ProtocolState protocolState;
        private ConcurrentLinkedQueue<byte[]> send;
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ReceiverThread receiverThread;
        private NotifyOperateInfo notifyOperateInfo;
        private NotifyProcessDialogInfo notifyProcessDialogInfo;
        public TcpServerInfo(String name,String ip,int port,ConcurrentLinkedQueue<byte[]> buff){
            this.ip = ip;
            this.name = name;
            this.port = port;
            send = buff;
            /*this.socket = socket;
            this.inputStream = inputStream;
            this.outputStream  =outputStream;
            this.receiverThread = receiverThread;*/
        }

        public NotifyOperateInfo getNotifyOperateInfo() {
            return notifyOperateInfo;
        }

        public void setNotifyOperateInfo(NotifyOperateInfo notifyOperateInfo) {
            this.notifyOperateInfo = notifyOperateInfo;
        }

        public NotifyProcessDialogInfo getNotifyProcessDialogInfo() {
            return notifyProcessDialogInfo;
        }

        public void setNotifyProcessDialogInfo(NotifyProcessDialogInfo notifyProcessDialogInfo) {
            this.notifyProcessDialogInfo = notifyProcessDialogInfo;
        }

        public void setReceiverThread(ReceiverThread receiverThread) {
            this.receiverThread = receiverThread;
        }

        public ReceiverThread getReceiverThread() {
            return receiverThread;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public Socket getSocket() {
            return socket;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void setRun(boolean run) {
            this.run = run;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setProtocolState(ProtocolState protocolState) {
            this.protocolState = protocolState;
        }

        public boolean isConnected() {
            return connected;
        }

        public boolean isRun() {
            return run;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public ConcurrentLinkedQueue<byte[]> getSend() {
            return send;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        public ProtocolState getProtocolState() {
            return protocolState;
        }
    }

    private class BackupTcpServer implements ProtocolCommand{

        @Override
        public boolean executeSendTask(byte[] buff) {
            if(backupServerInfo.isConnected()) {
                //Log.d(tag,backupServerInfo.name+"executeSendTask");
                backupServerInfo.getSend().add(buff);
            }
            return backupServerInfo.isConnected();
        }

        @Override
        public boolean isConnected() {
            return backupServerInfo.isConnected();
        }

        @Override
        public void reconnect() {

        }
    }

    @Override
    public boolean executeSendTask(byte[] buff) {
        if(mainServerInfo.isConnected()) {
            //Log.d(tag,mainServerInfo.name+"executeSendTask");
            mainServerInfo.getSend().add(buff);
        }
        return mainServerInfo.isConnected();
    }

    @Override
    public boolean isConnected() {
        return mainServerInfo.isConnected();
    }

    @Override
    public void reconnect() {
        if(mainServerInfo.isRun()){
            if(mainServerInfo.getSocket()!=null){
                if(mainServerInfo.getSocket().isConnected()){
                    try {
                        mainServerInfo.getSocket().shutdownInput();
                        mainServerInfo.getSocket().shutdownOutput();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            /*connectThread = new ConnectThread(context);
            connectThread.start();*/

        }
    }


    private class ConnectThread extends Thread{
        private Context context;
        private TcpServerInfo info;
        public ConnectThread(Context context,TcpServerInfo info){
            this.context = context;
            this.info = info;
        }

        @Override
        public void run() {
            info.setRun(true);
            int times  =0;
            while (!interrupted()&&info.isRun()){
                if(isOnline()){
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(times == 6){
                if(info.getNotifyOperateInfo()!=null){
                    info.getNotifyOperateInfo().cancelDialog();
                }
                }else{
                    times++;
                }
            }

            while ((!interrupted())&&info.isRun()){
                if (info.isConnected()){//已连接服务器
                    try {
                        if(!info.getSend().isEmpty()){
                            info.getSocket().sendUrgentData(0xFF);
                            info.getOutputStream().write(info.getSend().poll());
                            info.getOutputStream().flush();
                            //Log.d(tag,info.name+" send buff rest size "+String.valueOf(sendBuff.size())
                                   // +"ID="+String.valueOf(getId()));
                        }
                    } catch (IOException e) {
                        Log.d(tag,"发送失败");
                        try {
                            info.getSocket().close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }else{
                    info.setSocket(new Socket());
                    info.setReceiverThread(new ReceiverThread(info));
                    info.getReceiverThread().start();
                    /*receiverThread = new ReceiverThread();
                    receiverThread.start();*/

                    try {
                        Thread.sleep(29900);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(100);
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
                if(mainServerInfo.getNotifyProcessDialogInfo()!=null){
                    mainServerInfo.getNotifyProcessDialogInfo().showInfo("已联网");
                }
                return true;
            }else{
                return false;
            }
        }
    }

    private class ReceiverThread extends Thread{
        TcpServerInfo info;
        public ReceiverThread (TcpServerInfo info){
            this.info = info;
        }

        @Override
        public void run() {
            super.run();
            try {
                if(mainServerInfo.getNotifyProcessDialogInfo()!=null){
                    mainServerInfo.getNotifyProcessDialogInfo().showInfo("新建链接");
                }
                Log.d(tag,info.name+"IP:"+info.getIp()+" Port:"+String.valueOf(info.getPort())
                        +"Thread ID="+String.valueOf(getId()));
                info.getSocket().connect(new InetSocketAddress(info.getIp(),info.getPort()),5000);
                info.getSocket().setTcpNoDelay(true);
                info.getSocket().setSoLinger(true,30);
                info.getSocket().setSendBufferSize(10240);
                info.getSocket().setKeepAlive(true);
                info.setInputStream(info.getSocket().getInputStream());
                info.setOutputStream(info.getSocket().getOutputStream());
                info.getSocket().setOOBInline(true);

                int count;
                byte[] readBuff = new byte[4096];
                if(info.getNotifyProcessDialogInfo()!=null){
                    info.getNotifyProcessDialogInfo().showInfo("已链接");
                }
                if(info.getNotifyOperateInfo()!=null){
                    info.getNotifyOperateInfo().cancelDialog();
                }

                info.setConnected(true);
                setChanged();
                Log.d(tag,info.name+"已连接服务器" +"Thread ID="+String.valueOf(getId()));
                notifyObservers(new LogFormat(info.name+"已连接服务器"));
                info.getProtocolState().handleNewConnect();
                while (info.isConnected()){
                    if (info.getSocket().isConnected()&&(!info.getSocket().isClosed())){
                        while ((count = info.getInputStream().read(readBuff))!=-1 && info.isConnected()){
                            info.getProtocolState().handleReceiveBuff(readBuff,count);
                        }
                        info.setConnected(false);
                        break;
                    }else {
                        info.setConnected(false);
                    }
                    Log.d(tag,"one turn");
                }
            } catch (IOException e) {
                info.setConnected(false);
                Log.d(tag,"找不到服务器" +"Thread ID="+String.valueOf(getId()));
                info.getProtocolState().handleNetError();
                if(info.getNotifyProcessDialogInfo()!=null){
                    info.getNotifyProcessDialogInfo().showInfo("服务器未开启" +"Thread ID="+String.valueOf(getId()));
                }
                if(info.getNotifyOperateInfo()!=null){
                    info.getNotifyOperateInfo().cancelDialog();
                }
                e.printStackTrace();
            }
            finally {
                info.setConnected(false);
                try {
                    info.getSocket().close();
                    Log.d(tag,info.name+"关闭链接" +"Thread ID="+String.valueOf(getId()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
