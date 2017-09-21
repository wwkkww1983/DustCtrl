package com.grean.dustctrl.protocol;

import android.content.Context;

import com.grean.dustctrl.SocketTask;
import com.grean.dustctrl.myApplication;

/**
 * 获取各种协议的接口
 * Created by weifeng on 2017/9/8.
 */

public class GetProtocols {
    private static GetProtocols instance = new GetProtocols();
    private GeneralClientProtocol clientProtocol;
    private GeneralServerProtocol serverProtocol;
    private GeneralInfoProtocol infoProtocol;
    private GeneralDataBaseProtocol dataBaseProtocol;
    private Context context;

    private GetProtocols(){

    }

    synchronized public GeneralDataBaseProtocol getDataBaseProtocol() {
        if(dataBaseProtocol == null){
            if(context == null){
                context = myApplication.getInstance().getApplicationContext();
            }
            dataBaseProtocol = new TcpDataBase(context);
        }
        return dataBaseProtocol;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static GetProtocols getInstance() {
        return instance;
    }

    synchronized public GeneralInfoProtocol getInfoProtocol() {
        if(infoProtocol == null){
            infoProtocol = new InformationProtocol();
        }
        return infoProtocol;
    }

    synchronized public GeneralClientProtocol getClientProtocol() {
        if(clientProtocol==null){
            clientProtocol = new TcpClient(SocketTask.getInstance());
        }
        return clientProtocol;
    }

    synchronized public GeneralServerProtocol getServerProtocol() {
        if (serverProtocol == null){
            serverProtocol = new TcpServer();
        }
        return serverProtocol;
    }
}
