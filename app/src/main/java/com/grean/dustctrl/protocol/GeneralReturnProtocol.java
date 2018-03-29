package com.grean.dustctrl.protocol;

import java.util.List;

/**
 * Created by weifeng on 2017/10/24.
 */

public interface GeneralReturnProtocol {
    String getRealTimeData(String qn);
    List<String> getMinData(String qn, long begin, long end);
    List<String> getHourData(String qn,long begin,long end);
    String getSystemResponse(String qn);
    String getSystemOk(String qn);
}
