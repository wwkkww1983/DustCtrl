package com.grean.dustctrl.model;

import android.content.Context;
import android.content.Intent;

import com.grean.dustctrl.SystemConfig;
import com.grean.dustctrl.myApplication;
import com.tools;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateInit {
    private long nextCalTime,interval;
    private boolean autoCalEnable;
    private Context context;
    private String dustName = "TSP:";

    public String getAutoNextTime(){
        SystemConfig config = SystemConfig.getInstance(context);
        autoCalEnable = config.getConfigBoolean("AutoCalibrationEnable");
        nextCalTime = config.getConfigLong("AutoCalTime");
        interval = config.getConfigLong("AutoCalInterval");
        if (autoCalEnable){
            return "下次自动校准时间:"+ tools.timestamp2string(nextCalTime);

        }else {
            return "下次校准时间:--";
        }

    }

    public String getDustName(){
        return dustName;
    }

    public OperateInit (Context context){
        this.context = context;
        int name = SystemConfig.getInstance(context).getConfigInt("DustName");
        dustName = OperateDustMeter.DustNames[name]+":";
    }

    public void setAutoCalTime(){
        if(nextCalTime!= 0){
            long now = tools.nowtime2timestamp();
            long next = tools.calcNextTime(now,nextCalTime,interval);
            nextCalTime = next;
            SystemConfig.getInstance(context).saveConfig("AutoCalTime",next);
            Intent intent = new Intent();
            intent.setAction("autoCalibration");
            intent.putExtra("enable",autoCalEnable);
            intent.putExtra("date",nextCalTime);
            context.sendBroadcast(intent);
        }
    }

}
