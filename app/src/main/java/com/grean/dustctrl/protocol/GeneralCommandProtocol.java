package com.grean.dustctrl.protocol;

/**
 * HJ_T212的反控方法
 * Created by weifeng on 2017/10/24.
 */

public interface GeneralCommandProtocol {
    void setMnCode(String string);
    void setPassWord(String string);
    boolean handleString(String string);
    void executeProtocol(GeneralReturnProtocol returnProtocol,TcpClientCallBack callBack,GeneralInfoProtocol infoProtocol);
    boolean checkRecString(byte[] rec,int count);
}
