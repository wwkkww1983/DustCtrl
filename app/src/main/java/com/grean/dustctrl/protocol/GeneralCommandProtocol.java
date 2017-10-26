package com.grean.dustctrl.protocol;

/**
 * Created by weifeng on 2017/10/24.
 */

public interface GeneralCommandProtocol {
    void setMnCode(String string);
    void setPassWord(String stirng);
    boolean handleString(String string);
    void executeProtocol(GeneralReturnProtocol returnProtocol,TcpClientCallBack callBack,GeneralInfoProtocol infoProtocol);
    boolean checkRecString(byte[] rec,int count);
}
