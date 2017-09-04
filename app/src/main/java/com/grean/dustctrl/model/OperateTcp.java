package com.grean.dustctrl.model;

import com.grean.dustctrl.SocketClientCtrl;
import com.grean.dustctrl.SocketTask;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.presenter.NotifyOperateInfo;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;

/**
 * Created by Administrator on 2017/9/4.
 */

public class OperateTcp {
    NotifyProcessDialogInfo notifyProcessDialogInfo;
    private NotifyOperateInfo info;

    public OperateTcp(NotifyOperateInfo info){
        this.info= info;
    }

    public void setTcpSocketClient(String ip,int port,NotifyProcessDialogInfo notifyProcessDialogInfo){
        myApplication.getInstance().saveConfig("ServerIp",ip);
        myApplication.getInstance().saveConfig("ServerPort",port);
        this.notifyProcessDialogInfo = notifyProcessDialogInfo;
        SocketTask.getInstance().resetSocketClient(ip,port,info,notifyProcessDialogInfo);

    }
}
