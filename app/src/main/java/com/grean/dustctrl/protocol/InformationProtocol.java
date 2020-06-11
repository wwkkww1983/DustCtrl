package com.grean.dustctrl.protocol;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.SystemDateTime;
import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.NoiseCalibrationListener;
import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.UploadingProtocol.UploadingConfigFormat;
import com.grean.dustctrl.device.DevicesManage;
import com.grean.dustctrl.device.DustMeterControl;
import com.grean.dustctrl.model.OperateDustMeter;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;

import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by weifeng on 2017/9/8.
 */

public class InformationProtocol extends Observable implements GeneralInfoProtocol,NoiseCalibrationListener {
    private static final String tag = "InformationProtocol";
    private String stateString;
    private SensorData data = new SensorData();
    private boolean autoCalEnable,dustMeterCalBgOk,dustMeterCalSpanOk,exportDataResult,alarm;
    private long autoCalTime,autoCalInterval;
    private String serverIp,mnCode;
    private int serverPort,pumpTime,laserTime,dustMeterCalProcess,exportDataProcess,calibrationNoiseSate;
    private float paraK,paraB;
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
        this.data.setParaB(data.getParaB());
        this.data.setValue(data.getValue());
        this.data.setAirTemperature(data.getAirTemperature());
        this.data.setAirHumidity(data.getAirHumidity());
        this.data.setAirPressure(data.getAirPressure());
        this.data.setNoise(data.getNoise());
        this.data.setWindDirection(data.getWindDirection());
        this.data.setWindForce(data.getWindForce());
        this.data.setLoHumidity(data.getLoHumidity());
        this.data.setLoTemp(data.getLoTemp());
        this.data.calcHiDewPoint();
        this.data.calcLoDewPoint();
        this.data.setAcIn(data.isAcIn());
        this.data.setBatteryLow(data.isBatteryLow());
        this.data.setCalPos(data.isCalPos());
        this.data.setMeasurePos(data.isMeasurePos());
        this.data.setHeatPwm(data.getHeatPwm());
        this.data.setCtrlDo(data.getCtrlDo());
        this.data.setPipeTemp(data.getPipeTemp());
        this.data.setHiHumidity(data.getHiHumidity());
        this.data.setHiTemp(data.getHiTemp());
    }

    @Override
    public void notifySystemState(String string) {
        this.stateString = string;
    }

    public void loadSetting(ReadWriteConfig config){
        this.config = config;

        autoCalEnable = config.getConfigBoolean("auto_calibration_enable");
        autoCalTime = config.getConfigLong("auto_calibration_date");
        autoCalInterval = config.getConfigLong("auto_calibration_interval");
        paraK = config.getConfigFloat("dust_para_k");
        paraB = config.getConfigFloat("dust_para_b");

        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        serverIp = format.getServerAddress();
        serverPort = format.getServerPort();
        mnCode = format.getMnCode();
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
                UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
                format.setServerAddress(ip);
                format.setServerPort(port);
                try {
                    config.saveUploadSetting(format.getConfigString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ProtocolTcpServer.getInstance().reconnectServer(context,null,null);
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
                config.saveConfig("auto_calibration_enable",enable);
                config.saveConfig("auto_calibration_date",date);
                config.saveConfig("auto_calibration_interval",interval);

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
        float value = DevicesManage.getInstance().getData().getValue();
        paraK = target / value;
        DevicesManage.getInstance().getData().setParaK(paraK);
        config.saveConfig("dust_para_k",paraK);
    }

    @Override
    public void setDustParaK(float paraK) {
        this.paraK = paraK;
        DevicesManage.getInstance().getData().setParaK(paraK);
        config.saveConfig("dust_para_k",paraK);
       // Log.d(tag,"记录数据");
        notifyObservers(new LogFormat("修改斜率为"+String.valueOf(paraK)));
        setChanged();
    }

    @Override
    public void setDustParaB(float paraB) {
        this.paraB = paraB;
        DevicesManage.getInstance().getData().setParaB(paraB);
        config.saveConfig("dust_para_b",paraB);
        notifyObservers(new LogFormat("修改截距为"+String.valueOf(paraB)));
        setChanged();
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
        return GetProtocols.getInstance().getDataBaseProtocol().getHourData(dateStart,dateStart+3600000l);
    }

    @Override
    public GeneralHistoryDataFormat getHistoryData(long startDate, long endDate) {
        return GetProtocols.getInstance().getDataBaseProtocol().getData(startDate,endDate);
    }

    @Override
    public GeneralHistoryDataFormat getHistoryHourData(long startDate, long endDate) {
        return GetProtocols.getInstance().getDataBaseProtocol().getHourData(startDate,endDate);
    }

    @Override
    public ArrayList<String> getLog(long endDate) {
        return GetProtocols.getInstance().getDataBaseProtocol().getDayLog(endDate);
    }

    @Override
    public ArrayList<String> getLog(long startDate, long endDate) {
        return GetProtocols.getInstance().getDataBaseProtocol().getLog(startDate,endDate);
    }

    @Override
    public float getParaK() {
        return paraK;
    }

    @Override
    public float getParaB() {
        return paraB;
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
        UploadingConfigFormat format = ProtocolTcpServer.getInstance().getFormat();
        format.setMnCode(code);
        try {
            config.saveUploadSetting(format.getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //GetProtocols.getInstance().getClientProtocol().setMnCode(code);
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
    public void setRhCorrectionEnable(boolean enable) {
        DevicesManage.getInstance().getData().setRhCorrectionEnable(enable);
        config.saveDeviceSetting(DevicesManage.getInstance());
        notifyObservers(new LogFormat("湿度补偿修改为"+String.valueOf(enable)));
        setChanged();
    }

    @Override
    public int getClientProtocolName() {
        return GetProtocols.getInstance().getClientProtocolName();
    }

    @Override
    public String[] getDustNames() {
        return DevicesManage.DustNames;
    }

    @Override
    public int getDustName() {
        return config.getConfigInt("dust_name");
    }

    @Override
    public void setDustName(int name) {
        config.saveConfig("dust_name",name);
    }

    @Override
    public float getAlarmDust() {
        return ScanSensor.getInstance().getAlarmDust();
    }

    @Override
    public void setAlarmDust(float alarm) {
        Log.d(tag,String.valueOf(alarm));
        config.saveConfig("dust_alarm",alarm);
        ScanSensor.getInstance().setAlarmDust(alarm);
    }

    @Override
    public void setClientProtocol(int name) {
        config.saveConfig("ClientProtocol ",name);
        GetProtocols.getInstance().setClientProtocol(name);
    }

    @Override
    public void calDustMeterZero() {

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
        return ProtocolTcpServer.getInstance().isConnected();
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

    @Override
    public int getMotorTime() {
        return DevicesManage.getInstance().getData().getMotorTime()/100;
    }

    @Override
    public int getMotorStep() {
        return DevicesManage.getInstance().getData().getMotorRounds();
    }

    @Override
    public void setMotorTime(int time) {
        config.saveConfig("motor_time",time*100);
        DevicesManage.getInstance().getData().setMotorTime(time*100);
    }

    @Override
    public void setMotorStep(int step) {
        config.saveConfig("motor_rounds",step);
        DevicesManage.getInstance().getData().setMotorRounds(step);
    }

    @Override
    public void setRelay(int num, boolean key) {
        DevicesManage.getInstance().setDo(num,key);
    }

    @Override
    public void ForwardTest() {
        DevicesManage.getInstance().setMotor(DustMeterControl.DustMeterMotorForward);
    }

    @Override
    public void BackwardTest() {
        DevicesManage.getInstance().setMotor(DustMeterControl.DustMeterMotorBackward);
    }

    @Override
    public void ForwardStep() {
        DevicesManage manage = DevicesManage.getInstance();

        int time = manage.getData().getMotorTime();
        int step = manage.getData().getMotorRounds();
        manage.getData().setMotorTime(500);
        manage.getData().setMotorRounds(100);
        manage.setMotor(DustMeterControl.DustMeterMotorForward);
        manage.getData().setMotorTime(time);
        manage.getData().setMotorRounds(step);
    }

    @Override
    public void BackwardStep() {
        DevicesManage manage = DevicesManage.getInstance();

        int time = manage.getData().getMotorTime();
        int step = manage.getData().getMotorRounds();
        manage.getData().setMotorTime(500);
        manage.getData().setMotorRounds(100);
        manage.setMotor(DustMeterControl.DustMeterMotorBackward);
        manage.getData().setMotorTime(time);
        manage.getData().setMotorRounds(step);
    }

    @Override
    public boolean isDustMeterRun() {
        return true;
    }

    @Override
    public void setDustMeterRun(boolean key) {
        //Log.d(tag,"dustMeterRun = "+String.valueOf(true));
    }

    @Override
    public void calibrationNoise() {
        calibrationNoiseSate = 0;
        DevicesManage.getInstance().calibrationNoise(this);
    }

    @Override
    public int getCalibrationNoiseState() {
        return calibrationNoiseSate;
    }

    @Override
    public String[] getDustMeterNames() {
        return DevicesManage.DustMeterNames;
    }

    @Override
    public int getDustMeter() {
        return DevicesManage.getInstance().getDustMeterName();
    }

    @Override
    public void setDustMeter(int name) {
        config.saveConfig("dust_meter_name",name);
    }

    @Override
    public int getCameraOffset() {
        return DevicesManage.getInstance().getCameraOffset();
    }

    @Override
    public void setCameraOffset(int offset) {
        DevicesManage.getInstance().setCameraDirectionOffset(offset);
        config.saveDeviceSetting(DevicesManage.getInstance());
    }

    @Override
    public boolean isCameraEnable() {
        if(config.getConfigInt("camera_name")==3){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void setCameraEnable(boolean enable) {
        if(!enable) {
            config.saveConfig("camera_name", 2);
        }
    }

    @Override
    public void saveUploadingConfig(String configString) {
        config.saveUploadSetting(configString);
    }


    @Override
    public void onResult(String calInfo, boolean success) {
        if(success){
            calibrationNoiseSate = 1;
        }else{
            calibrationNoiseSate = 2;
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
            Log.d(tag,"导出完成");
        }

        @Override
        public void setProcess(int process) {
            exportDataProcess = process;
        }
    }
}
