package com.grean.dustctrl.protocol;

/**
 * Created by weifeng on 2017/9/5.
 */

public interface GeneralServerProtocol {
    byte[] handleProtocol(byte[] rec,int count);
}
