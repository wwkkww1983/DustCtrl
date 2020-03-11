package com.grean.dustctrl.model;

import android.content.Context;
import android.content.Intent;
import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.device.DevicesManage;
import com.tools;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateInit {
    private long nextCalTime,interval;
    private boolean autoCalEnable;
    private Context context;
    private ReadWriteConfig config;
    private String dustName = "TSP:";

    public String getAutoNextTime(){
        autoCalEnable = config.getConfigBoolean("auto_calibration_enable");
        nextCalTime = config.getConfigLong("auto_calibration_date");
        interval = config.getConfigLong("auto_calibration_interval");
        if (autoCalEnable){
            return "下次自动校准时间:"+ tools.timestamp2string(nextCalTime);

        }else {
            return "下次校准时间:--";
        }

    }

    public String getDustName(){
        return dustName;
    }

    public OperateInit (Context context,ReadWriteConfig config){
        this.context = context;
        this.config = config;
        dustName = DevicesManage.DustNames[DevicesManage.getInstance().getDustName()]+":";
    }

    public void setAutoCalTime(){
        if(nextCalTime!= 0){
            long now = tools.nowtime2timestamp();
            long next = tools.calcNextTime(now,nextCalTime,interval);
            nextCalTime = next;
            config.saveConfig("auto_calibration_date",next);
            Intent intent = new Intent();
            intent.setAction("autoCalibration");
            intent.putExtra("enable",autoCalEnable);
            intent.putExtra("date",nextCalTime);
            context.sendBroadcast(intent);
        }
    }

}
