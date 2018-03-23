package com.grean.dustctrl.model;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.NoiseCalibrationListener;
import com.grean.dustctrl.NoiseCommunication;
import com.grean.dustctrl.SocketServerTask;
import com.grean.dustctrl.SocketTask;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.presenter.UpDateProcessFragment;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;


/**
 * Created by Administrator on 2017/8/30.
 */

public class OperateSystem {
    private static final String tag = "OperateSystem";
    private CtrlCommunication com = CtrlCommunication.getInstance();
    private long interval = 24*3600000l,date;
    private CalibrationNoiseThread calibrationNoiseThread;
    public OperateSystem(){

    }

    public void ctrlDo(int num,boolean key){
        com.ctrlDo(num,key);
    }

    public void startDownLoadSoftware(Context context,String url,NotifyProcessDialogInfo processDialogInfo,NotifyOperateInfo operateInfo){
        new Thread(new DownloadRunnable(context,url,processDialogInfo,operateInfo)).start();
    }


    public String[] getClientProtocolNames(){
        return GetProtocols.CLIENT_PROTOCOL_DEFAULT_NAMES;
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
        myApplication.getInstance().saveConfig("AlarmDust",alarm);
        ScanSensor.getInstance().setAlarmDust(alarm);
    }

    public String getServerIp(){
        return myApplication.getInstance().getConfigString("ServerIp");
    }

    public String getServerPort(){
        return String.valueOf(myApplication.getInstance().getConfigInt("ServerPort"));
    }

    public String getAutoCalNextTime(){
        String string;
        date = myApplication.getInstance().getConfigLong("AutoCalTime");
        string = tools.timestamp2string(date);
        return string;
    }

    public boolean getAutoCalibrationEnable(){
        return myApplication.getInstance().getConfigBoolean("AutoCalibrationEnable");
    }

    public void setAutoCalibrationEnable(boolean key){
        myApplication.getInstance().saveConfig("AutoCalibrationEnable",key);
    }

    public void setAutoTime(String string){
        date = tools.string2timestamp(string);
        myApplication.getInstance().saveConfig("AutoCalTime",date);
    }

    public long getAutoTimeDate(){
        return  date;
    }

    public String getAutoCalInterval(){
        String string;
        interval = myApplication.getInstance().getConfigLong("AutoCalInterval");
        //string = String.valueOf(interval / 3600000l);
        string = String.valueOf(interval / 60000l);
        return  string;
    }

    public void setAutoCalInterval(String string){
        //long l = Integer.valueOf(string)*3600000l;
        long l = Integer.valueOf(string)*60000l;
        interval = l;
        myApplication.getInstance().saveConfig("AutoCalInterval",l);
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
        return com.getMotorRounds();
    }

    public int getMotorTime(){
        return com.getMotorTime();
    }

    public void setMotorSetting(int rounds,int time){
        com.setMotorTime(time);
        com.setMotorRounds(rounds);
        myApplication.getInstance().saveConfig("MotorRounds",rounds);
        myApplication.getInstance().saveConfig("MotorTime",time);
    }

    public void testMotor(boolean forward){
        if(forward) {
            com.setMotorSetting(CtrlCommunication.MotorForward);
        }else{
            com.setMotorSetting(CtrlCommunication.MotorBackward);
        }
    }

    public void calNoise(UpDateProcessFragment upDateProcessFragment){
        calibrationNoiseThread = new CalibrationNoiseThread(upDateProcessFragment);
        NoiseCommunication.getInstance().sendCalibrationCmd(calibrationNoiseThread);
        calibrationNoiseThread.start();
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

    public void resetComFlag(){
        com.resetComFlag();
    }


}
