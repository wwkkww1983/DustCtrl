package com.grean.dustctrl.model;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.util.Log;


import com.grean.dustctrl.NoiseCalibrationListener;
import com.grean.dustctrl.R;
import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.device.DevicesManage;
import com.grean.dustctrl.device.DustMeterControl;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.presenter.UpDateProcessFragment;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import org.json.JSONException;



/**
 * Created by Administrator on 2017/8/30.
 */

public class OperateSystem {
    private static final String tag = "OperateSystem";
    private long interval = 24*3600000l,date;
    private CalibrationNoiseThread calibrationNoiseThread;
    private Context context;
    private ReadWriteConfig store;
    public OperateSystem(Context context,ReadWriteConfig store){
        this.context = context;
        this.store = store;
    }

    public void setCameraDirectionOffset(int offset){
        DevicesManage.getInstance().setCameraDirectionOffset(offset);
        store.saveDeviceSetting(DevicesManage.getInstance());
    }

    public void setRhCorrectionEnable(boolean enable){
        DevicesManage.getInstance().setRhCorrectionEnable(enable);
        store.saveDeviceSetting(DevicesManage.getInstance());
    }

    public String getParaTempSlope(){
        return String.valueOf(DevicesManage.getInstance().getData().getParaTempSlope());

    }

    public String getParaTempIntercept(){
        return String.valueOf(DevicesManage.getInstance().getData().getParaTempIntercept());
    }

    public String getParaHumiSlope(){
        return String.valueOf(DevicesManage.getInstance().getData().getParaHumiSlope());
    }

    public String[] getCameraNames(){
        return DevicesManage.CameraNames;
    }

    public void saveTempHumiPara(float paraTempSlope,float paraTempIntercept,float paraHumiSlope,float paraHumiIntercept){
        SensorData  data = DevicesManage.getInstance().getData();
        data.setParaTempSlope(paraTempSlope);
        data.setParaTempIntercept(paraTempIntercept);
        data.setParaHumiSlope(paraHumiSlope);
        data.setParaHumiIntercept(paraHumiIntercept);
        store.saveConfig("temp_para_k ",paraTempSlope);
        store.saveConfig("temp_para_b",paraTempIntercept);
        store.saveConfig("humi_para_k",paraHumiSlope);
        store.saveConfig("humi_para_b",paraHumiIntercept);
    }

    public String getParaHumiIntercept(){
        return String.valueOf(DevicesManage.getInstance().getData().getParaHumiIntercept());
    }

    public int getCameraDirectionOffset(){
        return DevicesManage.getInstance().getCameraOffset();
    }

    public boolean isRhCorrectionEnable(){
        return DevicesManage.getInstance().isRhCorrectionEnable();
    }

    public void ctrlDo(int num,boolean key){
        DevicesManage.getInstance().setDo(num,key);
    }

    public void startDownLoadSoftware(Context context,String url,NotifyProcessDialogInfo processDialogInfo,NotifyOperateInfo operateInfo){
        new Thread(new DownloadRunnable(context,url,processDialogInfo,operateInfo)).start();
    }


    public String[] getClientProtocolNames(){
        return GetProtocols.CLIENT_PROTOCOL_DEFAULT_NAMES;
    }

    public int getCameraName(){
        Log.d(tag,"Camera Name = "+String.valueOf(DevicesManage.getInstance().getCameraName()));
        return DevicesManage.getInstance().getCameraName();
    }

    public int getLedDisplayName(){
        return DevicesManage.getInstance().getLedDisplayName();
    }

    public int getNoiseName(){
        return DevicesManage.getInstance().getNoiseName();
    }


    public boolean isDustMeterSibataLd8(){
        return DevicesManage.getInstance().isSibataLd8();
    }

    public boolean isWindDirLinkageEnable(){
        return DevicesManage.getInstance().isWindDirLinkageEnable();
    }

    public boolean isNoiseMeterEnable(){
        return DevicesManage.getInstance().isNoiseMeterEnable();
    }

    public void saveCameraName(int name){
        store.saveConfig("camera_name",name);
    }

    public int getClientName(){
        return GetProtocols.getInstance().getClientProtocolName();
    }


    private class DownloadRunnable implements Runnable{

        private String url;
        private NotifyProcessDialogInfo info;
        private Context context;
        private NotifyOperateInfo operateInfo;

        public DownloadRunnable(Context context, String url, NotifyProcessDialogInfo info, NotifyOperateInfo operateInfo){
            this.context = context;
            this.url = url;
            this.info = info;
            this.operateInfo = operateInfo;
        }


        private void queryDownloadProcess(long requestId,DownloadManager downloadManager){
            DownloadManager.Query query= new DownloadManager.Query();
            query.setFilterById(requestId);
            try{
                boolean isGoing = true;
                int times = 0;
                while (isGoing){
                    Cursor cursor = downloadManager.query(query);
                    if(cursor!=null && cursor.moveToFirst()){
                        int state = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        switch (state){
                            case DownloadManager.STATUS_SUCCESSFUL:
                                isGoing = false;
                                operateInfo.cancelDialogWithToast("下载成功!");
                                break;
                            case DownloadManager.STATUS_FAILED:
                                isGoing = false;
                                operateInfo.cancelDialogWithToast("下载失败!");
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                isGoing = false;
                                operateInfo.cancelDialogWithToast("下载失败!");
                                break;
                            case DownloadManager.STATUS_PENDING:
                                info.showInfo("准备下载");
                                break;
                            case DownloadManager.STATUS_RUNNING:
                                Log.d(tag,"下载中");
                                break;
                            default:
                                break;

                        }
                        Thread.sleep(200);
                        if(cursor!=null){
                            cursor.close();
                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d(tag,"下载完成");
        }

        private long startDownload(){
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long requestId = downloadManager.enqueue(CreateRequest(url));
            myApplication.getInstance().getConfig().put("ID",requestId);
            queryDownloadProcess(requestId,downloadManager);
            return requestId;
        }

        private DownloadManager.Request CreateRequest(String url){

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            Log.d(tag,url);
           // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,"123.apk");
            request.setDescription("杭州绿洁扬尘在线监测系统");
            return request;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            startDownload();
        }
    }

    public String getAlarmDust(){
        return tools.float2String3(ScanSensor.getInstance().getAlarmDust());
    }

    public void setAlarmDust(String alarmString){
        float alarm = Float.valueOf(alarmString);
        store.saveConfig("dust_alarm",alarm);
        DevicesManage.getInstance().getData().setDustAlarm(alarm);
        ScanSensor.getInstance().setAlarmDust(alarm);
    }

    public String getServerIp(){
        return ProtocolTcpServer.getInstance().getFormat().getServerAddress();
    }

    public String getServerPort(){
        return String.valueOf(ProtocolTcpServer.getInstance().getFormat().getServerPort());
    }

    public String getAutoCalNextTime(){
        String string;
        date = store.getConfigLong("auto_calibration_date");
        string = tools.timestamp2string(date);
        return string;
    }

    public boolean getAutoCalibrationEnable(){
        return store.getConfigBoolean("auto_calibration_enable");
    }

    public void setAutoCalibrationEnable(boolean key){
        store.saveConfig("auto_calibration_enable",key);
    }

    public void setBackupServerEnable(boolean enable){
        ProtocolTcpServer.getInstance().getFormat().setBackupServerEnable(enable);
        try {
            store.saveUploadSetting(ProtocolTcpServer.getInstance().getFormat().getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSystemSetting(int dustName,int dustMeterName,int cameraName,int noiseName,int ledDisplayName){
        store.saveConfig("dust_name",dustName);
        store.saveConfig("dust_meter_name",dustMeterName);
        store.saveConfig("led_display_name",ledDisplayName);
        store.saveConfig("noise_name",noiseName);
        store.saveConfig("camera_name",cameraName);
    }

    public boolean isBackupServerEnable(){
        return ProtocolTcpServer.getInstance().getFormat().isBackupServerEnable();
    }

    public void setAutoTime(String string){
        date = tools.string2timestamp(string);
        store.saveConfig("auto_calibration_date",date);
    }

    public long getAutoTimeDate(){
        return  date;
    }

    public String getAutoCalInterval(){
        String string;
        interval = store.getConfigLong("auto_calibration_interval");
        string = String.valueOf(interval / 60000l);
        return  string;
    }

    public void setAutoCalInterval(String string){
        long l = Integer.valueOf(string)*60000l;
        interval = l;
        store.saveConfig("auto_calibration_interval",l);
    }



    public String calNexTime(String string){
        Log.d(tag,string);
        long plan = tools.string2timestamp(string);
        long now = tools.nowtime2timestamp();
        long next;
        Log.d(tag,String.valueOf(interval));
        if (interval!=0){
            next = tools.calcNextTime(now,plan,interval);
            Log.d(tag,String.valueOf(next));
        }else {
            next = now + 24*3600l;
        }
        return tools.timestamp2string(next);
    }

    public int getMotorRounds(){
        return DevicesManage.getInstance().getData().getMotorRounds();
    }

    public int getMotorTime(){
         return DevicesManage.getInstance().getData().getMotorTime();
    }

    public void setMotorSetting(int rounds,int time){
        DevicesManage.getInstance().getData().setMotorTime(time);
        DevicesManage.getInstance().getData().setMotorRounds(rounds);
        store.saveConfig("motor_rounds",rounds);
        store.saveConfig("motor_time",time);
    }

    public void testMotor(boolean forward){
        if(forward) {
            DevicesManage.getInstance().setMotor(DustMeterControl.DustMeterMotorForward);
        }else{
            DevicesManage.getInstance().setMotor(DustMeterControl.DustMeterMotorBackward);
        }
    }

    public void calNoise(UpDateProcessFragment upDateProcessFragment){
        calibrationNoiseThread = new CalibrationNoiseThread(upDateProcessFragment);
        calibrationNoiseThread.start();
    }

    public String getVersionName(Context context){
        String versionName = "1.0.3";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            //versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        Log.d(tag,"versin Name="+versionName);
        return context.getResources().getString(R.string.software_version)+";补丁版本:"+versionName;
    }

    private class CalibrationNoiseThread extends Thread implements NoiseCalibrationListener{
        private UpDateProcessFragment upDateProcessFragment;
        private boolean success;

        public CalibrationNoiseThread(UpDateProcessFragment upDateProcessFragment){
            this.upDateProcessFragment = upDateProcessFragment;
        }
        @Override
        public void run() {
            success = false;
            upDateProcessFragment.setContent("正在校准声级计");
            DevicesManage.getInstance().calibrationNoise(this);
            for (int i=0;i<10;i++){
                upDateProcessFragment.setProcess(i*10);
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            if(!success) {
                upDateProcessFragment.cancelFragmentWithToast("声级计无响应");
            }
        }

        @Override
        public void onResult(String calInfo, boolean success) {
            this.success = success;
            if(success){
                upDateProcessFragment.cancelFragmentWithToast("校准成功!");
            }else{
                upDateProcessFragment.cancelFragmentWithToast("校准失败!");
            }
        }
    }

}
