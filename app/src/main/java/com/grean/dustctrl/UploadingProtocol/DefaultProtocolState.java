package com.grean.dustctrl.UploadingProtocol;

import com.grean.dustctrl.process.SensorData;
import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 默认协议
 * Created by weifeng on 2018/6/28.
 */

public class DefaultProtocolState implements ProtocolState{
    private UploadingConfigFormat format;
    private ProtocolCommand command;
    private DefaultFrameBuilder frameBuilder;
    private long lastUploadMinDate;

    public DefaultProtocolState(ProtocolCommand command){
        this.command = command;
        frameBuilder = new DefaultFrameBuilder();
    }

    @Override
    public void handleReceiveBuff(byte[] buff, int length) {

    }

    @Override
    public void uploadSystemTime(long now,long lastUploadMinDate,long lastUploadHourDate) {
        this.lastUploadMinDate = lastUploadMinDate;
    }

    @Override
    public void uploadMinDate(long now, long date) {

        if(command.isConnected()) {
            if(lastUploadMinDate <= date) {
                GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getData(lastUploadMinDate ,date);
                lastUploadMinDate = date;
                for(int i=0;i<dataFormat.getSize();i++) {
                    frameBuilder.cleanContent();
                    long minDate = dataFormat.getDate(i);
                    ArrayList<Float> item = dataFormat.getItem(i);
                    frameBuilder.addContentField("DataTime", tools.timeStamp2TcpStringWithoutMs(minDate));
                    Iterator it = format.getFactorMap().entrySet().iterator();
                    while (it.hasNext()){
                        Map.Entry entry = (Map.Entry) it.next();
                        String key = (String) entry.getKey();
                        Integer val  = (Integer) entry.getValue();
                        frameBuilder.addContentFactor(key,tools.float2String3(item.get(val)),"N");
                    }
                    command.executeSendTask(frameBuilder.setSt("31").setCn("2061").setPw(format.getPassword())
                            .setMn(format.getMnCode()).insertOneFrame().getBytes());
                }
            }
        }
    }

    @Override
    public void uploadSecondDate(long now) {

    }

    @Override
    public void uploadHourDate(long now, long date) {

    }

    @Override
    public void setConfig(UploadingConfigFormat format) {
        this.format = format;
        format.addFactor("103-Cou",GeneralHistoryDataFormat.Dust);
        format.addFactor("104-Cou",GeneralHistoryDataFormat.Noise);
        format.addFactor("105-Cou",GeneralHistoryDataFormat.Humidity);
        format.addFactor("106-Cou",GeneralHistoryDataFormat.Temperature);
        format.addFactor("107-Cou",GeneralHistoryDataFormat.Pressure);
        format.addFactor("108-Cou",GeneralHistoryDataFormat.WindForce);
        format.addFactor("109-Cou",GeneralHistoryDataFormat.WindDirection);
    }

    @Override
    public void setRealTimeData(SensorData data) {

    }
}
