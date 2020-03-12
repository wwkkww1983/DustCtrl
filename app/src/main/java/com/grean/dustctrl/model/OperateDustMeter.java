package com.grean.dustctrl.model;


import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.device.DevicesManage;
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
    private NotifyOperateInfo info;
    private NotifyProcessDialogInfo dialogInfo;
    private ReadWriteConfig config;

    public OperateDustMeter(NotifyOperateInfo info, ReadWriteConfig config){
        this.info = info;
        this.config = config;
    }

    public void switchDustMeter(boolean key){
        DevicesManage.getInstance().setDustMeterRun(key);
    }

    public void calibrationDustMeter(NotifyProcessDialogInfo notifyProcessDialogInfo){
        this.dialogInfo = notifyProcessDialogInfo;
        dialogInfo.showInfo("停止测量");
        ScanSensor scan = ScanSensor.getInstance();
        scan.stopScan(this);
        scan.calibrationDustMeterWithMan(info,dialogInfo);
    }

    public boolean isDustMeterRun(){
        return DevicesManage.getInstance().isDustMeterRun();
    }

    @Override
    public void onComplete() {
        if (dialogInfo!=null){
            dialogInfo.showInfo("处理中...");
        }
    }


    public void setParaK(String paraK){
        float k = Float.valueOf(paraK);
        DevicesManage.getInstance().getData().setParaK(k);
        config.saveConfig("dust_para_k",k);
    }

    public String getDustMeterWorkedInfo(){
        return  "泵运行累计时间:"+String.valueOf(DevicesManage.getInstance().getData().getPumpTime())
                +"h;激光运行累计时间:"+String.valueOf(DevicesManage.getInstance().getData().getLaserTime())
                +"h;激光运行累计时间:"+String.valueOf(DevicesManage.getInstance().getData().getLaserTime())
                +"h;";
    }

    public void setParaB(String paraB){
        float b = Float.valueOf(paraB);
        DevicesManage.getInstance().getData().setParaB(b);
        config.saveConfig("dust_para_b",b);
    }

    public int getDustName(){
        return DevicesManage.getInstance().getDustName();
    }

    public int getDustMeter(){
        return DevicesManage.getInstance().getDustMeterName();
    }

    public void setDustName(int name){
        config.saveConfig("dust_name",name);
    }

    public void setDustMeter(int name){
        config.saveConfig("dust_meter_name",name);

    }

    public String getParaKString(){
        float k = DevicesManage.getInstance().getData().getParaK();
        return String.valueOf(k);
    }

    public String getParaBString(){
        float b=DevicesManage.getInstance().getData().getParaB();
        return String.valueOf(b);
    }

    public boolean getCtrlDo(int num){
        return DevicesManage.getInstance().getData().getCtrlDo(num);
    }


}
