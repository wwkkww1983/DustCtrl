package com.grean.dustctrl;

import android.util.Log;

import com.grean.dustctrl.protocol.GeneralServerProtocol;
import com.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private GeneralServerProtocol serverProtocol;

    private SocketServerTask(){

    }

    public static SocketServerTask getInstance() {
        return instance;
    }

    public void startSocketServer(GeneralServerProtocol serverProtocol,int port){
        this.serverProtocol = serverProtocol;
        acceptThread = new AcceptThread(port);
        acceptThread.start();
    }

    public void stopServer(){
        stop = true;
        serverRun = false;
    }

    private class AcceptThread extends Thread{

        int port;

        public AcceptThread(int port){
            this.port = port;
        }
        @Override
        public void run() {
            serverRun = true;

                try {
                    serverSocket = new ServerSocket(port);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            while (serverRun){
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(clientSocket.isConnected()) {
                    Log.d(tag,"开启接收线程");
                    receiveThread = new ReceiveThread(clientSocket);
                    stop = false;
                    receiveThread.start();
                }else{
                    Log.d(tag,"未连接");
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class ReceiveThread extends Thread{

        private InputStream inputStream;
        private OutputStream outputStream;
        private byte[] buf = new byte[512];;
        private Socket s;
        ReceiveThread (Socket s){

            try {
                this.s = s;
                this.outputStream = s.getOutputStream();
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

                        byte [] data = serverProtocol.handleProtocol(buf,count);
                        outputStream.write(data);
                        outputStream.flush();
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
