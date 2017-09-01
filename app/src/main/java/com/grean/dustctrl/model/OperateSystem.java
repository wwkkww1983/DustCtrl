package com.grean.dustctrl.model;

import android.util.Log;
import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.myApplication;
import com.tools;


/**
 * Created by Administrator on 2017/8/30.
 */

public class OperateSystem {
    private static final String tag = "OperateSystem";
    private CtrlCommunication com = CtrlCommunication.getInstance();
    private long interval = 24*3600000l,date;
    public OperateSystem(){

    }

    public void ctrlDo(int num,boolean key){
        com.ctrlDo(num,key);
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
        myApplication.getInstance().saveConfig("AutoCalibration",key);
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
        string = String.valueOf(interval / 3600000l);
        return  string;
    }

    public void setAutoCalInterval(String string){
        long l = Integer.valueOf(string)*3600000l;
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
}
