package com.grean.dustctrl.protocol;

import android.content.Context;
import android.util.Log;

import com.grean.dustctrl.SystemLog;
import com.grean.dustctrl.UploadingProtocol.DefaultProtocolState;
import com.grean.dustctrl.UploadingProtocol.HJT212_2017ProtocolState;
import com.grean.dustctrl.UploadingProtocol.ProtocolState;
import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.myApplication;

/**
 * 获取各种协议的接口
 * Created by weifeng on 2017/9/8.
 */

public class GetProtocols {
    private static final String tag = "GetProtocols";
    private static GetProtocols instance = new GetProtocols();
    private ProtocolState protocolState;
    private GeneralClientProtocol clientProtocol;
    private GeneralServerProtocol serverProtocol;
    private GeneralInfoProtocol infoProtocol;
    private GeneralDataBaseProtocol dataBaseProtocol;
    private GeneralCommandProtocol commandProtocol;
    private int clientProtocolName =0;
    public static final int CLIENT_PROTOCOL_DEFAULT=0,CLIENT_PROTOCOL_HJT212 = 1,
            CLIENT_PROTOCOL_MAX = 2;
    public static final String[] CLIENT_PROTOCOL_DEFAULT_NAMES ={"Default","HJ/T-212-2017"};
    private Context context;

    private GetProtocols(){

    }

    public synchronized ProtocolState getProtocolState(){
        if(protocolState==null){

            if(clientProtocolName == CLIENT_PROTOCOL_DEFAULT) {
                protocolState = new DefaultProtocolState(ProtocolTcpServer.getInstance());
            }else if(clientProtocolName == CLIENT_PROTOCOL_HJT212){
                Log.d(tag,"CLIENT_PROTOCOL_HJT212");
                protocolState = new HJT212_2017ProtocolState(ProtocolTcpServer.getInstance());
            }else{
                protocolState = new DefaultProtocolState(ProtocolTcpServer.getInstance());
            }
        }
        return protocolState;
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

    public Context getContext() {
        if(context == null){
            context = myApplication.getInstance().getApplicationContext();
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static GetProtocols getInstance() {
        return instance;
    }

    synchronized public GeneralInfoProtocol getInfoProtocol() {
        if(infoProtocol == null){
            InformationProtocol informationProtocol = new InformationProtocol();
            informationProtocol.addObserver(SystemLog.getInstance(context));
            infoProtocol = informationProtocol;
        }
        return infoProtocol;
    }

    public void setClientProtocol (int name){
        clientProtocolName = name;
        /*if(clientProtocolName == CLIENT_PROTOCOL_DEFAULT) {
            protocolState = new DefaultProtocolState(ProtocolTcpServer.getInstance());
        }else{
            protocolState = new DefaultProtocolState(ProtocolTcpServer.getInstance());
        }*/
    }

    /**
     * 获取当前协议编号
     * @return
     */
    public int getClientProtocolName(){
        return clientProtocolName;
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
