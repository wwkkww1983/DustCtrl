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

/**
 * 朗亿机电 LPM1000 粉尘仪
 * Created by weifeng on 2020/3/4.
 */

public class DustMeterLyjdLpm1000 implements ComReceiveProtocol,DustMeterControl{
    private static final byte[] cmdInquireCpm = {0x01,0x03,0x00,0x00,0x00,0x0e, (byte) 0xc4,0x0e };
    private static final byte[] cmdInquireSample={0x01,0x03,0x01,0x60,0x00,0x0A, (byte) 0xC4,0x2F };
    private static final byte[] cmdCalibration = {0x01,0x06,0x00,0x1E,0x00,0x03, (byte) 0xA9, (byte) 0xCD};
    private static final byte[] cmdCalibrationState = {0x01,0x03,0x00, (byte) 0xE4,0x00,0x01, (byte) 0xC4,0x3D  };
    private static final String tag=  "DustMeterLyjdLpm1000";
    protected boolean measuring = true;
    protected SensorData data;
    private SerialCommunicationController com;
    public DustMeterLyjdLpm1000 (SerialCommunicationController com,SensorData data){
        this.data = data;
        this.com = com;
        this.com.setComReceiveProtocol(this);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {
        if (tools.checkFrameWithAddr(rec,size,(byte)0x01)) {
            float f;
            switch (state) {
                case Inquire:
                    f = tools.getFloat(rec,6);//环境湿度
                    data.setLoHumidity(f);
                    f = tools.getFloat(rec,10);//环境温度
                    data.setLoTemp(f);
                    f = tools.getFloat(rec,14);//加热管温度
                    data.setPipeTemp(f);
                    data.setHiHumidity(f);//主界面显示 加热管温度
                    f = tools.getFloat(rec,22);//加热管PWM
                    data.setHeatPwm((int) f);
                    //Log.d(tag,"inquire = "+String.valueOf(data.getLoHumidity())+";"+String.valueOf(data.getLoTemp()+";"+
                    //String.valueOf(data.getPipeTemp())+";"+String.valueOf(data.getHeatPwm())));
                    break;
                case DustCpm:
                    f = tools.getFloat(rec,6);//分钟值
                    data.setValue(f);
                    f = tools.getFloat(rec,14);//流量
                    data.setFlow(f);
                    f = tools.getFloat(rec,22);//仪器内部温度
                    data.setInnerTemp(f);
                    //Log.d(tag,"DustCpm="+tools.bytesToHexString(rec,size));
                    break;
                case DustMeterCalibrationState:
                    //Log.d(tag,"DustMeterCalibrationState="+tools.bytesToHexString(rec,size));
                    if(rec[4] == 0x00){//测量状态
                        measuring  =true;
                    }else{
                        measuring = false;
                    }
                    break;
                default:

                    break;
            }
        }
    }

    @Override
    public void receiveAsyncProtocol(byte[] rec, int size) {

    }

    @Override
    public void inquire() {
        com.send(cmdInquireSample,Inquire);
        com.send(cmdInquireCpm,DustCpm);
    }

    @Override
    public void setAlarmRelay(boolean key) {

    }

    @Override
    public void inquireDustMeterWorkedTime() {

    }

    @Override
    public void setDo(int num, boolean key) {

    }

    @Override
    public void setDustMeterRun(boolean key) {

    }

    @Override
    public void setMotor(int fun) {

    }

    @Override
    public void calibrationDustMeter(GeneralInfoProtocol infoProtocol, NotifySystemLog systemLog,
                                     NotifyProcessDialogInfo dialogInfo, NotifyMainFragment notifyMainFragment) {
        infoProtocol.notifySystemState("停止测量，开始校准");
        if (dialogInfo!=null) {
            dialogInfo.showInfo("停止测量，开始校准");
        }
        notifyMainFragment.sendMainFragmentString("停止测量,开始校准");
        infoProtocol.setDustCalMeterProcess(0);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        measuring = false;
        com.send(cmdCalibration,DustMeterSpanStart);
        infoProtocol.notifySystemState("正在校准");
        if (dialogInfo!=null) {
            dialogInfo.showInfo("正在校准...0%");
        }
        notifyMainFragment.sendMainFragmentString("正在校准");
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<60;i++){//预计4分钟 60%进度
            com.send(cmdCalibrationState,DustMeterCalibrationState);
            infoProtocol.setDustCalMeterProcess(i);
            if (dialogInfo!=null) {
                dialogInfo.showInfo("正在校准..."+String.valueOf(i)+"%");
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(measuring){
                break;
            }
        }

        //等待数据平稳160s 60~99%
        for(int i=0;i<40;i++){
            infoProtocol.setDustCalMeterProcess(i+60);
            if (dialogInfo!=null) {
                dialogInfo.showInfo("正在校准..."+String.valueOf(i+60)+"%");
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        infoProtocol.setDustCalMeterProcess(100);
        infoProtocol.notifySystemState("校准结束");
        if (dialogInfo!=null) {
            dialogInfo.showInfo("校准结束");
        }
        notifyMainFragment.sendMainFragmentString("校准结束");

    }
}
