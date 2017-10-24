package com.grean.dustctrl.protocol;

/**
 * Created by weifeng on 2017/10/24.
 */

public interface GeneralReturnProtocol {
    String getRealTimeData();
    String getMinData(String qn,long begin,long end);
    String getHourData(String qn,long begin,long end);
    String getSystemResponse(String qn);
    String getSystemOk(String qn);
}
