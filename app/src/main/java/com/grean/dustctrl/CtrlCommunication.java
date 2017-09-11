package com.grean.dustctrl;

import android.util.Log;
import android.widget.Switch;

import com.SerialCommunication;
import com.grean.dustctrl.process.DustMeterInfo;
import com.grean.dustctrl.process.SensorData;
import com.tools;


/**
 * 处理与控制板通讯
 * Created by Administrator on 2017/8/24.
 */

public class CtrlCommunication extends SerialCommunication{
    private static final String tag="CtrlCommunication";
    private static CtrlCommunication instance = new CtrlCommunication();
    private static final byte[] cmdInquire = {0x55,0x03,0x20,0x01,0x00,0x1f,0x53, (byte) 0xd6};
    private static final byte[] cmdDustMeterCpm = {(byte) 0xdd,0x03,0x00,0x01,0x00,0x01, (byte) 0xc6, (byte) 0x96};
    private static final byte[] cmdStopDustMeter = {(byte) 0xdd,0x06,0x00,0x03,0x00,0x00,0x6a, (byte) 0x96};
    private static final byte[] cmdRunDustMeter = {(byte) 0xdd,0x06,0x00,0x03,0x00,0x01, (byte) 0xab,0x56};
    private static final byte[] cmdDustMeterPumpTime = {(byte) 0xdd,0x03,0x00,0x04,0x00,0x01, (byte) 0xd6, (byte) 0x97};
    private static final byte[] cmdDustMeterLaserTime = {(byte) 0xdd,0x03,0x00,0x05,0x00,0x01, (byte) 0x87,0x57};
    private static final byte[] cmdDustMeterBgStart = {(byte) 0xdd,0x06,0x00,0x06,0x00,0x01, (byte) 0xbb,0x57};
    private static final byte[] cmdDustMeterBgEnd={(byte) 0xdd,0x06,0x00,0x06,0x00,0x00,0x7a, (byte) 0x97};
    private static final byte[] cmdDustMeterBgResult={(byte) 0xdd,0x03,0x00,0x07,0x00,0x01,0x26, (byte) 0x97};
    private static final byte[] cmdDustMeterSpanStart={(byte) 0xdd,0x06,0x00,0x08,0x00,0x01, (byte) 0xda, (byte) 0x94};
    private static final byte[] cmdDustMeterSpanEnd={(byte) 0xdd,0x06,0x00,0x08,0x00,0x00,0x1b,0x54};
    private static final byte[] cmdDustMeterSpanResult={(byte) 0xdd,0x03,0x00,0x09,0x00,0x01,0x47,0x54};
    private static final byte[] cmdAirData={(byte) 0xe3,0x03,0x00,0x00,0x00,0x03,0x12,0x49};
    private static final byte[] cmdWindForce = {(byte) 0xe1,0x03,0x00,0x00,0x00,0x01, (byte) 0x92,0x6a};
    private static final byte[] cmdWindDirection = {(byte) 0xe2,0x03,0x00,0x00,0x00,0x01, (byte) 0x92,0x59};
    private SensorData data = new SensorData();
    private DustMeterInfo info = new DustMeterInfo();
    private int motorRounds,motorTime;
    private boolean dustMeterRun = true;
    public static final int  Inquire = 1,
        Dust = 2,
        DustMeterStop =3,
        DustMeterRun=4,
        DustMeterPumpTime=5,
        DustMeterLaserTime=6,
        DustMeterBgStart=7,
        DustMeterBgEnd=8,
        DustMeterBgResult=9,
        DustMeterSpanStart=10,
        DustMeterSpanEnd=11,
        DustMeterSpanResult=12,
        MotorStop=13,
        MotorForward = 14,
        MotorBackward = 15,
        AirParameter = 16,
        WindForce = 17,
        WindDirection=18,
        Other=0;
    private CtrlCommunication(){
        super(0,9600,0);
        //this.data = new SensorData();
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

    public void setMotorSetting(int fun){
        byte[] cmdMotorRounds = {0x55,0x06,0x10,0x03,0x00,0x0a,0x0d,0x0a},cmdMotorTime = {0x55,0x06,0x10,0x04,0x00, (byte) 0xa0,0x0d,0x0a},cmdMotorState = {0x55,0x06,0x10,0x05,0x00,0x00,0x0d,0x0a},buff=new byte[2];
        buff = tools.int2byte(motorRounds);
        cmdMotorRounds[4] = buff[0];
        cmdMotorRounds[5] = buff[1];
        tools.addCrc16(cmdMotorRounds,0,6);
        addSendBuff(cmdMotorRounds,Other);
        buff = tools.int2byte(motorTime);
        cmdMotorTime[4] = buff[0];
        cmdMotorTime[5] = buff[1];
        tools.addCrc16(cmdMotorTime,0,6);
        addSendBuff(cmdMotorTime,Other);
        switch (fun){
            case MotorStop:
                cmdMotorState[5] = 0x00;
                break;
            case MotorForward:
                cmdMotorState[5] = 0x01;
                break;
            case MotorBackward:
                cmdMotorState[5] = 0x02;
                break;
            default:
                break;
        }
        tools.addCrc16(cmdMotorState,0,6);
        addSendBuff(cmdMotorState,Other);
    }

    public static CtrlCommunication getInstance() {
        return instance;
    }

    public SensorData getData() {
        return data;
    }

    public DustMeterInfo getInfo() {
        return info;
    }

    public boolean isDustMeterRun() {
        return dustMeterRun;
    }

    public void setDustParaK(float k){
        if (data == null){
            data = new SensorData();
        }
        data.setParaK(k);
    }
    @Override
    protected boolean checkRecBuff() {
        return true;
    }

    @Override
    protected void communicationProtocol(byte[] rec, int size,int state) {

        if (checkFrameWithAddr(rec,size,(byte)0x55)){//主板
            //Log.d(tag,"board sync right check");
            switch (state){
                case Inquire:
                    if (rec[2]==0x3e) {
                        for(int i=0;i<5;i++) {
                            if (rec[3+i] == 0x00) {
                                data.setCtrlDo(i, false);
                            } else {
                                data.setCtrlDo(i, false);
                            }
                        }
                       /* data.setAirTemperature(tools.getFloat(rec,11));
                        data.setAirHumidity(tools.getFloat(rec,15));
                        data.setAirPressure(tools.getFloat(rec,23));
                        data.setWindForce(tools.getFloat(rec,27));
                        data.setWindDirection(tools.getFloat(rec,31));*/
                        if(rec[32]==0x00){
                            data.setAcIn(false);
                        }else{
                            data.setAcIn(true);
                        }

                        if(rec[33]==0x00){
                            data.setBatteryLow(false);
                        }else{
                            data.setBatteryLow(true);
                        }
                        data.setHiTemp(tools.getFloat(rec,37));
                        data.setLoTemp(tools.getFloat(rec,41));
                        data.setHiHumidity(tools.getFloat(rec,45));
                        data.setLoHumidity(tools.getFloat(rec,49));
                        data.setHeatPwm(tools.byte2int(rec,50));
                        data.setMotorState(tools.byte2int(rec,51));
                        data.setMotorRounds(tools.byte2int(rec,53));
                        data.setMotorTime(tools.byte2int(rec,55));
                    }
                    break;
                default:
                    break;

            }

        }else if (checkFrameWithAddr(rec,size,(byte)0xdd)){//粉尘仪
           // Log.d(tag,"dust sync right check");
            switch(state){
                case Dust:
                    int intDust = tools.byte2int(rec,3);
                    data.setValue(intDust);
                   // Log.d(tag,"value="+String.valueOf(data.getValue()));
                    break;
                case DustMeterPumpTime:
                    int intTime = tools.byte2int(rec,3);
                    info.setPumpTime(intTime);
                    Log.d(tag,"DustMeterPumpTime"+String.valueOf(intTime));
                    break;
                case DustMeterLaserTime:
                    int intLaser = tools.byte2int(rec,3);
                    info.setLaserTime(intLaser);
                    Log.d(tag,"DustMeterLaserTime"+String.valueOf(intLaser));
                    break;
                case DustMeterBgResult:
                    if (rec[4]==0x01){
                        info.setBgOk(true);
                    }else{
                        info.setBgOk(false);
                    }
                    Log.d(tag,"BgResult:"+String.valueOf(info.isBgOk()));
                    break;
                case DustMeterSpanResult:
                    if (rec[4]==0x01){
                        info.setSpanOk(true);
                    }else{
                        info.setSpanOk(false);
                    }
                    Log.d(tag,"SpanResult:"+String.valueOf(info.isSpanOk()));
                    break;
                default:
                    break;
            }

        }else if (checkFrameWithAddr(rec,size,(byte)0xe1)){//风速
            switch (state){
                case WindForce:
                    int intDate = tools.byte2int(rec,3);
                    float floatData = ((float)intDate)/10.0f;
                    data.setWindForce(floatData);
                    break;
                default:
                    break;
            }

        }else if(checkFrameWithAddr(rec,size,(byte)0xe2)){//风向
            switch (state){
                case WindDirection:
                    int intDate = tools.byte2int(rec,3);
                    float floatData = (float)intDate;
                    data.setWindDirection(floatData);
                    break;
                default:
                    break;
            }

        }else if(checkFrameWithAddr(rec,size,(byte)0xe3)){//温湿度大气压
            switch (state){
                case AirParameter:
                    int intDate = tools.byte2int(rec,3);
                    float floatData = ((float)intDate)/10.0f;
                    data.setAirPressure(floatData);
                    intDate = tools.byte2int(rec,5);
                    floatData = ((float)intDate)/10.0f;
                    data.setAirTemperature(floatData);
                    intDate = tools.byte2int(rec,7);
                    //Log.d(tag,"Humidity"+String.valueOf(intDate));
                    floatData = ((float)intDate)/10.0f;
                    data.setAirHumidity(floatData);
                    break;
                default:
                    break;
            }
        }else{
            Log.d(tag,"sync wrong check");
        }
    }

    @Override
    protected void asyncCommunicationProtocol(byte[] rec, int size) {
        if (checkFrameWithAddr(rec,size,(byte)0x55)){//主板
            Log.d(tag,"async right check");
        }else if (checkFrameWithAddr(rec,size,(byte)0xdd)){//粉尘仪
            Log.d(tag,"async right check");
        }else{
            Log.d(tag,"async wrong check");
        }

    }

    public void send(byte[] buff) {
        addSendBuff(buff,Other);
    }

    public void ctrlDo(int num,boolean key){
        final byte [] doNum = {0x00,0x01,0x02,0x03,0x04,0x05};
        byte [] cmd = {0x55,0x06,0x10,0x02,0x00,0x02,0x0d,0x0a};
        if (key){
            cmd[3] = 0x01;
        }
        if (num <= 5){
            cmd[5] = doNum[num];
            tools.addCrc16(cmd,0,6);
            addSendBuff(cmd,Other);
        }
    }

    /**
     * 发送已成套的命令
     * @param cmd
     */
    public void SendFrame(int cmd){
        switch (cmd){
            case Inquire:
                addSendBuff(cmdInquire,cmd);
            break;
            case Dust:
                addSendBuff(cmdDustMeterCpm,cmd);
                break;
            case DustMeterStop:
               addSendBuff(cmdStopDustMeter,cmd);
                dustMeterRun = false;
                break;
            case DustMeterRun:
                addSendBuff(cmdRunDustMeter,cmd);
                dustMeterRun = true;
                break;
            case DustMeterPumpTime:
                addSendBuff(cmdDustMeterPumpTime,cmd);
                break;
            case DustMeterLaserTime:
                addSendBuff(cmdDustMeterLaserTime,cmd);
                break;
            case DustMeterBgStart:
                addSendBuff(cmdDustMeterBgStart,cmd);
                break;
            case DustMeterBgEnd:
                addSendBuff(cmdDustMeterBgEnd,cmd);
                break;
            case DustMeterBgResult:
                addSendBuff(cmdDustMeterBgResult,cmd);
                break;
            case DustMeterSpanStart:
                addSendBuff(cmdDustMeterSpanStart,cmd);
                break;
            case DustMeterSpanEnd:
                addSendBuff(cmdDustMeterSpanEnd,cmd);
                break;
            case DustMeterSpanResult:
                addSendBuff(cmdDustMeterSpanResult,cmd);
                break;
            case AirParameter:
                addSendBuff(cmdAirData,cmd);
                break;
            case WindDirection:
                addSendBuff(cmdWindDirection,cmd);
                break;
            case WindForce:
                addSendBuff(cmdWindForce,cmd);
                break;
            case Other:
            default:
                break;
        }
    }

    private boolean checkFrame(byte[] buff,int size){
        if (buff[0]!=0x55){
            return false;
        }

        if(!((buff[1]==0x03)||(buff[1]==0x06))){
            return false;
        }


        if (tools.calcCrc16(buff,0,size)!=0x0000){
            return false;
        }

        return true;
    }

    private boolean checkFrameWithAddr(byte []buff,int size,byte addr){
        if (buff[0]!=addr){
            return false;
        }

        if(!((buff[1]==0x03)||(buff[1]==0x06))){
            return false;
        }


        if (tools.calcCrc16(buff,0,size)!=0x0000){
            return false;
        }

        return true;
    }
}
