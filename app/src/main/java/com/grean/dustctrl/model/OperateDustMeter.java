package com.grean.dustctrl.model;

import com.grean.dustctrl.CtrlCommunication;
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
    public static final int TSP=0,PM10=1,PM2_5=2;
    private int dustName;

    public OperateDustMeter(NotifyOperateInfo info){
        dustMeterRun = com.isDustMeterRun();
        this.info = info;
        dustName = myApplication.getInstance().getConfigInt("DustName");
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
        myApplication.getInstance().saveConfig("DustParaK",k);
        return String.valueOf(k);
    }

    public void setParaK(String paraK){
        float k = Float.valueOf(paraK);
        com.getData().setParaK(k);
        myApplication.getInstance().saveConfig("DustParaK",k);
    }

    public int getDustName(){
        return myApplication.getInstance().getConfigInt("DustName");
    }

    public void setDustName(int name){
        if(name <DustNames.length){
            dustName = name;
            myApplication.getInstance().saveConfig("DustName",name);
        }
    }

    public String getParaKString(){
        float k = com.getData().getParaK();
        return String.valueOf(k);
    }

    public boolean getCtrlDo(int num){
        return com.getData().getCtrlDo(num);
    }


}
