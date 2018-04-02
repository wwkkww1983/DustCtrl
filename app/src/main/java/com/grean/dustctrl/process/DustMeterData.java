package com.grean.dustctrl.process;

/**
 * Created by weifeng on 2018/4/2.
 */

public class DustMeterData {
    private static final String tag = "SensorData";
    private float dust;//粉尘仪计算值
    private float value;//粉尘仪原始值
    private float paraK;//粉尘仪K值
    private float paraB;//
    private boolean blockPos;
    private float loTemp,loHumidity,pipeTemp;
    private int heatPwm,motorState,motorRounds,motorTime;
    private float loDewPoint;


    public void setValue(float value) {
        dust = value*paraK+paraB;
        this.value = value;
    }

    public void setParaK(float paraK) {
        this.paraK = paraK;
    }

    public void setParaB(float paraB) {
        this.paraB = paraB;
    }

    public void setBlockPos(boolean blockPos) {
        this.blockPos = blockPos;
    }

    public void setLoTemp(float loTemp) {
        this.loTemp = loTemp;
    }

    public void setLoHumidity(float loHumidity) {
        this.loHumidity = loHumidity;
    }

    public void setPipeTemp(float pipeTemp) {
        this.pipeTemp = pipeTemp;
    }

    public void setHeatPwm(int heatPwm) {
        this.heatPwm = heatPwm;
    }

    public void setMotorState(int motorState) {
        this.motorState = motorState;
    }

    public void setMotorRounds(int motorRounds) {
        this.motorRounds = motorRounds;
    }

    public void setMotorTime(int motorTime) {
        this.motorTime = motorTime;
    }

    public void setLoDewPoint(float loDewPoint) {
        this.loDewPoint = loDewPoint;
    }

    public float calcLoDewPoint(){
        if(loTemp >= 0f){
            loDewPoint = (float) (243.12f*(Math.log(loHumidity/100)+17.62f*loTemp/(243.12+loTemp))/(17.62 - Math.log(loHumidity/100)-17.62*loTemp/(243.12+loTemp)));
        }else{
            loDewPoint = (float) (272.62f*(Math.log(loHumidity/100)+22.46f*loTemp/(272.62+loTemp))/(22.46 - Math.log(loHumidity/100)-22.46*loTemp/(272.62+loTemp)));
        }
        if(loDewPoint != loDewPoint){//判断 Nan
            loDewPoint = 99999.9f;
        }
        return loDewPoint;
    }
}
