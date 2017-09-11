package com.grean.dustctrl.protocol;


import android.content.Context;
import android.content.Intent;

import com.grean.dustctrl.MainActivity;
import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.SocketTask;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.process.SensorData;

import java.net.Socket;

/**
 * Created by weifeng on 2017/9/8.
 */

public class InformationProtocol implements GeneralInfoProtocol{
    private String stateString;
    private SensorData data = new SensorData();
    private boolean autoCalEnable;
    private long autoCalTime,autoCalInterval;
    private String serverIp;
    private int serverPort;
    private float paraK;
    private Context context;
    ReadWriteConfig config;
    @Override
    public String getSystemState() {
        return stateString;
    }

    @Override
    public SensorData getSensorData() {
        return data;
    }

    @Override
    public void notifySenorData(SensorData data) {
        this.data.setParaK(data.getParaK());
        this.data.setValue(data.getValue());
        this.data.setAirTemperature(data.getAirTemperature());
        this.data.setAirHumidity(data.getAirHumidity());
        this.data.setAirPressure(data.getAirPressure());
        this.data.setNoise(data.getNoise());
        this.data.setWindDirection(data.getWindDirection());
        this.data.setWindForce(data.getWindForce());
    }

    @Override
    public void notifySystemState(String string) {
        this.stateString = string;
    }

    public void loadSetting(ReadWriteConfig config){
        this.config = config;
        serverIp = config.getConfigString("ServerIp");
        serverPort = config.getConfigInt("ServerPort");
        autoCalEnable = config.getConfigBoolean("AutoCalibrationEnable");
        autoCalTime = config.getConfigLong("AutoCalTime");
        autoCalInterval = config.getConfigLong("AutoCalInterval");
        paraK = config.getConfigFloat("ParaK");
    }

    private void getAvailableContext(){
        if (context == null){
            context = myApplication.getInstance().getApplicationContext();
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setServer(String ip, int port) {
        if((!ip.equals(serverIp))||(port!=serverPort)) {
            if(config!=null){
                config.saveConfig("ServerIp",ip);
                config.saveConfig("ServerPort",port);
            }
            SocketTask.getInstance().resetSocketClient(ip, port, null, null);
        }
    }

    @Override
    public boolean setAutoCal(boolean enable, long date, long interval) {
        if((enable!=autoCalEnable)||(date!=autoCalTime)||(interval!=autoCalInterval)){
            boolean success = false;
            autoCalInterval = interval;
            autoCalEnable = enable;
            autoCalTime = date;
            if(config!=null){
                config.saveConfig("AutoCalibrationEnable",enable);
                config.saveConfig("AutoCalTime",date);
                config.saveConfig("AutoCalInterval",interval);

            }
            if(context!=null){
                Intent intent = new Intent();
                intent.setAction("autoCalibration");
                intent.putExtra("enable",enable);
                intent.putExtra("date",date);
                context.sendBroadcast(intent);
                success = true;
            }
            return success;
        }else {
            return true;
        }
    }

    @Override
    public void calDust(float target) {

    }

    @Override
    public void calDustMeter() {

    }

    @Override
    public void setDustMeterResult(boolean bg, boolean span) {

    }

    @Override
    public boolean getDustMeterBg() {
        return false;
    }

    @Override
    public boolean getDustMeterSpan() {
        return false;
    }

    @Override
    public int getDustMeterPumpTime() {
        return 0;
    }

    @Override
    public int getDustMeterLaserTime() {
        return 0;
    }

    @Override
    public float getParaK() {
        return paraK;
    }

    @Override
    public String getServerIp() {
        return serverIp;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public boolean getAutoCalEnable() {
        return autoCalEnable;
    }

    @Override
    public long getAutoCalTime() {
        return autoCalTime;
    }

    @Override
    public long getAutoCalInterval() {
        return autoCalInterval;
    }
}
