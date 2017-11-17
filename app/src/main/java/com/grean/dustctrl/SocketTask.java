package com.grean.dustctrl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.protocol.GeneralClientProtocol;
import com.grean.dustctrl.protocol.TcpClientCallBack;
import com.tools;

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
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TCP客户端任务类，单例化
 * Created by Administrator on 2017/9/4.
 */

public class SocketTask implements TcpClientCallBack{
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
    private GeneralClientProtocol clientProtocol;
    private ConcurrentLinkedQueue <byte[]> sendBuff = new ConcurrentLinkedQueue<>();

    public static SocketTask getInstance() {
        return instance;
    }

    private SocketTask(){

    }

    public static boolean isConnected() {
        return connected;
    }

    public void resetSocketClient(String ip, int port, NotifyOperateInfo notifyOperateInfo, NotifyProcessDialogInfo notifyProcessDialogInfo){
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

    public void startSocketHeart(String ip, int port, Context context, SocketClientCtrl clientCtrl, GeneralClientProtocol clientProtocol){
        this.clientProtocol = clientProtocol;
        this.context = context;
        this.clientCtrl = clientCtrl;
        this.serverIp = ip;
        this.serverPort = port;
        if (!heartRun){
            heartThread = new HeartThread();
            heartThread.start();
        }
    }

    @Override
    public boolean addOneFrame(byte[] data) {
        //Log.d(tag,"connected"+String.valueOf(connected));
        if (connected){
            //Log.d(tag,new String(data));
            sendBuff.add(data);

        }
        return connected;
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

                int count;
                byte[] readBuff = new byte[4096];
                if(notifyProcessDialogInfo!=null){
                    notifyProcessDialogInfo.showInfo("已链接");
                }
                if(notifyOperateInfo!=null){
                    notifyOperateInfo.cancelDialog();
                }

                connected = true;
                Log.d(tag,"已连接服务器");
                while (connected){
                    if (socketClient.isConnected()&&(!socketClient.isClosed())){
                        while ((count = receive.read(readBuff))!=-1 && connected){
                            clientProtocol.handleProtocol(readBuff,count);
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

    /*作者：A_客
    链接：http://www.jianshu.com/p/be244fb85a4e
    來源：简书
    著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。*/

    /**
     * 得到无线网关的IP地址
     *
     * @return
     */
    private void getAllIp() {

        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("tag", "网络名字" + interfaceName);

                Enumeration<InetAddress> enumIpAddr = networkInterface
                        .getInetAddresses();

                while (enumIpAddr.hasMoreElements()) {
                    // 返回枚举集合中的下一个IP地址信息
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    Log.i("tag", inetAddress.getHostAddress() + "哪个类型的   "+inetAddress.getClass().toString());

                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
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
            Log.d(tag,"IP=V"+getIpAddressString());
            //getAllIp();
            while ((!interrupted())&&(heartRun)){
                if (connected){//已连接服务器
                    try {
                        /*send.write(heartString.getBytes());
                        send.flush();*/

                        if(!sendBuff.isEmpty()){
                            socketClient.sendUrgentData(0xFF);
                           //Log.d(tag,"socket is"+String.valueOf(socketClient.isClosed()));
                            send.write(sendBuff.poll());
                            send.flush();
                            //Log.d(tag,"success send rest buff size is "+String.valueOf(sendBuff.size()));
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
            clientCtrl.endHeartThread();
        }
    }
}
