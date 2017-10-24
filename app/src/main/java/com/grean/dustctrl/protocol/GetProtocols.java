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
    private GeneralCommandProtocol commandProtocol;
    private int clientProtocolName =0;
    public static final int CLIENT_PROTOCOL_DEFAULT=0,CLIENT_PROTOCOL_HJT212 = 1,CLIENT_PROTOCOL_DB12T725 = 2,CLIENT_PROTOCOL_MAX = 3;
    public static final String[] CLIENT_PROTOCOL_DEFAULT_NAMES ={"Default","HJ/T-212","DB12T 725-2017"};
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

    public void setClientProtocol (int name){
        clientProtocolName = name;
    }

    /**
     * 获取当前协议编号
     * @return
     */
    public int getClientProtocolName(){
        return clientProtocolName;
    }

    synchronized public GeneralClientProtocol getClientProtocol() {
        if(clientProtocol==null){
            if(clientProtocolName == CLIENT_PROTOCOL_DEFAULT) {
                clientProtocol = new TcpClient(SocketTask.getInstance());
            }else if(clientProtocolName == CLIENT_PROTOCOL_HJT212){
                clientProtocol = new TcpClientHjt212(SocketTask.getInstance());
            }else if(clientProtocolName == CLIENT_PROTOCOL_DB12T725){
                clientProtocol = new TcpClientDB12t725(SocketTask.getInstance());
            }else{
                clientProtocol = new TcpClient(SocketTask.getInstance());
            }
        }
        return clientProtocol;
    }

    public GeneralCommandProtocol getGeneralCommandProtocol(){
        if(commandProtocol==null){
           commandProtocol = new Hjt212CommandProtocol();
        }
        return commandProtocol;
    }

    synchronized public GeneralServerProtocol getServerProtocol() {
        if (serverProtocol == null){
            serverProtocol = new TcpServer();
        }
        return serverProtocol;
    }
}
