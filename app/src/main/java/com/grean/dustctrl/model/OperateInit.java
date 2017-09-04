package com.grean.dustctrl.model;

import android.content.Context;
import android.content.Intent;

import com.grean.dustctrl.myApplication;
import com.tools;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateInit {
    private long nextCalTime,interval;
    private boolean autoCalEnable;
    private Context context;

    public String getAutoNextTime(){
        myApplication app = myApplication.getInstance();
        autoCalEnable = app.getConfigBoolean("AutoCalibrationEnable");
        nextCalTime = app.getConfigLong("AutoCalTime");
        interval = app.getConfigLong("AutoCalInterval");
        if (autoCalEnable){
            return "下次自动校准时间:"+ tools.timestamp2string(nextCalTime);

        }else {
            return "下次校准时间:--";
        }

    }

    public OperateInit (Context context){
        this.context = context;

    }

    public void setAutoCalTime(){
        if(nextCalTime!= 0){
            long now = tools.nowtime2timestamp();
            long next = tools.calcNextTime(now,nextCalTime,interval);
            nextCalTime = next;
            myApplication.getInstance().saveConfig("AutoCalTime",next);
            Intent intent = new Intent();
            intent.setAction("autoCalibration");
            intent.putExtra("enable",autoCalEnable);
            intent.putExtra("date",nextCalTime);
            context.sendBroadcast(intent);
        }
    }

}
