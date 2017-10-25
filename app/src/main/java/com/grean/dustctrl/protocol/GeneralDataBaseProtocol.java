package com.grean.dustctrl.protocol;

import java.util.ArrayList;

/**
 * 网络通讯，操作数据库及配置文件接口
 * Created by weifeng on 2017/9/21.
 */

public interface GeneralDataBaseProtocol {
    boolean exportData2File(long start, long  end,ExportDataProcessListener listener);
    GeneralHistoryDataFormat getHourData(long dateStart);
    ArrayList<String> getDayLog(long endDate);
    GeneralHistoryDataFormat getData(long start,long end);

    /**
     * 获取上一次分钟数据时间戳
     * @return
     */
    long getLastMinDate();

    /**
     * 获取下一次分钟数据时间戳
     * @return
     */
    long getNextMinDate();

    /**
     * 设置分钟数据发送间隔
     * @param min
     */
    void setMinDataInterval(long min);

    long getLastHourDate();

    long getNextHourDate();

    long calcNextHourDate(long now);

    /**
     * 计算下一次时间
     * @return
     */
    long calcNextMinDate(long now);

    void loadMinDate();
}
