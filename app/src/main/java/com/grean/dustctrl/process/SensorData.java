package com.grean.dustctrl.process;

/**
 * 传感器数据结构
 * Created by Administrator on 2017/8/28.
 */

public class SensorData {
    private float dust;//粉尘仪计算值
    private float value;//粉尘仪原始值
    private float paraK;//粉尘仪K值
    private float airTemperature;
    private float airHumidity;
    private float airPressure;
    private float windForce;
    private float windDirection;
    private boolean acIn;
    private boolean batteryLow;
    private boolean[] ctrlDo = new boolean[5];
    private float hiTemp;
    private float loTemp;
    private float hiHumidity;
    private float loHumidity;
    private int heatPwm;
    private int motorState;
    private int motorRounds;
    private int motorTime;

    public SensorData(){

    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getParaK() {
        return paraK;
    }

    public void setParaK(float paraK) {
        this.paraK = paraK;
    }

    public boolean[] getCtrlDo() {
        return ctrlDo;
    }

    public void setCtrlDo(boolean[] ctrlDo) {
        this.ctrlDo = ctrlDo;
    }

    public float getHiTemp() {
        return hiTemp;
    }

    public void setHiTemp(float hiTemp) {
        this.hiTemp = hiTemp;
    }

    public float getLoTemp() {
        return loTemp;
    }

    public void setLoTemp(float loTemp) {
        this.loTemp = loTemp;
    }

    public float getHiHumidity() {
        return hiHumidity;
    }

    public void setHiHumidity(float hiHumidity) {
        this.hiHumidity = hiHumidity;
    }

    public float getLoHumidity() {
        return loHumidity;
    }

    public void setLoHumidity(float loHumidity) {
        this.loHumidity = loHumidity;
    }

    public int getHeatPwm() {
        return heatPwm;
    }

    public void setHeatPwm(int heatPwm) {
        this.heatPwm = heatPwm;
    }

    public int getMotorState() {
        return motorState;
    }

    public void setMotorState(int motorState) {
        this.motorState = motorState;
    }

    public int getMotorRounds() {
        return motorRounds;
    }

    public void setMotorRounds(int motorRounds) {
        this.motorRounds = motorRounds;
    }

    public int getMotorTime() {
        return motorTime;
    }

    public void setMotorTime(int motorTime) {
        this.motorTime = motorTime;
    }

    public boolean getCtrlDo(int index) {
        if(index < ctrlDo.length) {
            return ctrlDo[index];
        }else {
            return false;
        }
    }

    public void setCtrlDo(int index,boolean value) {
        if (index < ctrlDo.length) {
            this.ctrlDo[index] = value;
        }
    }

    public float getDust() {
        dust = paraK * value;
        return dust;
    }

    public float getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(float airTemperature) {
        this.airTemperature = airTemperature;
    }

    public float getAirHumidity() {
        return airHumidity;
    }

    public void setAirHumidity(float airHumidity) {
        this.airHumidity = airHumidity;
    }

    public float getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(float airPressure) {
        this.airPressure = airPressure;
    }

    public float getWindForce() {
        return windForce;
    }

    public void setWindForce(float windForce) {
        this.windForce = windForce;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(float windDirection) {
        this.windDirection = windDirection;
    }

    public boolean isAcIn() {
        return acIn;
    }

    public void setAcIn(boolean acIn) {
        this.acIn = acIn;
    }

    public boolean isBatteryLow() {
        return batteryLow;
    }

    public void setBatteryLow(boolean batteryLow) {
        this.batteryLow = batteryLow;
    }
}
