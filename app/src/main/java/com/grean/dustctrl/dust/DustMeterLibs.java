package com.grean.dustctrl.dust;

import android.util.Log;

/**
 * Created by weifeng on 2018/4/2.
 */

public class DustMeterLibs {
    private static final String tag = "DustMeterLibs";
    private DustMeterController dustMeterController;
    private int dustMeterName;
    private static DustMeterLibs instance = new DustMeterLibs();

    public static DustMeterLibs getInstance() {
        return instance;
    }

    private void selectController(int name){
        Log.d(tag,String.valueOf(name));
        switch (name){
            case 0:
                dustMeterController = new SibataLd8Gean();
                break;
            case 1:
                dustMeterController = new SibataLd8Japan();
                break;
            case 2:
                dustMeterController = new LyjdLpm1000();
                break;
            default:
                dustMeterController = new SibataLd8Gean();
                break;
        }
    }

    public DustMeterController getDustMeterController() {
        if(dustMeterController==null){
            selectController(dustMeterName);
        }
        return dustMeterController;
    }

    public void setDustMeterController(DustMeterController dustMeterController) {
        this.dustMeterController = dustMeterController;
    }

    public int getDustMeterName() {
        return dustMeterName;
    }

    public void setDustMeterName(int dustMeterName) {
        if(dustMeterName!=this.dustMeterName) {
            this.dustMeterName = dustMeterName;
            selectController(dustMeterName);
        }
    }

    private DustMeterLibs(){

    }
}
