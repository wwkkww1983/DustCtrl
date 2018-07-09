package com.grean.dustctrl.process;

import android.util.Log;

/**
 * 传感器数据结构
 * Created by Administrator on 2017/8/28.
 */

public class SensorData {
    private static final String tag = "SensorData";
    private float dust;//粉尘仪计算值
    private float value;//粉尘仪原始值
    private float paraK;//粉尘仪K值
    private float paraB;//
    private float noise,airTemperature,airHumidity,airPressure,windForce,windDirection;
    private boolean acIn,batteryLow,calPos,measurePos;
    private boolean[] ctrlDo = new boolean[5];
    private float hiTemp,loTemp,hiHumidity,loHumidity,pipeTemp;
    private int heatPwm,motorState,motorRounds,motorTime;
    private float hiDewPoint,loDewPoint;

    public SensorData(){

    }

    public float getNoise() {
        return noise;
    }

    public void setNoise(float noise) {
        this.noise = noise;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        dust = paraK * value+paraB;
        this.value = value;
    }

    public float getParaK() {
        return paraK;
    }

    public void setParaK(float paraK) {
        this.paraK = paraK;
    }

    public float getParaB() {
        return paraB;
    }

    public void setParaB(float paraB) {
        this.paraB = paraB;
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
        return hiHumidity;//采样管目标温度
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

    public boolean isCalPos() {
        return calPos;
    }

    public void setCalPos(boolean calPos) {
        this.calPos = calPos;
    }

    public boolean isMeasurePos() {
        return measurePos;
    }

    public void setMeasurePos(boolean measurePos) {
        this.measurePos = measurePos;
    }

    public float getPipeTemp() {
        return pipeTemp;
    }

    public void setPipeTemp(float pipeTemp) {
        this.pipeTemp = pipeTemp;
    }

    public void setCtrlDo(int index, boolean value) {
        if (index < ctrlDo.length) {
            this.ctrlDo[index] = value;
        }
    }

    public float getDust() {

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

    public float calcHiDewPoint(){
        if(hiTemp >= 0f){
            hiDewPoint = (float) (243.12f*(Math.log(airHumidity/100)+17.62f*airTemperature/(243.12+airTemperature))/(17.62 - Math.log(airHumidity/100)-17.62*airTemperature/(243.12+airTemperature)));
        }else{
            hiDewPoint = (float) (272.62f*(Math.log(airHumidity/100)+22.46f*airTemperature/(272.62+airTemperature))/(22.46 - Math.log(airHumidity/100)-22.46*airTemperature/(272.62+airTemperature)));
        }
        if(hiDewPoint != hiDewPoint){
            hiDewPoint = 99999.9f;
        }
        return hiDewPoint;
    }

    public float calcLoDewPoint(){
        if(loTemp >= 0f){
            loDewPoint = (float) (243.12f*(Math.log(loHumidity/100)+17.62f*loTemp/(243.12+loTemp))/(17.62 - Math.log(loHumidity/100)-17.62*loTemp/(243.12+loTemp)));
        }else{
            loDewPoint = (float) (272.62f*(Math.log(loHumidity/100)+22.46f*loTemp/(272.62+loTemp))/(22.46 - Math.log(loHumidity/100)-22.46*loTemp/(272.62+loTemp)));
        }
        if(loDewPoint != loDewPoint){//判断 Nan
            //Log.d(tag,"loDew error");
            loDewPoint = 99999.9f;
        }
        //Log.d(tag,"loDew"+String.valueOf(loDewPoint));
        return loDewPoint;
    }

    public float getHiDewPoint() {
        return hiDewPoint;
    }

    public float getLoDewPoint() {
        return loDewPoint;
    }
}
