package com.grean.dustctrl.model;

import android.content.Context;

import com.grean.dustctrl.SystemConfig;
import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.UploadingProtocol.UploadingConfigFormat;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.protocol.GetProtocols;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateTcp {
    NotifyProcessDialogInfo notifyProcessDialogInfo;
    private NotifyOperateInfo info;

    public OperateTcp(NotifyOperateInfo info){
        this.info= info;
    }

    public String getLocalIpAddress(){
        return ProtocolTcpServer.getIpAddressString();
    }

    public void setTcpSocketClient(Context context, String ip, int port, String mnCode, NotifyProcessDialogInfo notifyProcessDialogInfo, int clientProtocolName){
        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        format.setMnCode(mnCode);
        format.setServerAddress(ip);
        format.setServerPort(port);
        try {
            SystemConfig.getInstance(context).saveConfig("UploadConfig",format.getConfigString());//固化
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SystemConfig.getInstance(context).saveConfig("ClientProtocol",clientProtocolName);
        GetProtocols.getInstance().setClientProtocol(clientProtocolName);
        ProtocolTcpServer.getInstance().reconnectServer(context,info,notifyProcessDialogInfo);
    }

    public String getTcpMnCode(){
        return myApplication.getInstance().getConfigString("MnCode");
    }

    public String getLng(){
        return String.valueOf(ProtocolTcpServer.getInstance().getFormat().getLng());
    }

    public String getLat(){
        return String.valueOf(ProtocolTcpServer.getInstance().getFormat().getLat());
    }

    public void setLocation(Context context,String lngString,String latString){
        Double lng = Double.valueOf(lngString),lat = Double.valueOf(latString);
        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        format.setLat(lat);
        format.setLng(lng);
        try {
            SystemConfig.getInstance(context).saveConfig("UploadConfig",format.getConfigString());//固化
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
