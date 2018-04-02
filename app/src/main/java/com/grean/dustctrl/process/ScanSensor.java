package com.grean.dustctrl.process;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.NoiseCommunication;
import com.grean.dustctrl.dust.DustMeterLibs;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.CalcNextAutoCalibration;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.protocol.ClientDataBaseCtrl;
import com.grean.dustctrl.protocol.GeneralClientProtocol;
import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GeneralInfoProtocol;
import com.grean.dustctrl.protocol.GetProtocols;
import com.grean.dustctrl.protocol.InformationProtocol;
import com.tools;

import java.util.Observable;

/**
 * 扫描传感器
 * Created by Administrator on 2017/8/25.
 */

public class ScanSensor extends Observable implements ClientDataBaseCtrl {
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
    private SensorData data;
    private float alarmDust;
    private double [] sumData = new double[7];
    private int scanTimes = 0;
    private float [] minData = new float[7];

    public boolean isRun() {
        return run;
    }

    public void setNotifyScanSensor(NotifyScanSensor notifyScanSensor) {
        this.notifyScanSensor = notifyScanSensor;
    }

    private ScanSensor(){

    }

    public float getAlarmDust() {
        return alarmDust;
    }

    public void setAlarmDust(float alarmDust) {
        this.alarmDust = alarmDust;
    }

    public void calibrationDustMeterWithMan(NotifyOperateInfo info, NotifyProcessDialogInfo dialogInfo){
        this.info = info;
        this.dialogInfo = dialogInfo;
        run = false;
        CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();
    }

    public void calibrationDustMeterZeroWithMan(NotifyOperateInfo info, NotifyProcessDialogInfo dialogInfo){
        this.info = info;
        this.dialogInfo = dialogInfo;
        run = false;
        CalibrationDustMeterZeroThread thread = new CalibrationDustMeterZeroThread();
        thread.start();
    }

    public void calibrationDustMeterWithAuto (CalcNextAutoCalibration calcNextAutoCalibration){
        this.calcNextAutoCalibration = calcNextAutoCalibration;
        run = false;
        CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();
    }

    @Override
    public void saveMinData(long now) {
        calcMean();
        DbTask helper = new DbTask(context,1);
        SQLiteDatabase db = helper.getReadableDatabase();
        Log.d(tag,"存储数据"+tools.timestamp2string(now));
        ContentValues values = new ContentValues();
        values.put("date",now);
        values.put("dust",minData[GeneralHistoryDataFormat.Dust]);
        if(data!=null) {
            values.put("value", data.getValue());
        }
        values.put("temperature",minData[GeneralHistoryDataFormat.Temperature]);
        values.put("humidity",minData[GeneralHistoryDataFormat.Humidity]);
        values.put("pressure",minData[GeneralHistoryDataFormat.Pressure]);
        values.put("windforce",minData[GeneralHistoryDataFormat.WindForce]);
        values.put("winddirection",minData[GeneralHistoryDataFormat.WindDirection]);
        values.put("noise",minData[GeneralHistoryDataFormat.Noise]);
        db.beginTransaction();
        try{
            db.insert("result",null,values);
            db.setTransactionSuccessful();
        }catch (Exception e){

        }finally {
            db.endTransaction();
        }
        if(data!=null) {
            values = new ContentValues();
            values.put("date", now);
            values.put("hitemp", data.getHiTemp());
            values.put("lotemp", data.getLoTemp());
            values.put("hihumidity", data.getHiHumidity());
            values.put("lohumidity", data.getLoHumidity());
            values.put("pwm", data.getHeatPwm());
            db.beginTransaction();
            try{
                db.insert("detail", null, values);
                db.setTransactionSuccessful();
            }catch (Exception e){

            }finally {
                db.endTransaction();
            }
        }
        db.close();
        helper.close();
    }

    @Override
    public void getRealTimeData(Handler handler) {
        /*if((realTimeData.length >= 7)&&(data!=null)){
            realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
            realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
            realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
            realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
            realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
            realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
            realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
        }*/
        Message msg = new Message();
        msg.what = ClientDataBaseCtrl.UPDATE_REAL_TIME;
        msg.obj = data;
        handler.sendMessage(msg);
    }

    private class CalibrationDustMeterZeroThread extends Thread{

        @Override
        public void run() {
            Log.d(tag,"开始校准");
            setChanged();
            notifyObservers(new LogFormat("开始校准"));
            sendMainFragmentString("停止测量,开始校准");
            CtrlCommunication com;
            com = CtrlCommunication.getInstance();
            GeneralInfoProtocol infoProtocol = GetProtocols.getInstance().getInfoProtocol();
            infoProtocol.notifySystemState("停止测量，开始校准");
            infoProtocol.setDustCalMeterProcess(2);
            GeneralClientProtocol clientProtocol = GetProtocols.getInstance().getClientProtocol();
            clientProtocol.setRealTimeAlarm(GeneralClientProtocol.ALARM_C);
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
            infoProtocol.notifySystemState("正在校零");
            infoProtocol.setDustCalMeterProcess(15);
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
                infoProtocol.notifySystemState("校零成功");
            }else{
                notifyObservers(new LogFormat("校零失败"));
                sendMainFragmentString("校零失败");
                infoProtocol.notifySystemState("校零失败");
            }
            infoProtocol.setDustCalMeterProcess(80);

            com.SendFrame(CtrlCommunication.DustMeterBgEnd);

            infoProtocol.setDustMeterResult(dustMeterInfo.isBgOk(),dustMeterInfo.isSpanOk());
            infoProtocol.setDustCalMeterProcess(90);
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
            infoProtocol.setDustCalMeterProcess(100);
            infoProtocol.notifySystemState("校准结束");
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
            GeneralInfoProtocol infoProtocol = GetProtocols.getInstance().getInfoProtocol();
            infoProtocol.notifySystemState("停止测量，开始校准");
            infoProtocol.setDustCalMeterProcess(2);
            GeneralClientProtocol clientProtocol = GetProtocols.getInstance().getClientProtocol();
            clientProtocol.setRealTimeAlarm(GeneralClientProtocol.ALARM_C);
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
            for(int i=0;i<10;i++) {
                infoProtocol.setDustCalMeterProcess(4+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            if (dialogInfo!=null) {
                dialogInfo.showInfo("本底校准...");
            }
            sendMainFragmentString("正在校零");
            infoProtocol.notifySystemState("正在校零");
            infoProtocol.setDustCalMeterProcess(15);
            com.SendFrame(CtrlCommunication.DustMeterBgStart);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<30;i++){
                infoProtocol.setDustCalMeterProcess(15+i);
                try {
                    Thread.sleep(3300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            infoProtocol.setDustCalMeterProcess(45);
            com.SendFrame(CtrlCommunication.DustMeterBgResult);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<4;i++){
                infoProtocol.setDustCalMeterProcess(46+i);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            DustMeterInfo dustMeterInfo = com.getInfo();

            setChanged();
            String zeroResultString;
            if (dustMeterInfo.isBgOk()){
                notifyObservers(new LogFormat("校零成功"));
                sendMainFragmentString("校零成功");
                zeroResultString = "校零成功";
                infoProtocol.notifySystemState("校零成功");
            }else{
                notifyObservers(new LogFormat("校零失败"));
                sendMainFragmentString("校零失败");
                zeroResultString = "校零失败";
            }
            infoProtocol.notifySystemState(zeroResultString);
            infoProtocol.setDustCalMeterProcess(50);

            com.SendFrame(CtrlCommunication.DustMeterBgEnd);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.setMotorSetting(CtrlCommunication.MotorForward);//转遮光板

            if (dialogInfo!=null) {
                dialogInfo.showInfo("量程校准...");
            }
            sendMainFragmentString("正在校跨");
            int motorTime = com.getMotorTime();
            if(motorTime <= 0){
                motorTime = 1800;
            }

            try {
                Thread.sleep((motorTime+200)*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            infoProtocol.setDustCalMeterProcess(55);

            infoProtocol.notifySystemState(zeroResultString+",正在校跨");
            com.SendFrame(CtrlCommunication.DustMeterSpanStart);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<30;i++){
                infoProtocol.setDustCalMeterProcess(55+i);
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            infoProtocol.setDustCalMeterProcess(85);
            com.SendFrame(CtrlCommunication.DustMeterSpanResult);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            String spanResultString;
            if (dustMeterInfo.isSpanOk()){
                notifyObservers(new LogFormat("校跨成功"));
                sendMainFragmentString("校跨成功");
                spanResultString = "校跨成功";
            }else{
                notifyObservers(new LogFormat("校跨失败"));
                sendMainFragmentString("校跨失败");
                spanResultString = "校跨失败";
            }
            infoProtocol.notifySystemState(zeroResultString+","+spanResultString);
            infoProtocol.setDustMeterResult(dustMeterInfo.isBgOk(),dustMeterInfo.isSpanOk());
            infoProtocol.setDustCalMeterProcess(90);
            com.SendFrame(CtrlCommunication.DustMeterSpanEnd);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(dialogInfo!=null) {
                dialogInfo.showInfo(zeroResultString+","+spanResultString+",结束校准...");
            }
           // sendMainFragmentString("结束校准");
            com.ctrlDo(1,false);
            infoProtocol.setDustCalMeterProcess(95);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            com.setMotorSetting(CtrlCommunication.MotorBackward);//撤回转遮光板
            try {
                Thread.sleep((motorTime + 200)*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            notifyObservers(new LogFormat("结束校准"));
            infoProtocol.setDustCalMeterProcess(100);
            infoProtocol.notifySystemState("校准结束");
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
            DustMeterLibs.getInstance().setDustMeterName(myApplication.getInstance().getConfigInt("DustMeter"));
            CtrlCommunication com;
            com = CtrlCommunication.getInstance();
            NoiseCommunication noiseCom;
            noiseCom = NoiseCommunication.getInstance();
            float paraK = myApplication.getInstance().getConfigFloat("DustParaK");
            float paraB = myApplication.getInstance().getConfigFloat("DustParaB");
            alarmDust = myApplication.getInstance().getConfigFloat("AlarmDust");
            boolean alarm;
            com.setDustParaK(paraK);
            com.setDustParaB(paraB);
            com.setMotorRounds(myApplication.getInstance().getConfigInt("MotorRounds"));
            com.setMotorTime(myApplication.getInstance().getConfigInt("MotorTime"));
            GeneralInfoProtocol infoProtocol = GetProtocols.getInstance().getInfoProtocol();
            GeneralClientProtocol clientProtocol = GetProtocols.getInstance().getClientProtocol();
            //infoProtocol.
            //int i=0;
            setChanged();
            notifyObservers(new LogFormat("开始测量"));
            infoProtocol.notifySystemState("正在测量");
            clientProtocol.setRealTimeAlarm(GeneralClientProtocol.AlARM_N);
            scanTimes = 0;
            for(int i=0;i<7;i++){
                sumData[i] = 0;
            }

            try {
                Thread.sleep(2000);
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
            infoProtocol.setDustMeterPumpTime(dustMeterInfo.getPumpTime());
            infoProtocol.setDustMeterLaserTime(dustMeterInfo.getLaserTime());
            setChanged();
            notifyObservers(new LogFormat("查询粉尘仪:"+string));

            try {
                Thread.sleep(16000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (run){
                com.SendFrame(CtrlCommunication.Inquire);
                com.SendFrame(CtrlCommunication.WindForce);
                com.SendFrame(CtrlCommunication.WindDirection);
                com.SendFrame(CtrlCommunication.AirParameter);
                com.SendFrame(CtrlCommunication.Dust);
                noiseCom.sendFrame(NoiseCommunication.NoiseRealTimeData);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data = com.getData();
                data.calcHiDewPoint();
                data.calcLoDewPoint();
                data.setNoise(noiseCom.getNoiseData());
                if(data.getDust()>=alarmDust){
                    alarm = true;
                    clientProtocol.setRealTimeAlarm(GeneralClientProtocol.ALARM_ADD);
                }else{
                    alarm = false;
                    clientProtocol.setRealTimeAlarm(GeneralClientProtocol.AlARM_N);
                }
                infoProtocol.notifySenorData(data);
                clientProtocol.setRealTimeData(data);
                infoProtocol.setAlarmMark(alarm);
                calcSum(data);
               /* if(i>29){//1min一条数据
                    i=0;
                    DbTask helper = new DbTask(context,1);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    //Log.d(tag,"存储数据");
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
                    values.put("noise",data.getNoise());
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
                }*/

                if(notifyScanSensor!=null){
                    notifyScanSensor.onResult(data);
                    notifyScanSensor.setAlarmDust(alarm);
                }

            }

            if(notifyScanEnd!=null){
                notifyScanEnd.onComplete();
            }
        }
    }

    synchronized private void calcSum(SensorData data){
        sumData[GeneralHistoryDataFormat.Dust] += data.getDust();
        sumData[GeneralHistoryDataFormat.Noise] += data.getNoise();
        sumData[GeneralHistoryDataFormat.Temperature] += data.getAirTemperature();
        sumData[GeneralHistoryDataFormat.Humidity] += data.getAirHumidity();
        sumData[GeneralHistoryDataFormat.Pressure] += data.getAirPressure();
        sumData[GeneralHistoryDataFormat.WindForce] += data.getWindForce();
        sumData[GeneralHistoryDataFormat.WindDirection] += data.getWindDirection();
        scanTimes++;
    }

    synchronized private void calcMean(){
        if(scanTimes != 0) {
            for (int i = 0; i < 7; i++) {
                minData[i] = (float) (sumData[i] / scanTimes);
                sumData[i] = 0d;
            }
        }
        scanTimes = 0;
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
            GeneralInfoProtocol infoProtocol = GetProtocols.getInstance().getInfoProtocol();
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
            if(info!=null) {
                info.showDustMeterInfo(string);
                info.cancelDialog();
            }
            infoProtocol.setDustMeterPumpTime(dustMeterInfo.getPumpTime());
            infoProtocol.setDustMeterLaserTime(dustMeterInfo.getLaserTime());
            ScanSensor.getInstance().restartScanSensor();
            setChanged();
            notifyObservers(new LogFormat("查询粉尘仪:"+string));
        }
    }
}
