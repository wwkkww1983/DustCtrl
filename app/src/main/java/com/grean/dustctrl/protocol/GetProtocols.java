package com.grean.dustctrl.protocol;

/**
 * 获取各种协议的接口
 * Created by weifeng on 2017/9/8.
 */

public class GetProtocols {
    private static GetProtocols instance = new GetProtocols();
    private GeneralClientProtocol clientProtocol;
    private GeneralServerProtocol serverProtocol;
    private GeneralInfoProtocol infoProtocol;

    private GetProtocols(){

    }

    public static GetProtocols getInstance() {
        return instance;
    }

    public GeneralInfoProtocol getInfoProtocol() {
        if(infoProtocol == null){
            infoProtocol = new InformationProtocol();
        }
        return infoProtocol;
    }

    public GeneralClientProtocol getClientProtocol() {
        if(clientProtocol==null){
            clientProtocol = new TcpClient();
        }
        return clientProtocol;
    }

    public GeneralServerProtocol getServerProtocol() {
        if (serverProtocol == null){
            serverProtocol = new TcpServer();
        }
        return serverProtocol;
    }
}
