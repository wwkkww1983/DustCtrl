package com.grean.dustctrl.process;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.CalcNextAutoCalibration;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.tools;

import java.util.Observable;

/**
 * 扫描传感器
 * Created by Administrator on 2017/8/25.
 */

public class ScanSensor extends Observable{
    private static final String tag = "ScanSensor";
    private static ScanSensor instance = new ScanSensor();
    private boolean run = false;
    private ScanSensorThread scanSensorThread;
    private NotifyScanEnd notifyScanEnd;
    private NotifyScanSensor notifyScanSensor;
    private Context context;
    private NotifyOperateInfo info;
    private NotifyProcessDialogInfo dialogInfo;
    private CalcNextAutoCalibration calcNextAutoCalibration;
    public boolean isRun() {
        return run;
    }

    public void setNotifyScanSensor(NotifyScanSensor notifyScanSensor) {
        this.notifyScanSensor = notifyScanSensor;
    }

    private ScanSensor(){

    }

    public void calibrationDustMeterWithMan(NotifyOperateInfo info,NotifyProcessDialogInfo dialogInfo){
        this.info = info;
        this.dialogInfo = dialogInfo;
        run = false;
        CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();
    }

    public void calibrationDustMeterWithAuto (CalcNextAutoCalibration calcNextAutoCalibration){
        this.calcNextAutoCalibration = calcNextAutoCalibration;
        run = false;
        CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();
    }

    private class CalibrationDustMeterThread extends Thread{
        @Override
        public void run() {
            super.run();
            Log.d(tag,"开始校准");
            setChanged();
            notifyObservers(new LogFormat("开始校准"));
            sendMainFragmentString("停止测量,开始校准");
            CtrlCommunication com;
            com = CtrlCommunication.getInstance();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.ctrlDo(1,true);
            /*try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            com.SendFrame(CtrlCommunication.DustMeterStop);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (dialogInfo!=null) {
                dialogInfo.showInfo("本底校准...");
            }
            sendMainFragmentString("正在校零");
            com.SendFrame(CtrlCommunication.DustMeterBgStart);
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.SendFrame(CtrlCommunication.DustMeterBgResult);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DustMeterInfo dustMeterInfo = com.getInfo();

            setChanged();
            if (dustMeterInfo.isBgOk()){
                notifyObservers(new LogFormat("校零成功"));
                sendMainFragmentString("校零成功");
            }else{
                notifyObservers(new LogFormat("校零失败"));
                sendMainFragmentString("校零失败");
            }

            com.SendFrame(CtrlCommunication.DustMeterBgEnd);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (dialogInfo!=null) {
                dialogInfo.showInfo("量程校准...");
            }
            sendMainFragmentString("正在校跨");
            com.SendFrame(CtrlCommunication.DustMeterSpanStart);
            try {
                Thread.sleep(80000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.SendFrame(CtrlCommunication.DustMeterSpanResult);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            if (dustMeterInfo.isSpanOk()){
                notifyObservers(new LogFormat("校跨成功"));
                sendMainFragmentString("校跨成功");
            }else{
                notifyObservers(new LogFormat("校跨失败"));
                sendMainFragmentString("校跨失败");
            }
            com.SendFrame(CtrlCommunication.DustMeterSpanEnd);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(dialogInfo!=null) {
                dialogInfo.showInfo("结束校准...");
            }
           // sendMainFragmentString("结束校准");
            com.ctrlDo(1,false);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            notifyObservers(new LogFormat("结束校准"));

            com.SendFrame(CtrlCommunication.DustMeterRun);
            if(info!=null) {
                info.cancelDialog();
            }
            if(calcNextAutoCalibration!=null){
                calcNextAutoCalibration.onComplete();
            }

            restartScanSensor();
        }
    }

    public static ScanSensor getInstance() {
        return instance;
    }

    public void startScan(Context context){
        this.context = context;

        if (!run) {
            scanSensorThread = new ScanSensorThread();
            scanSensorThread.start();
        }
    }

    private void sendMainFragmentString(String string){
        Intent intent = new Intent();
        intent.setAction("autoCalNextString");
        intent.putExtra("content",string);
        context.sendBroadcast(intent);

    }

    public boolean restartScanSensor(){
        if (context != null){
            if (!run){
                scanSensorThread = new ScanSensorThread();
                scanSensorThread.start();
                return true;
            }else {
                return false;
            }
        }else{
            return false;
        }

    }

    public void stopScan(NotifyScanEnd notifyScanEnd){
        this.notifyScanEnd = notifyScanEnd;
        run = false;
    }

    private class ScanSensorThread extends Thread{

        @Override
        public void run() {
            run = true;
            super.run();
            CtrlCommunication com;
            com = CtrlCommunication.getInstance();
            SensorData data;
            com.setDustParaK(myApplication.getInstance().getConfigFloat("DustParaK"));
            com.setMotorRounds(myApplication.getInstance().getConfigInt("MotorRounds"));
            com.setMotorTime(myApplication.getInstance().getConfigInt("MotorTime"));
            int i=0;
            setChanged();
            notifyObservers(new LogFormat("开始测量"));
            while (run){
                com.SendFrame(CtrlCommunication.Inquire);
                com.SendFrame(CtrlCommunication.Dust);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data = com.getData();
                if(i>299){
                    i=0;
                    DbTask helper = new DbTask(context,1);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    Log.d(tag,"存储数据");
                    ContentValues values = new ContentValues();
                    long l = tools.nowtime2timestamp();
                    values.put("date",l);
                    values.put("dust",data.getDust());
                    values.put("value",data.getValue());
                    values.put("temperature",data.getAirTemperature());
                    values.put("humidity",data.getAirHumidity());
                    values.put("pressure",data.getAirPressure());
                    values.put("windforce",data.getWindForce());
                    values.put("winddirection",data.getWindDirection());
                    db.insert("result",null,values);
                    values = new ContentValues();
                    values.put("date",l);
                    values.put("hitemp",data.getHiTemp());
                    values.put("lotemp",data.getLoTemp());
                    values.put("hihumidity",data.getHiHumidity());
                    values.put("lohumidity",data.getLoHumidity());
                    values.put("pwm",data.getHeatPwm());
                    db.insert("detail",null,values);
                    db.close();
                    helper.close();
                }else{
                    i++;
                }

                if(notifyScanSensor!=null){
                    notifyScanSensor.onResult(data);
                }

            }


            if(notifyScanEnd!=null){
                notifyScanEnd.onComplete();
            }
        }
    }

    public void inquireDustMeterInfo(NotifyOperateInfo info){
        run = false;
        this.info = info;
        InquireInfoThread thread = new InquireInfoThread();
        thread.start();
    }

    private class InquireInfoThread extends Thread{
        @Override
        public void run() {
            CtrlCommunication com;
            com = CtrlCommunication.getInstance();
            super.run();
            Log.d(tag,"开始查询");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.SendFrame(CtrlCommunication.DustMeterPumpTime);
            com.SendFrame(CtrlCommunication.DustMeterLaserTime);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DustMeterInfo dustMeterInfo = com.getInfo();
            String string = "泵运行累计时间:"+String.valueOf(dustMeterInfo.getPumpTime())+"h;激光运行累计时间:"+String.valueOf(dustMeterInfo.getLaserTime())+"h;";
            info.showDustMeterInfo(string);
            info.cancelDialog();
            ScanSensor.getInstance().restartScanSensor();
            Log.d(tag,"结束查询");
            setChanged();
            notifyObservers(new LogFormat("结束查询"));
        }
    }
}
