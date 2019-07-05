package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by weifeng on 2019/6/26.
 */

public class HJ212_HzProtocolState extends HJT212_2017ProtocolState{
    private GeneralHistoryDataFormat lastMinDataFormat = new GeneralHistoryDataFormat();
    private static String tag = "HJ212_HzProtocolState";
    private static long oneDayTimestamps = Long.valueOf(60*60*24*1000);
    private boolean realTimeDataEnable = true;
    private int flag;
    private Hjt212HzFrameBuilder frameBuilder;
    public HJ212_HzProtocolState(ProtocolCommand command) {
        super(command);
    }

    @Override
    protected void initConfig() {
        frameBuilder = new Hjt212HzFrameBuilder();
        RequestHandle cp = new HzCpRequestHandle();
        RequestHandle cnRh = new CnRequestHandle(cp);
        RequestHandle qn = new HzQnRequestHandle(cnRh);
        RequestHandle flag = new HzFlagRequestHandle(qn);
        RequestHandle pw = new PwRequestHandle(flag);
        RequestHandle mn = new MnRequestHandle(pw);
        rhStart = mn;
    }

    protected class HzQnRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public HzQnRequestHandle(RequestHandle rh){
            this.rh = rh;
        }
        @Override
        public void handleRequest(HashMap<String, String> map) {
            /*if(map.get("QN").equals(qnSend)){
                //Log.d(tag,"接收到返回帧");
                hasReceived = true;
            }else{
                qnReceived = map.get("QN");
            }*/
            rh.handleRequest(map);
        }
    }

    private class HzFlagRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public HzFlagRequestHandle(RequestHandle rh){
            this.rh = rh;
        }

        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("Flag")!=null){
                if(map.get("Flag").equals("3")||map.get("Flag").equals("2")
                        ||map.get("Flag").equals("1")||map.get("Flag").equals("0")) {
                    flag  =Integer.valueOf(map.get("Flag"));
                    this.rh.handleRequest(map);
                }
            }else{
                Log.d(tag,"Flag异常");
            }
        }
    }
    @Override
    public void setConfig(UploadingConfigFormat format) {
        format.addFactor("a34001", GeneralHistoryDataFormat.Dust);
        format.addFactor("a01001",GeneralHistoryDataFormat.Temperature);
        format.addFactor("a01002",GeneralHistoryDataFormat.Humidity);
        format.addFactor("a01006",GeneralHistoryDataFormat.Pressure);
        format.addFactor("a01007",GeneralHistoryDataFormat.WindForce);
        format.addFactor("a01008",GeneralHistoryDataFormat.WindDirection);
        this.format = format;
    }




    private class HzCpRequestHandle implements RequestHandle{

        private void handleParameter (int num,String string){

        }

        private void handleInteraction (int num,String string){

        }

        private void handleData (int num,String string){

        }

        private void handleControl(int num,String string){

        }
        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("CP")!=null) {
                //Log.d(tag, "处理内容:"+map.get("CP"));
                if(cn<2000){//参数命令
                    handleParameter(cn,map.get("CP"));
                }else if(cn<3000){//数据命令
                    handleData(cn,map.get("CP"));
                }else if(cn<4000){//控制命令
                    handleControl(cn,map.get("CP"));
                }else if(cn<10000){//交互命令
                    handleInteraction(cn,map.get("CP"));
                }else{//其他命令

                }
            }
        }
    }

    @Override
    protected boolean isCnAvailable(int num) {
        switch (num){
            case 1000:
            case 1011:
            case 1012:
            case 1021:
            case 1022:
            case 1031:
            case 1032:
            case 1041:
            case 1042:
            case 1061:
            case 1062:
            case 1072:
            case 2011:
            case 2012:
            case 2021:
            case 2022:
            case 2031:
            case 2041:
            case 2051:
            case 2061:
            case 2071:
            case 3011:
            case 3012:
            case 3013:
            case 3014:
            case 9013:
            case 9014:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void uploadSecondDate(long now) {
        if(command.isConnected()) {
            if (realTimeDataEnable) {
                frameBuilder.cleanContent();
                qnSend = tools.timeStamp2TcpString(now);

                frameBuilder.addContentField("DataTime", tools.timeStamp2TcpStringWithoutMs(now));
                if(realTimeData!=null){
                    frameBuilder.addContentFactor("a34001","Rtd",
                            tools.float2String3(realTimeData.getDust()),"N");
                    frameBuilder.addContentFactor("a01001","Rtd",
                            tools.float2String1(realTimeData.getAirTemperature()),"N");
                    frameBuilder.addContentFactor("a01002","Rtd",
                            tools.float2String1(realTimeData.getAirHumidity()),"N");
                    frameBuilder.addContentFactor("a01006","Rtd",
                            tools.float2String1(realTimeData.getAirPressure()),"N");
                    frameBuilder.addContentFactor("a01007","Rtd",
                            tools.float2String1(realTimeData.getWindForce()),"N");
                    frameBuilder.addContentFactor("a01008","Rtd",
                            tools.float2String0(realTimeData.getWindDirection()),"N");
                }
                command.executeSendTask(frameBuilder.setQn(qnSend).setSt("39").setCn("2011").setPw(format.getPassword())
                        .setMn(format.getMnCode()).insertOneFrame().getBytes());
            }
        }else{
            noResponseTimes++;
        }
        if(noResponseTimes > 5){
            noResponseTimes = 0;
            Log.d(tag,"无响应重连");
            //command.reconnect();
        }
    }

    @Override
    protected void sendMinData(String qn, long begin, long end, boolean response) {
        GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getData(begin, end);

        for (int i = 0; i < dataFormat.getSize(); i++) {
            frameBuilder.cleanContent();
            long minDate = dataFormat.getDate(i);
            ArrayList<Float> item = dataFormat.getItem(i);
            frameBuilder.addContentField("DataTime", tools.timeStamp2TcpStringWithoutMs(minDate));
            Iterator it = format.getFactorMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Integer val = (Integer) entry.getValue();
                frameBuilder.addContentFactor(key,"Min", tools.float2String3(item.get(val)),
                        "Avg",tools.float2String3(item.get(val)),"Max"
                        ,tools.float2String3(item.get(val)));
            }
            command.executeSendTask(frameBuilder.setQn(qn).setSt("39").setCn("2051").setPw(format.getPassword())
                    .setMn(format.getMnCode()).insertOneFrame().getBytes());
        }
    }

    @Override
    public void uploadMinDate(long now, long date) {
        super.uploadMinDate(now, date);
    }

    @Override
    protected void sendHourData(String qn, long begin, long end, boolean response) {
        GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getHourData(begin, end);
        for (int i = 0; i < dataFormat.getSize(); i++) {
            frameBuilder.cleanContent();
            long minDate = dataFormat.getDate(i);
            ArrayList<Float> item = dataFormat.getItem(i);
            frameBuilder.addContentField("DataTime", tools.timeStamp2TcpStringWithoutMs(minDate));
            Iterator it = format.getFactorMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Integer val = (Integer) entry.getValue();
                frameBuilder.addContentFactor(key,"Min", tools.float2String3(item.get(val)),
                        "Avg",tools.float2String3(item.get(val)),"Max"
                        ,tools.float2String3(item.get(val)));
            }
            command.executeSendTask(frameBuilder.setQn(qn).setSt("39").setCn("2061").setPw(format.getPassword())
                    .setMn(format.getMnCode()).insertOneFrame().getBytes());
        }
    }

    private void sendDayData(String qn,long begin,long end,boolean response){
        GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getHourData(begin, end);
        frameBuilder.cleanContent();
        int size  = dataFormat.getSize();
        if(size>0){//有数据,则发送日数据
            frameBuilder.cleanContent();
            ArrayList<Float> results = dataFormat.getItem(0);
            ArrayList<Float> item;
            for(int i=1;i<size;i++){
                 item = dataFormat.getItem(i);
                for(int j=0;j<item.size();j++){
                    float result= results.get(j)+item.get(j);
                    results.add(j,result);
                }
            }
            Iterator it = format.getFactorMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Integer val = (Integer) entry.getValue();
                frameBuilder.addContentFactor(key,"Min", tools.float2String3(results.get(val)/size),
                        "Avg",tools.float2String3(results.get(val)/size),"Max"
                        ,tools.float2String3(results.get(val)/size));
            }

        }
    }

    @Override
    public void uploadHourDate(long now, long date) {
        if(command.isConnected()) {
            Log.d(tag,"lastUploadHourDate="+tools.timestamp2string(lastUploadHourDate)+";"+tools.timestamp2string(date));
            if(lastUploadHourDate <= date) {
                uploadHourDate = date;
                hasSendHourData = true;
                qnSend = tools.timeStamp2TcpString(now);
                sendHourData(qnSend,lastUploadHourDate,date,true);
                if((date%oneDayTimestamps)==0){//0点时刻，上传日数据
                    sendDayData(qnSend,date-oneDayTimestamps,date,true);

                }
                lastUploadHourDate = uploadHourDate;//测试用
            }
        }


    }
}
