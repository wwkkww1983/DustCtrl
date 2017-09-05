package com.grean.dustctrl;

import android.util.Log;

import com.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by weifeng on 2017/9/5.
 */

public class SocketServerTask {
    private static final String tag = "SocketServerTask";
    private static SocketServerTask instance = new SocketServerTask();
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean stop = true;
    private ReceiveThread receiveThread;
    private AcceptThread acceptThread;

    private SocketServerTask(){

    }

    public static SocketServerTask getInstance() {
        return instance;
    }

    public void startSocketServer(){
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread{
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            receiveThread = new ReceiveThread(clientSocket);
            stop = false;
            receiveThread.start();

        }
    }

    private class ReceiveThread extends Thread{

        private InputStream inputStream;
        private byte[] buf;
        private Socket s;
        ReceiveThread (Socket s){

            try {
                this.s = s;
                this.inputStream = s.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!stop&&s.isConnected()&&!s.isInputShutdown()&&!s.isClosed()){
                buf = new byte[512];
                try {
                    int count = inputStream.read(buf);
                    Log.d(tag, "receive:"+tools.bytesToHexString(buf,count)+";sizeof"+String.valueOf(count));
                } catch (IOException e) {
                    e.printStackTrace();
                    stop = true;
                }


            }
            stop = true;
            Log.d(tag,"结束链接");
        }
    }


}
