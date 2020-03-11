package com.grean.dustctrl.device;

import android.util.Log;


import com.grean.dustctrl.ComReceiveProtocol;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.process.NotifyMainFragment;
import com.grean.dustctrl.process.NotifySystemLog;
import com.grean.dustctrl.process.SensorData;
import com.grean.dustctrl.protocol.GeneralInfoProtocol;
import com.tools;

import java.util.Observable;

/**
 * Created by weifeng on 2020/3/2.
 */

public class DustMeterSibataLd8 implements ComReceiveProtocol,DustMeterControl{
    private static final String tag=  "DustMeterSibataLd8";
    private SensorData data;
    private SerialCommunicationController com;
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

    private static final byte[] cmdAutoCalValveOn={ 0x55,0x06,0x10,0x01,0x00,0x01,0x10, (byte) 0xDE};
    private static final byte[] cmdAutoCalValveOff={ 0x55,0x06,0x10,0x02,0x00,0x01, (byte) 0xE0, (byte) 0xDE};

    public DustMeterSibataLd8 (SensorData data,SerialCommunicationController com){
        this.data = data;
        this.com = com;
        this.com.setComReceiveProtocol(this);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {
        if (tools.checkFrameWithAddr(rec,size, (byte) 0x55)){
            switch (state){
                case Inquire:
                    if (rec[2]==0x3e) {
                        for(int i=0;i<5;i++) {//开关量输出
                            if (rec[4+i*2] == 0x00) {
                                data.setCtrlDo(i, false);
                            } else {
                                data.setCtrlDo(i, true);
                            }
                        }
                        data.setAirTemperature(tools.getFloat(rec,16));//大气温度
                        data.setAirHumidity(tools.getFloat(rec,20));//大气湿度
                        //21~24 备用
                        data.setAirPressure(tools.getFloat(rec,28));//大气压
                        data.setWindForce(tools.getFloat(rec,32));//风速
                        data.setWindDirection(tools.getFloat(rec,36));//风向
                        if(rec[37]==0x00){//自动校准滑块，限位开关。限位开关按压时为true
                            data.setCalPos(false);
                        }else{
                            data.setCalPos(true);
                        }

                        if(rec[38]==0x00){//外接电源输入，当外接电源时为true
                            data.setAcIn(false);
                        }else{
                            data.setAcIn(true);
                        }

                        if(rec[39]==0x00){//自动校准滑块，测量位置检测开关，当未处于检测位置时为true
                            data.setMeasurePos(false);
                        }else{
                            data.setMeasurePos(true);
                        }

                        if(rec[40]==0x00){//电池电压，电池电压高为true
                            data.setBatteryLow(true);
                        }else{
                            data.setBatteryLow(false);
                        }
                        if(tools.getFloat(rec,44)>999){
                            data.setHiTemp(999);
                        }else {
                            data.setHiTemp(tools.getFloat(rec, 44));//采样管温度
                        }
                        data.setLoTemp(tools.getFloat(rec,48));//采样管出口温度
                        data.setHiHumidity(tools.getFloat(rec,52));//采样管目标温度
                        data.setPipeTemp(tools.getFloat(rec,52));//同上
                        data.setLoHumidity(tools.getFloat(rec,56));//采样管出口湿度
                        data.setHeatPwm(tools.byte2int(rec,57));//加热pwm系数2~1000 数字越高加热功率越大
                        data.setMotorState(tools.byte2int(rec,59));//电机状态：停止；正转；反转
                        //data.setMotorRounds(tools.byte2int(rec,61));//当前步进步数 200step/round
                        //data.setMotorTime(tools.byte2int(rec,63));//步进电机步进时间
                        data.setInnerTemp(data.getLoTemp());
                    }
                    break;
                default:

                    break;
            }
        }

        if (tools.checkFrameWithAddr(rec,size,(byte)0xdd)){
            switch (state){
                case DustCpm:
                    int intDust = tools.byte2int(rec,3);
                    data.setValue(intDust);
                    break;
                case DustMeterPumpTime:
                    int intTime = tools.byte2int(rec,3);
                    data.setPumpTime(intTime);
                    Log.d(tag,"DustMeterPumpTime"+String.valueOf(intTime));
                    break;
                case DustMeterLaserTime:
                    int intLaser = tools.byte2int(rec,3);
                    data.setLaserTime(intLaser);
                    Log.d(tag,"DustMeterLaserTime"+String.valueOf(intLaser));
                    break;
                case DustMeterBgResult:
                    if (rec[4]==0x01){
                        data.setBgOk(true);
                    }else{
                        data.setBgOk(false);
                    }
                    Log.d(tag,"BgResult:"+String.valueOf(data.isBgOk()));
                    break;
                case DustMeterSpanResult:
                    if (rec[4]==0x01){
                        data.setSpanOk(true);
                    }else{
                        data.setSpanOk(false);
                    }
                    Log.d(tag,"SpanResult:"+String.valueOf(data.isSpanOk()));
                    break;
                default:

                    break;
            }
        }

    }

    @Override
    public void receiveAsyncProtocol(byte[] rec, int size) {

    }

    private void ctrlDo(int num,boolean key){
        final byte [] doNum = {0x00,0x01,0x02,0x03,0x04,0x05};
        byte [] cmd = {0x55,0x06,0x10,0x02,0x00,0x02,0x0d,0x0a};
        if (key){
            cmd[3] = 0x01;
        }
        if(num == 1) {//控制球阀
            if(key){
                com.send(cmdAutoCalValveOn,DustMeterControl.DustMeterRelay);
            }else{
                com.send(cmdAutoCalValveOff,DustMeterControl.DustMeterRelay);
            }
        }else if (num <= 5){
            //Log.d(tag,"单控继电器");
            cmd[5] = doNum[num];
            tools.addCrc16(cmd,0,6);
            com.send(cmd,DustMeterControl.DustMeterRelay);
        }
    }

    @Override
    public void inquire() {
        com.send(cmdInquire,DustMeterControl.Inquire);//查询主板参数
        com.send(cmdDustMeterCpm,DustMeterControl.DustCpm);//查询粉尘仪瞬时值

    }

    private void setMotorSetting(int rounds,int time,int fun){
        byte[] cmdMotorRounds = {0x55,0x06,0x10,0x03,0x00,0x0a,0x0d,0x0a},cmdMotorTime = {0x55,0x06,0x10,0x04,0x00, (byte) 0xa0,0x0d,0x0a},buff=new byte[2];//,cmdMotorState = {0x55,0x06,0x10,0x05,0x00,0x00,0x0d,0x0a}
        buff = tools.int2byte(rounds);
        cmdMotorRounds[4] = buff[0];
        cmdMotorRounds[5] = buff[1];
        tools.addCrc16(cmdMotorRounds,0,6);
        com.send(cmdMotorRounds,DustMeterMotorSetting);
        Log.d(tag,"Send"+tools.bytesToHexString(cmdMotorRounds,cmdMotorRounds.length));
        buff = tools.int2byte(time);
        cmdMotorTime[4] = buff[0];
        cmdMotorTime[5] = buff[1];
        tools.addCrc16(cmdMotorTime,0,6);
        com.send(cmdMotorTime,DustMeterMotorSetting);
        Log.d(tag,"Send"+tools.bytesToHexString(cmdMotorTime,cmdMotorTime.length));
        byte[] cmdMotorState = {0x55,0x06,0x10,0x05,0x00,0x00,0x0d,0x0a};

        switch (fun){
            case DustMeterMotorStop:
                cmdMotorState[5] = 0x00;
                tools.addCrc16(cmdMotorState,0,6);
                com.send(cmdMotorState,DustMeterMotorStop);
                break;
            case DustMeterMotorForward:
                cmdMotorState[5] = 0x01;
                tools.addCrc16(cmdMotorState,0,6);
                com.send(cmdMotorState,DustMeterMotorForward);
                break;
            case DustMeterMotorBackward:
                cmdMotorState[5] = 0x02;
                tools.addCrc16(cmdMotorState,0,6);
                com.send(cmdMotorState,DustMeterMotorBackward);
                break;
            default:
                break;
        }
    }

    private void setMotorSetting(int fun){
        byte[] cmdMotorRounds = {0x55,0x06,0x10,0x03,0x00,0x0a,0x0d,0x0a},cmdMotorTime = {0x55,0x06,0x10,0x04,0x00, (byte) 0xa0,0x0d,0x0a},buff=new byte[2];//,cmdMotorState = {0x55,0x06,0x10,0x05,0x00,0x00,0x0d,0x0a}
        buff = tools.int2byte(data.getMotorRounds());
        cmdMotorRounds[4] = buff[0];
        cmdMotorRounds[5] = buff[1];
        tools.addCrc16(cmdMotorRounds,0,6);
        com.send(cmdMotorRounds,DustMeterMotorSetting);
        Log.d(tag,"Send"+tools.bytesToHexString(cmdMotorRounds,cmdMotorRounds.length));
        buff = tools.int2byte(data.getMotorTime());
        cmdMotorTime[4] = buff[0];
        cmdMotorTime[5] = buff[1];
        tools.addCrc16(cmdMotorTime,0,6);
        com.send(cmdMotorTime,DustMeterMotorSetting);
        Log.d(tag,"Send"+tools.bytesToHexString(cmdMotorTime,cmdMotorTime.length));
        byte[] cmdMotorState = {0x55,0x06,0x10,0x05,0x00,0x00,0x0d,0x0a};

        switch (fun){
            case DustMeterMotorStop:
                cmdMotorState[5] = 0x00;
                tools.addCrc16(cmdMotorState,0,6);
                //cmdMotorState[5] = 0x00;
                com.send(cmdMotorState,DustMeterMotorStop);
                break;
            case DustMeterMotorForward:
                cmdMotorState[5] = 0x01;
                tools.addCrc16(cmdMotorState,0,6);
                //cmdMotorState[5] = 0x01;
                com.send(cmdMotorState,DustMeterMotorForward);
                break;
            case DustMeterMotorBackward:
                cmdMotorState[5] = 0x02;
                tools.addCrc16(cmdMotorState,0,6);
                //cmdMotorState[5] = 0x02;
                com.send(cmdMotorState,DustMeterMotorBackward);
                break;
            default:
                break;
        }
        //tools.addCrc16(cmdMotorState,0,6);
        //addSendBuff(cmdMotorState,Other);
        Log.d(tag,"Send"+tools.bytesToHexString(cmdMotorState,cmdMotorState.length));
    }

    @Override
    public void setAlarmRelay(boolean key) {
        ctrlDo(3,key);
    }

    @Override
    public void inquireDustMeterWorkedTime() {
        com.send(cmdDustMeterPumpTime,DustMeterControl.DustMeterPumpTime);
        com.send(cmdDustMeterLaserTime,DustMeterControl.DustMeterLaserTime);
    }

    @Override
    public void setDo(int num, boolean key) {
        ctrlDo(num,key);
    }

    @Override
    public void setDustMeterRun(boolean key) {
        if(key){
            com.send(cmdRunDustMeter,DustMeterOther);
        }else{
            com.send(cmdStopDustMeter,DustMeterOther);
        }
    }

    @Override
    public void setMotor(int fun) {
        setMotorSetting(fun);
    }

    @Override
    public void calibrationDustMeter(GeneralInfoProtocol infoProtocol,
                                     NotifySystemLog systemLog, NotifyProcessDialogInfo dialogInfo,
                                     NotifyMainFragment notifyMainFragment) {
        systemLog.writeLog("开始校准");
        notifyMainFragment.sendMainFragmentString("停止测量,开始校准");

        infoProtocol.notifySystemState("停止测量，开始校准");
        infoProtocol.setDustCalMeterProcess(2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ctrlDo(1,true);

        com.send(cmdStopDustMeter,DustMeterControl.DustMeterStop);
        for(int i=0;i<10;i++) {
            infoProtocol.setDustCalMeterProcess(4+i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if (dialogInfo!=null) {
            dialogInfo.showInfo("本底校准...");
        }
        notifyMainFragment.sendMainFragmentString("正在校零");
        infoProtocol.notifySystemState("正在校零");
        infoProtocol.setDustCalMeterProcess(15);
        com.send(cmdDustMeterBgStart,DustMeterControl.DustMeterBgStart);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<30;i++){
            infoProtocol.setDustCalMeterProcess(15+i);
            try {
                Thread.sleep(3300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        infoProtocol.setDustCalMeterProcess(45);
        com.send(cmdDustMeterBgResult,DustMeterControl.DustMeterBgResult);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<4;i++){
            infoProtocol.setDustCalMeterProcess(46+i);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        String zeroResultString;
        if (data.isBgOk()){
            systemLog.writeLog("校零成功");
            notifyMainFragment.sendMainFragmentString("校零成功");
            zeroResultString = "校零成功";
            infoProtocol.notifySystemState("校零成功");
        }else{
            systemLog.writeLog("校零失败");
            notifyMainFragment.sendMainFragmentString("校零失败");
            zeroResultString = "校零失败";
        }
        infoProtocol.notifySystemState(zeroResultString);
        infoProtocol.setDustCalMeterProcess(50);

        com.send(cmdDustMeterBgEnd,DustMeterBgEnd);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setMotorSetting(DustMeterMotorForward);

        if (dialogInfo!=null) {
            dialogInfo.showInfo("量程校准...");
        }
        notifyMainFragment.sendMainFragmentString("正在校跨");
        int motorTime = data.getMotorTime();
        if(motorTime <= 0){
            motorTime = 1800;
        }

        try {
            Thread.sleep((motorTime+200)*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        infoProtocol.setDustCalMeterProcess(55);

        infoProtocol.notifySystemState(zeroResultString+",正在校跨");
        com.send(cmdDustMeterSpanStart,DustMeterSpanStart);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<30;i++){
            infoProtocol.setDustCalMeterProcess(55+i);
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        infoProtocol.setDustCalMeterProcess(85);
        com.send(cmdDustMeterSpanResult,DustMeterSpanResult);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String spanResultString;
        if (data.isSpanOk()){
            systemLog.writeLog("校跨成功");
            notifyMainFragment.sendMainFragmentString("校跨成功");
            spanResultString = "校跨成功";
        }else{
            systemLog.writeLog("校跨失败");
            notifyMainFragment.sendMainFragmentString("校跨失败");
            spanResultString = "校跨失败";
        }
        infoProtocol.notifySystemState(zeroResultString+","+spanResultString);
        infoProtocol.setDustMeterResult(data.isBgOk(),data.isSpanOk());
        infoProtocol.setDustCalMeterProcess(90);
        com.send(cmdDustMeterSpanEnd,DustMeterSpanEnd);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(dialogInfo!=null) {
            dialogInfo.showInfo(zeroResultString+","+spanResultString+",结束校准...");
        }
        // sendMainFragmentString("结束校准");
        ctrlDo(1,false);
        infoProtocol.setDustCalMeterProcess(95);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setMotorSetting(DustMeterMotorBackward);//撤回转遮光板
        try {
            Thread.sleep((motorTime + 200)*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        com.send(cmdInquire,Inquire);
        systemLog.writeLog("校准结束");
        infoProtocol.setDustCalMeterProcess(100);
        infoProtocol.notifySystemState("校准结束");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        systemLog.writeLog( "散光板:限位"+String.valueOf(data.isCalPos())
                +";测量位置"+String.valueOf(data.isMeasurePos()));
        //如测量位置有误则继续移动散光板
        if (data.isMeasurePos()) {
            for (int i = 0; i < 5; i++) {
                systemLog.writeLog("散光板位置未回至测量位置，启动第"
                        +String.valueOf(i+1)+"次回拨");
                setMotorSetting(20,200,DustMeterMotorBackward);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                com.send(cmdInquire,Inquire);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (data.isMeasurePos()) {
                    systemLog.writeLog("第"+String.valueOf(i+1)+"次回拨失败");
                } else {
                    systemLog.writeLog("散光板回至测量位置");
                    break;
                }
            }
        }
        com.send(cmdRunDustMeter,DustMeterRun);

    }
}
