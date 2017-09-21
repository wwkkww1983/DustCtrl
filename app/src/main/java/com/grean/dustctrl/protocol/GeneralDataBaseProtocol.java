package com.grean.dustctrl.protocol;

import java.util.ArrayList;

/**
 * Created by weifeng on 2017/9/21.
 */

public interface GeneralDataBaseProtocol {
    boolean exportData2File(long start, long  end,ExportDataProcessListener listener);
    GeneralHistoryDataFormat getHourData(long dateStart);
    ArrayList<String> getDayLog(long endDate);
}
