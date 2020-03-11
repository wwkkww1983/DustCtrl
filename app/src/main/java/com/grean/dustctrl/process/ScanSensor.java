package com.grean.dustctrl.process;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.LogFormat;
import com.grean.dustctrl.UploadingProtocol.ProtocolState;
import com.grean.dustctrl.device.DevicesManage;
import com.grean.dustctrl.presenter.CalcNextAutoCalibration;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.protocol.ClientDataBaseCtrl;
import com.grean.dustctrl.protocol.GeneralDataBaseProtocol;
import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GeneralInfoProtocol;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import java.util.ArrayList;
import java.util.Observable;

/**
 * 扫描传感器
 * Created by Administrator on 2017/8/25.
 */

public class ScanSensor extends Observable implements ClientDataBaseCtrl,NotifyMainFragment,NotifySystemLog {
    private static final String tag = "ScanSensor";
    private static ScanSensor instance = new ScanSensor();
    private boolean run = false,minUploadRun =false;
    private ScanSensorThread scanSensorThread;
    private NotifyScanEnd notifyScanEnd;
    private NotifyScanSensor notifyScanSensor;
    private Context context;
    private NotifyOperateInfo info;
    private NotifyProcessDialogInfo dialogInfo;
    private CalcNextAutoCalibration calcNextAutoCalibration;
    private ProtocolState protocolState,backupProtocolState;
    private SensorData data = DevicesManage.getInstance().getData(); ;
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


    public void setProtocolState(ProtocolState protocolState) {
        this.protocolState = protocolState;
    }

    public void setBackupProtocolState(ProtocolState backupProtocolState) {
        this.backupProtocolState = backupProtocolState;
    }

    public float getAlarmDust() {
        return alarmDust;
    }

    public void setAlarmDust(float alarmDust) {
        this.alarmDust = alarmDust;
        DevicesManage.getInstance().getData().setDustAlarm(alarmDust);
    }

    public void calibrationDustMeterWithMan(NotifyOperateInfo info, NotifyProcessDialogInfo dialogInfo){
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

    @Override
    public void saveMinData(long now) {
        calcMean();
        DbTask helper = new DbTask(context,4);
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues values;
        if(data!=null) {
            values = new ContentValues();
            values.put("date", now);
            values.put("dust", minData[GeneralHistoryDataFormat.Dust]);
            values.put("value", data.getValue());
            values.put("temperature", minData[GeneralHistoryDataFormat.Temperature]);
            values.put("humidity", minData[GeneralHistoryDataFormat.Humidity]);
            values.put("pressure", minData[GeneralHistoryDataFormat.Pressure]);
            values.put("windforce", minData[GeneralHistoryDataFormat.WindForce]);
            values.put("winddirection", minData[GeneralHistoryDataFormat.WindDirection]);
            values.put("noise", minData[GeneralHistoryDataFormat.Noise]);
            db.beginTransaction();
            try {
                db.insert("result", null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {

            } finally {
                db.endTransaction();
            }
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

        DevicesManage.getInstance().onMinDataResult(data);
        DevicesManage.getInstance().onMinResultAlarm(minData[GeneralHistoryDataFormat.Dust]);
/*
        if(ledDisplay!=null){
            ledDisplay.onResult(data);
        }*/
    }

    @Override
    public void getRealTimeData(Handler handler) {
        Message msg = new Message();
        msg.what = ClientDataBaseCtrl.UPDATE_REAL_TIME;
        msg.obj = data;
        handler.sendMessage(msg);
    }

    private float[] calcMean(GeneralHistoryDataFormat format){
        float[] mean = {0f,0f,0f,0f,0f,0f,0f};
        int size = format.getSize();
        for(int i=0;i<size;i++){
            ArrayList<Float> list = format.getItem(i);
            for(int j=0;j<7;j++){
                mean[j] += list.get(j)/size;
            }
        }
        return mean;
    }

    @Override
    public void saveHourData(long now) {
        DbTask helper = new DbTask(context,4);
        SQLiteDatabase db = helper.getReadableDatabase();
        Log.d(tag,"存储小时数据"+tools.timestamp2string(now));
        //提取分钟数据

        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >"+ String.valueOf(now-3600000l)+" and date <="+String.valueOf(now);
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date asc",new String[]{});
        if(cursor.getCount()>0) {//有数据
            ArrayList<Float> item;
            while (cursor.moveToNext()) {
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
            }
            cursor.close();
            //计算小时数据
            float[] mean = calcMean(format);

            ContentValues values = new ContentValues();
            values.put("date",now);
            values.put("dust",mean[GeneralHistoryDataFormat.Dust]);
            values.put("temperature",mean[GeneralHistoryDataFormat.Temperature]);
            values.put("humidity",mean[GeneralHistoryDataFormat.Humidity]);
            values.put("pressure",mean[GeneralHistoryDataFormat.Pressure]);
            values.put("windforce",mean[GeneralHistoryDataFormat.WindForce]);
            values.put("winddirection",mean[GeneralHistoryDataFormat.WindDirection]);
            values.put("noise",mean[GeneralHistoryDataFormat.Noise]);
            db.beginTransaction();
            try{
                db.insert("result_hour",null,values);
                db.setTransactionSuccessful();
            }catch (Exception e){

            }finally {
                db.endTransaction();
            }

        }else{
            cursor.close();
        }
        db.close();
        helper.close();
    }

    @Override
    public void writeLog(String string) {
        notifyObservers(new LogFormat(string));
        setChanged();
    }

    private class CalibrationDustMeterThread extends Thread{
        @Override
        public void run() {
            super.run();
            Log.d(tag,"开始校准");
            DevicesManage.getInstance().calibrationDustMeter(GetProtocols.getInstance().getInfoProtocol(),
                    instance,dialogInfo,instance);

            if(info!=null) {
                info.cancelDialog();
            }
            if(calcNextAutoCalibration!=null){
                calcNextAutoCalibration.onComplete();
            }

            restartScanSensor();
        }

    }

    private class MinUploadThread extends Thread{
        private ProtocolState protocolState;

        public MinUploadThread(ProtocolState state){
            this.protocolState = state;
        }
        @Override
        public void run() {
            minUploadRun = true;
            GeneralDataBaseProtocol dataBaseProtocol = GetProtocols.getInstance().getDataBaseProtocol();
            dataBaseProtocol.loadMinDate();
            dataBaseProtocol.setMinDataInterval(60000l);//设置为1分钟间隔
            long now = tools.nowtime2timestamp();
            long lastMinDate;
            long lastHourDate;
            if(now > dataBaseProtocol.getNextMinDate()) {
                lastMinDate = dataBaseProtocol.calcNextMinDate(now);
            }else{
                lastMinDate = dataBaseProtocol.getNextMinDate();
            }
            //Log.d(tag,"加载下次小时数据时间1 now="+tools.timestamp2string(now)+tools.timestamp2string(dataBaseProtocol.getNextHourDate()));
            if(now > dataBaseProtocol.getNextHourDate()) {
                lastHourDate = dataBaseProtocol.calcNextHourDate(now);
            }else{
                lastHourDate = dataBaseProtocol.getNextHourDate();
            }
            //Log.d(tag,"加载下次小时数据时间2 now="+tools.timestamp2string(now)+tools.timestamp2string(lastHourDate));
            protocolState.uploadSystemTime(now,lastMinDate,lastHourDate-60*60*1000l);
            if(backupProtocolState!=null){
                backupProtocolState.uploadSystemTime(now,lastMinDate,lastHourDate-60*60*1000l);
            }

            while (minUploadRun&&(!interrupted())) {
                now = tools.nowtime2timestamp();
                //Log.d(tag,"loop");

                if(now >  lastMinDate){//发送分钟数据
                    //Log.d(tag,"发送分钟数据"+String.valueOf(protocolState==null));
                    saveMinData(lastMinDate);
                    protocolState.uploadMinDate(now,lastMinDate);
                    if(backupProtocolState!=null){
                        backupProtocolState.uploadMinDate(now,lastMinDate);
                    }
                    lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                    dataBaseProtocol.saveMinDate();
                }else if(now > lastHourDate){
                    Log.d(tag,"发送小时数据"+tools.timestamp2string(lastHourDate));
                    saveHourData(lastHourDate);
                    protocolState.uploadHourDate(now,lastHourDate);
                    if(backupProtocolState!=null){
                        backupProtocolState.uploadHourDate(now,lastHourDate);
                    }
                    lastHourDate = dataBaseProtocol.calcNextHourDate(now);
                    dataBaseProtocol.saveHourDate();
                }else{
                    protocolState.uploadSecondDate(now);
                    if(backupProtocolState!=null){
                        backupProtocolState.uploadSecondDate(now);
                    }
                }

                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            minUploadRun = false;
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

    public void sendMainFragmentString(String string){
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
            alarmDust = DevicesManage.getInstance().getData().getDustAlarm();
            boolean alarm;
            GeneralInfoProtocol infoProtocol = GetProtocols.getInstance().getInfoProtocol();

            notifyObservers(new LogFormat("开始测量"));
            setChanged();
            infoProtocol.notifySystemState("正在测量");
            scanTimes = 0;
            for(int i=0;i<7;i++){
                sumData[i] = 0;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DevicesManage.getInstance().inquireDustMeterWorkedTime();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String string = "泵运行累计时间:"+String.valueOf(data.getPumpTime())
                    +"h;激光运行累计时间:"+String.valueOf(data.getLaserTime())
                    +"h;";
            infoProtocol.setDustMeterPumpTime(data.getPumpTime());
            infoProtocol.setDustMeterLaserTime(data.getLaserTime());

            notifyObservers(new LogFormat("查询粉尘仪:"+string));
            setChanged();
            try {
                Thread.sleep(16000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //cameraControl = GetProtocols.getInstance().getCameraControl();

            if(!minUploadRun){
                new MinUploadThread(GetProtocols.getInstance().getProtocolState()).start();
            }
            if(protocolState!=null){
                protocolState.setAlarmValue(alarmDust);
            }
            while (run){
                DevicesManage.getInstance().inquire();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                data.calcHiDewPoint();
                data.calcLoDewPoint();
                //cameraControl.setWindDirection((int) data.getWindDirection());

                if(data.getDust()>=alarmDust){
                    alarm = true;
                }else{
                    alarm = false;
                }
                DevicesManage.getInstance().setAlarmRelay(alarm);
                infoProtocol.notifySenorData(data);
                infoProtocol.setAlarmMark(alarm);
                calcSum(data);
                if(notifyScanSensor!=null){
                    notifyScanSensor.setServerOnline(infoProtocol.isServerConnected());
                    notifyScanSensor.onResult(data);
                    notifyScanSensor.setAlarmDust(alarm);
                }

                if(protocolState!=null){
                    protocolState.setRealTimeData(data);
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
        sumData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
        scanTimes++;
    }

    synchronized private void calcMean(){
        if(scanTimes != 0) {
            for (int i=0;i<GeneralHistoryDataFormat.WindDirection;i++){
                minData[i] = (float) (sumData[i] / scanTimes);
                sumData[i] = 0d;
            }
            minData [GeneralHistoryDataFormat.WindDirection]
                    = (float) sumData[GeneralHistoryDataFormat.WindDirection];
            sumData[GeneralHistoryDataFormat.WindDirection] = 0d;
            for (int i = (GeneralHistoryDataFormat.WindDirection+1);
                 i < GeneralHistoryDataFormat.MAX; i++) {
                minData[i] = (float) (sumData[i] / scanTimes);
                sumData[i] = 0d;
            }
        }
        scanTimes = 0;
    }

}
