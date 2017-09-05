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
    private boolean serverRun = true;
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
            serverRun = true;

                try {
                    serverSocket = new ServerSocket(8888);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            while (serverRun){
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
    }

    private class ReceiveThread extends Thread{

        private InputStream inputStream;
        private byte[] buf = new byte[512];;
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
            Log.d(tag,"建立链接");
            while (!stop){
                try {
                    int count = inputStream.read(buf);
                    if(count!=-1){
                        Log.d(tag, "receive:"+tools.bytesToHexString(buf,count)+";sizeof"+String.valueOf(count));
                    }else {
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    stop = true;
                }


            }
            stop = true;
            try {
                s.shutdownInput();
                s.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(tag,"结束链接");
        }
    }


}
