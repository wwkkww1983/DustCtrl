package com.grean.dustctrl.model;

import android.content.Context;

import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.SystemConfig;
import com.grean.dustctrl.dust.DustMeterLibs;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.process.NotifyScanEnd;
import com.grean.dustctrl.process.ScanSensor;

/**
 * 操作粉尘仪
 * Created by Administrator on 2017/8/29.
 */

public class OperateDustMeter implements NotifyScanEnd{
    private static final String tag = "OperateDustMeter";
    private CtrlCommunication com = CtrlCommunication.getInstance();
    private boolean dustMeterRun;
    private NotifyOperateInfo info;
    private NotifyProcessDialogInfo dialogInfo;
    public static final String[] DustNames = {"TSP","PM10","PM2.5"};
    public static final String[] DustMeters = {"LD-8-G","LD-8-J"};
    public static final int TSP=0,PM10=1,PM2_5=2;
    private int dustName,dustMeter;
    private Context context;

    public OperateDustMeter(NotifyOperateInfo info, Context context){
        dustMeterRun = com.isDustMeterRun();
        this.info = info;
        this.context = context;
        dustName = SystemConfig.getInstance(context).getConfigInt("DustName");
    }

    public boolean isDustMeterRun() {
        return dustMeterRun;
    }

    public void inquireDustMeter(NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.dialogInfo = notifyProcessDialogInfo;
        dialogInfo.showInfo("停止测量");
        ScanSensor.getInstance().stopScan(this);
        ScanSensor.getInstance().inquireDustMeterInfo(info);
    }

    public void switchDustMeter(boolean key){
        if (key){
            com.SendFrame(CtrlCommunication.DustMeterRun);
        }else {
            com.SendFrame(CtrlCommunication.DustMeterStop);
        }
    }

    public void calibrationDustMeter(NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.dialogInfo = notifyProcessDialogInfo;
        dialogInfo.showInfo("停止测量");
        ScanSensor scan = ScanSensor.getInstance();
        scan.stopScan(this);
        scan.calibrationDustMeterWithMan(info,dialogInfo);
        /*CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();*/
    }

    public void calibrationDustMeterZero(NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.dialogInfo = notifyProcessDialogInfo;
        dialogInfo.showInfo("停止测量");
        ScanSensor scan = ScanSensor.getInstance();
        scan.stopScan(this);
        scan.calibrationDustMeterZeroWithMan(info,dialogInfo);
        /*CalibrationDustMeterThread thread = new CalibrationDustMeterThread();
        thread.start();*/
    }

    @Override
    public void onComplete() {
        if (dialogInfo!=null){
            dialogInfo.showInfo("处理中...");
        }
    }

    public String calcParaK (String target){
        float t = Float.valueOf(target);
        float k = t / com.getData().getValue();
        com.getData().setParaK(k);
        SystemConfig.getInstance(context).saveConfig("DustParaK",k);
        return String.valueOf(k);
    }

    public void setParaK(String paraK){
        float k = Float.valueOf(paraK);
        com.getData().setParaK(k);
        SystemConfig.getInstance(context).saveConfig("DustParaK",k);
    }

    public void setParaB(String paraB){
        float b = Float.valueOf(paraB);
        com.getData().setParaB(b);
        SystemConfig.getInstance(context).saveConfig("DustParaB",b);
    }

    public int getDustName(){
        return SystemConfig.getInstance(context).getConfigInt("DustName");
    }

    public int getDustMeter(){
        return SystemConfig.getInstance(context).getConfigInt("DustMeter");
    }

    public void setDustName(int name){
        if(name <DustNames.length){
            dustName = name;
            SystemConfig.getInstance(context).saveConfig("DustName",name);
        }
    }

    public void setDustMeter(int name){
        if(name <DustMeters.length){
            DustMeterLibs.getInstance().setDustMeterName(name);
            SystemConfig.getInstance(context).saveConfig("DustMeter",name);
        }
    }

    public String getParaKString(){
        float k = com.getData().getParaK();
        return String.valueOf(k);
    }

    public String getParaBString(){
        float b=com.getData().getParaB();
        return String.valueOf(b);
    }

    public boolean getCtrlDo(int num){
        return com.getData().getCtrlDo(num);
    }


}
