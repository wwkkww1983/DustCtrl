package com.grean.dustctrl.process;

/**
 * 粉尘仪数据结构
 * Created by Administrator on 2017/8/29.
 */

public class DustMeterInfo {
    private int cpmTime;//CPM周期
    private int pumpTime;//泵累计运行时间
    private int laserTime;//激光累计运行时间
    private boolean bgOk;
    private boolean spanOk;
    public int getCpmTime() {
        return cpmTime;
    }

    public boolean isBgOk() {
        return bgOk;
    }

    public void setBgOk(boolean bgOk) {
        this.bgOk = bgOk;
    }

    public boolean isSpanOk() {
        return spanOk;
    }

    public void setSpanOk(boolean spanOk) {
        this.spanOk = spanOk;
    }

    public void setCpmTime(int cpmTime) {
        this.cpmTime = cpmTime;
    }

    public int getPumpTime() {
        return pumpTime;
    }

    public void setPumpTime(int pumpTime) {
        this.pumpTime = pumpTime;
    }

    public int getLaserTime() {
        return laserTime;
    }

    public void setLaserTime(int laserTime) {
        this.laserTime = laserTime;
    }
}
