package com.grean.dustctrl.device;

import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.process.NotifyMainFragment;
import com.grean.dustctrl.process.NotifySystemLog;
import com.grean.dustctrl.protocol.GeneralInfoProtocol;


/**
 * Created by weifeng on 2020/3/2.
 */

public interface DustMeterControl {
    int Inquire = 1,
        DustCpm =2,
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
        DustMeterRelay = 13,
        DustMeterMotorForward = 14,
        DustMeterMotorBackward = 15,
        DustMeterMotorStop = 16,
        DustMeterMotorSetting = 17,
        DustMeterCalibrationState = 19,
        DustMeterOther = 99,
        Other=9999;
    /**
     * 发送查询命令
     */
    void inquire();

    /**
     * 触发报警继电器
     * @param key =true 闭合继电器 =off 打开继电器
     */
    void setAlarmRelay(boolean key);

    /**
     * 查询粉尘仪工作时间
     */
    void inquireDustMeterWorkedTime();

    void setDo(int num,boolean key);

    void setDustMeterRun(boolean key);

    /**
     * 设置步进电机运动状态
     * @param fun
     */
    void setMotor(int fun);

    /**
     * 校准流程
     * @param infoProtocol 局域网通讯接口
     * @param systemLog 日志通讯接口
     * @param dialogInfo 对话框通讯接口
     * @param notifyMainFragment 实时数据界面通讯接口
     */
    void calibrationDustMeter(GeneralInfoProtocol infoProtocol, NotifySystemLog systemLog,
                              NotifyProcessDialogInfo dialogInfo, NotifyMainFragment notifyMainFragment);

}
