package com.grean.dustctrl.protocol;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.SystemDateTime;
import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.MainActivity;
import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.SocketTask;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyDataInfo;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by weifeng on 2017/9/8.
 */

public class InformationProtocol implements GeneralInfoProtocol{
    private static final String tag = "InformationProtocol";
    private String stateString;
    private SensorData data = new SensorData();
    private boolean autoCalEnable,dustMeterCalBgOk,dustMeterCalSpanOk,exportDataResult,alarm;
    private long autoCalTime,autoCalInterval;
    private String serverIp,mnCode;
    private int serverPort,pumpTime,laserTime,dustMeterCalProcess,exportDataProcess;
    private float paraK;
    private Context context;
    private ReadWriteConfig config;
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
        paraK = config.getConfigFloat("DustParaK");
        mnCode = config.getConfigString("MnCode");
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
            serverIp = ip;
            serverPort = port;
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
            getAvailableContext();
            Intent intent = new Intent();
            intent.setAction("autoCalibration");
            intent.putExtra("enable",enable);
            intent.putExtra("date",date);
            context.sendBroadcast(intent);
            success = true;

            return success;
        }else {
            return true;
        }
    }

    @Override
    public void calDust(float target) {
        float value = CtrlCommunication.getInstance().getData().getValue();
        paraK = target / value;
        CtrlCommunication.getInstance().getData().setParaK(paraK);
        config.saveConfig("DustParaK",paraK);
    }

    @Override
    public void calDustMeter() {
        dustMeterCalProcess = 0;
        ScanSensor.getInstance().stopScan(null);
        ScanSensor.getInstance().calibrationDustMeterWithMan(null,null);
    }

    @Override
    public void setDustMeterResult(boolean bg, boolean span) {
        this.dustMeterCalBgOk = bg;
        this.dustMeterCalSpanOk = span;
    }

    @Override
    public boolean getDustMeterBg() {
        return dustMeterCalBgOk;
    }

    @Override
    public boolean getDustMeterSpan() {
        return dustMeterCalSpanOk;
    }

    @Override
    public void inquireDustMeterInfo() {
        ScanSensor.getInstance().stopScan(null);
        ScanSensor.getInstance().inquireDustMeterInfo(null);
    }

    @Override
    public void setDustMeterPumpTime(int pumpTime) {
        this.pumpTime = pumpTime;
    }

    @Override
    public void setDustMeterLaserTime(int laserTime) {
        this.laserTime = laserTime;
    }

    @Override
    public int getDustMeterPumpTime() {
        return pumpTime;
    }

    @Override
    public int getDustMeterLaserTime() {
        return laserTime;
    }

    @Override
    public void setDustCalMeterProcess(int process) {
        this.dustMeterCalProcess = process;
    }

    @Override
    public int getDustMeterCalProcess() {
        return dustMeterCalProcess;
    }

    @Override
    public GeneralHistoryDataFormat getHistoryData(long dateStart) {
        return GetProtocols.getInstance().getDataBaseProtocol().getHourData(dateStart);
    }

    @Override
    public ArrayList<String> getLog(long endDate) {
        return GetProtocols.getInstance().getDataBaseProtocol().getDayLog(endDate);
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
    public String getMnCode() {
        return mnCode;
    }

    @Override
    public void setMnCode(String code) {
        mnCode = code;
        config.saveConfig("MnCode",code);
        GetProtocols.getInstance().getClientProtocol().setMnCode(code);
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

    @Override
    public int getExportDataProcess() {
        return exportDataProcess;
    }

    @Override
    public boolean getExportDataResult() {
        return exportDataResult;
    }

    @Override
    public void exportData(long start, long end) {
        exportDataProcess = 0;
        new ExportDataThread(start,end).start();
    }

    @Override
    public String[] getClientProtocolNames() {
        return GetProtocols.CLIENT_PROTOCOL_DEFAULT_NAMES;
    }

    @Override
    public int getClientProtocolName() {
        return GetProtocols.getInstance().getClientProtocolName();
    }

    @Override
    public float getAlarmDust() {
        return ScanSensor.getInstance().getAlarmDust();
    }

    @Override
    public void setAlarmDust(float alarm) {
        Log.d(tag,String.valueOf(alarm));
        config.saveConfig("AlarmDust",alarm);
        ScanSensor.getInstance().setAlarmDust(alarm);
    }

    @Override
    public void setClientProtocol(int name) {
        config.saveConfig("ClientProtocolName",name);
        GetProtocols.getInstance().setClientProtocol(name);
    }

    @Override
    public void calDustMeterZero() {
        dustMeterCalProcess = 0;
        ScanSensor.getInstance().stopScan(null);
        ScanSensor.getInstance().calibrationDustMeterZeroWithMan(null,null);
    }

    @Override
    public void setAlarmMark(boolean alarm) {
            this.alarm = alarm;
    }

    @Override
    public boolean getAlarmMark() {
        return alarm;
    }

    @Override
    public boolean isServerConnected() {
        return SocketTask.getInstance().isConnected();
    }

    @Override
    public void setSystemDate(int year, int mon, int day, int hour, int min, int second) {
        try {
            SystemDateTime.setDateTime(year,mon,day,hour,min,second);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ExportDataThread extends Thread implements ExportDataProcessListener{
        private long start,end;

        public ExportDataThread(long start, long  end){
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            exportDataResult = GetProtocols.getInstance().getDataBaseProtocol().exportData2File(start,end,this);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            exportDataProcess = 100;
        }

        @Override
        public void setProcess(int process) {
            exportDataProcess = process;
        }
    }
}
