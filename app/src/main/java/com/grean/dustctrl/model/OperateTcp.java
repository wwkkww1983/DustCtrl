package com.grean.dustctrl.model;

import android.content.Context;
import com.grean.dustctrl.SystemSettingStore;
import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.UploadingProtocol.UploadingConfigFormat;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateTcp {


    public OperateTcp(){
    }

    public String getLocalIpAddress(){
        return ProtocolTcpServer.getIpAddressString();
    }

    public void setTcpSocketClient(Context context, String ip, int port, String mnCode,int clientProtocolName){
        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        format.setMnCode(mnCode);
        format.setServerAddress(ip);
        format.setServerPort(port);
        SystemSettingStore store = new SystemSettingStore(context);
        try {
            store.saveUploadSetting(format.getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        store.saveConfig("ClientProtocol",clientProtocolName);
    }

    public void setBackupTcpSocketClient(Context context,String ip,int port,String mnCode){
        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        format.setBackupMnCode(mnCode);
        format.setBackupServerAddress(ip);
        format.setBackupServerPort(port);
        SystemSettingStore store = new SystemSettingStore(context);
        try {
            store.saveUploadSetting(format.getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getBackTcpMnCode(){
        return ProtocolTcpServer.getInstance().getFormat().getBackupMnCode();

    }

    public String getBackupServerAddress(){
        return ProtocolTcpServer.getInstance().getFormat().getBackupServerAddress();
    }

    public String getBackupServerPort(){
        return String.valueOf(ProtocolTcpServer.getInstance().getFormat().getBackupServerPort());
    }



    public String getTcpMnCode(){

        return ProtocolTcpServer.getInstance().getFormat().getMnCode();
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
        SystemSettingStore store = new SystemSettingStore(context);
        try {
            store.saveUploadSetting(format.getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
