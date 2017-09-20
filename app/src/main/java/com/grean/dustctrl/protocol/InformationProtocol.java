package com.grean.dustctrl.protocol;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
    private String stateString;
    private SensorData data = new SensorData();
    private boolean autoCalEnable,dustMeterCalBgOk,dustMeterCalSpanOk,exportDataResult;
    private long autoCalTime,autoCalInterval;
    private String serverIp,mnCode;
    private int serverPort,pumpTime,laserTime,dustMeterCalProcess,exportDataProcess;
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
        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >"+ String.valueOf(dateStart)+" and date <"+String.valueOf(dateStart + 3600000l);
        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date asc",new String[]{});
        int index = 0;
        ArrayList<Float> item;
        while ((cursor.moveToNext())&&index < 100){
            format.addDate(cursor.getLong(0));
            item = new ArrayList<Float>();
            item.add(cursor.getFloat(1));
            item.add(cursor.getFloat(3));
            item.add(cursor.getFloat(4));
            item.add(cursor.getFloat(5));
            item.add(cursor.getFloat(6));
            item.add(cursor.getFloat(7));
            item.add(cursor.getFloat(8));
            format.addItem(item);
            index++;
        }
        db.close();
        helperDbTask.close();
        return format;
    }

    @Override
    public ArrayList<String> getLog(long endDate) {
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        statement = "date >"+ String.valueOf(endDate - 3600000l*24)+" and date <"+String.valueOf(endDate);

        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM log WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index=0;
        while ((cursor.moveToNext())&&(index < 100)){
            list.add(cursor.getString(2));
            index++;
        }
        db.close();
        helperDbTask.close();
        return list;
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

    private ArrayList<String> exportDataBase(long start,long end){
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        if (start > end){
            statement = "date <"+ String.valueOf(start)+" and date >"+String.valueOf(end);
        }else{
            statement = "date >"+ String.valueOf(start)+" and date <"+String.valueOf(end);
        }

        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date desc",new String[]{});
        list.add("时间  TSP mg/m³ 温度 ℃ 湿度 % 气压 hPa 风速 m/s 风向 ° 噪声 dB");
        while (cursor.moveToNext()){
            String string = tools.timestamp2string(cursor.getLong(0))+"  ";
            string+=tools.float2String3(cursor.getFloat(1))+"  ";
            string+=tools.float2String3(cursor.getFloat(3))+"  ";
            string+=tools.float2String3(cursor.getFloat(4))+"  ";
            string+=tools.float2String3(cursor.getFloat(5))+"  ";
            string+=tools.float2String3(cursor.getFloat(6))+"  ";
            string+=tools.float2String3(cursor.getFloat(7))+"  ";
            string+=tools.float2String3(cursor.getFloat(8))+"  ";
            list.add(string);
        }
        db.close();
        helperDbTask.close();
        return list;

    }

    private class ExportDataThread extends Thread{
        private long start,end;

        public ExportDataThread(long start, long  end){
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            exportDataResult=true;
            String pathName = "/mnt/usbhost/Storage01/GREAN/"; // /storage/sdcard0/GREAN/
            String fileName = "数据"+tools.nowTime2FileString()+"导出.txt";
            File path = new File(pathName);
            File file = new File(pathName + fileName);
            boolean success = true;
            try {
                if (!path.exists()) {
                    //Log.d("TestFile", "Create the path:" + pathName);
                    path.mkdir();
                }
                if (!file.exists()) {
                   // Log.d("TestFile", "Create the file:" + fileName);
                    file.createNewFile();
                }
                exportDataProcess = 10;
                // 导出日志
                BufferedWriter bw = new BufferedWriter(new FileWriter(file,false)); // true// 是添加在后面// false// 是每次写新的
                bw.write("历史数据 \r\n");


                ArrayList<String> list = exportDataBase(start,end);
                for (String tmp : list) {
                    bw.write(tmp + "\r\n");
                    //Log.d("写入SD", tmp);
                }
                bw.flush();
                bw.close();
                exportDataProcess = 80;

            } catch (IOException e) {
                e.printStackTrace();
                exportDataResult = false;
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            exportDataProcess = 100;

        }
    }
}
