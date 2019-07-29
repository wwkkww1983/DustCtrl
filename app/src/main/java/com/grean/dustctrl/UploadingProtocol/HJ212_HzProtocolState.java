package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import org.json.JSONException;

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
            if(map.get("QN").equals(qnSend)){
                //Log.d(tag,"接收到返回帧");
                hasReceived = true;
            }else{
                qnReceived = map.get("QN");
            }
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


    private void sendQnRtn(String qn){
        frameBuilder.cleanContent();
        frameBuilder.contentQnRtn();
        command.executeSendTask(frameBuilder.setQn(qn).setSt("91").setCn("9011")
                .setPw(format.getPassword()).setMn(format.getMnCode()).setFlag("0").insertOneFrame().getBytes());
    }

    private void sendExeRtn(String qn){
        frameBuilder.cleanContent();
        frameBuilder.contentExeRtn();
        command.executeSendTask(frameBuilder.setQn(qn).setSt("91").setCn("9012")
                .setPw(format.getPassword()).setMn(format.getMnCode()).insertOneFrame().getBytes());
    }


    private class HzCpRequestHandle implements RequestHandle{

        private void handleParameter (int num,String string){
            HashMap<String,String> hashMap = new HashMap<>();
            switch (num){
                case 1000:
                    getField(string,hashMap);
                    if((hashMap.get("OverTime")!=null)&&(hashMap.get("ReCount")!=null)) {
                        try {
                            Log.d(tag, "设置超时及重发次数");
                            int overTime = Integer.valueOf(hashMap.get("OverTime"));
                            int reCount = Integer.valueOf(hashMap.get("ReCount"));
                            format.setTimeoutLimit(overTime);
                            format.setTimeoutRepetition(reCount);
                        }catch (NumberFormatException e){
                            Log.d(tag,"数字转换错误");
                        }//存储
                        saveUploadConfig();
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1001:
                    getField(string,hashMap);
                    if(hashMap.get("WarnTime")!=null) {
                        try {
                            Log.d(tag, "设置超时及重发次数");
                            int warnTime = Integer.valueOf(hashMap.get("WarnTime"));
                            format.setWarnTime(warnTime);
                        }catch (NumberFormatException e){
                            Log.d(tag,"数字转换错误");
                        }//存储
                        saveUploadConfig();
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1011:
                    getField(string,hashMap);
                    if(hashMap.get("PolId")!=null){
                        sendQnRtn(qnReceived);
                        frameBuilder.cleanContent();
                        frameBuilder.addContentField("SystemTime",tools.timeStamp2TcpStringWithoutMs(tools.nowtime2timestamp()));
                        command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("32")
                                .setCn("1011").setPw(format.getPassword()).setMn(format.getMnCode())
                                .insertOneFrame().getBytes());
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1012:
                    getField(string,hashMap);
                    if(hashMap.get("SystemTime")!=null) {
                        String systemTime = hashMap.get("SystemTime");
                        int year, month, day, hour, min, second;
                        year = Integer.valueOf(systemTime.substring(0, 4));
                        month = Integer.valueOf(systemTime.substring(4, 6));
                        day = Integer.valueOf(systemTime.substring(6, 8));
                        hour = Integer.valueOf(systemTime.substring(8, 10));
                        min = Integer.valueOf(systemTime.substring(10, 12));
                        second = Integer.valueOf(systemTime.substring(12, 14));
                        Log.d(tag, String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(min) + ":" + String.valueOf(second));
                        GetProtocols.getInstance().getInfoProtocol().setSystemDate(year, month, day, hour, min, second);
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1021://提取报警门限值
                    getField(string,hashMap);
                    if(hashMap.get("PolId")!=null){
                        String polId = hashMap.get("PolId");
                        if(polId.equals("a34001")){
                            sendQnRtn(qnReceived);
                            frameBuilder.cleanContent();
                            frameBuilder.addContentValues(polId,"LowValue","0","UpVlaue",
                                    String.valueOf(GetProtocols.getInstance().getInfoProtocol().getAlarmDust()));
                            command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("32")
                                    .setCn("1021").setPw(format.getPassword()).setMn(format.getMnCode())
                                    .insertOneFrame().getBytes());
                            sendExeRtn(qnReceived);
                        }
                    }
                    break;
                case 1022:
                    hashMap = getCode(string);
                    if(hashMap.get("a34001-UpValue")!=null){
                        String upValue = hashMap.get("a34001-UpValue");
                        try {
                            float fUpValue = Float.valueOf(upValue);
                            GetProtocols.getInstance().getInfoProtocol().setAlarmDust(fUpValue);
                            saveUploadConfig();
                            sendQnRtn(qnReceived);
                            sendExeRtn(qnReceived);
                        }catch(Exception e){

                        }
                    }
                    break;
                case 1031:
                    sendQnRtn(qnReceived);
                    frameBuilder.cleanContent();
                    frameBuilder.addContentField("AlarmTarget",format.getAlarmTarget());
                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("32")
                            .setCn("1031").setPw(format.getPassword()).setMn(format.getMnCode())
                            .insertOneFrame().getBytes());
                    sendExeRtn(qnReceived);
                    break;
                case 1032:
                    getField(string,hashMap);
                    if(hashMap.get("AlarmTarget")!=null){
                        format.setAlarmTarget(hashMap.get("AlarmTarget"));
                        saveUploadConfig();
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;

                case 1061:
                    sendQnRtn(qnReceived);
                    frameBuilder.cleanContent();
                    frameBuilder.addContentField("RtdInterval",String.valueOf(format.getRealTimeInterval()));
                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("32")
                            .setCn("1061").setPw(format.getPassword()).setMn(format.getMnCode())
                            .insertOneFrame().getBytes());
                    sendExeRtn(qnReceived);
                    break;
                case 1062:
                    getField(string,hashMap);
                    if(hashMap.get("RtdInterval")!=null){
                        try {
                            int rtdInterval = Integer.valueOf(hashMap.get("RtdInterval"));
                            format.setRealTimeInterval(rtdInterval);
                            saveUploadConfig();
                        }catch (NumberFormatException e){
                            Log.d(tag,"数字转换错误");
                        }

                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1072:
                    getField(string,hashMap);
                    if(hashMap.get("PW")!=null){
                        format.setPassword(hashMap.get("PW"));
                        saveUploadConfig();
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                default:

                    break;
            }
        }

        private void handleInteraction (int num,String string){

        }

        private void handleData (int num,String string){
            long begin=0,end=0;
            HashMap<String,String> hashMap = new HashMap<>();
            switch(num){
                case 2011:
                    sendQnRtn(qnReceived);
                    realTimeDataEnable = true;
                    break;
                case 2012:
                    realTimeDataEnable = false;
                    sendExeRtn(qnReceived);
                    break;
                case 2051:
                    hashMap = getCode(string);
                    if((hashMap.get("BeginTime")!=null)&&(hashMap.get("EndTime")!=null)){
                        //Log.d(tag,"提取分钟数据2");
                        begin = tools.tcpTimeString2timestamp(hashMap.get("BeginTime"))-60000l;
                        end = tools.tcpTimeString2timestamp(hashMap.get("EndTime"));
                        Log.d(tag,tools.timestamp2string(begin)+"->"+tools.timestamp2string(end));
                        sendQnRtn(qnReceived);
                        sendMinData(qnReceived,begin,end,false);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 2061://提取小时数据
                    hashMap = getCode(string);
                    if((hashMap.get("BeginTime")!=null)&&(hashMap.get("EndTime")!=null)) {
                        begin = tools.tcpTimeString2timestamp(hashMap.get("BeginTime"));
                        end = tools.tcpTimeString2timestamp(hashMap.get("EndTime"));
                        sendQnRtn(qnReceived);
                        sendHourData(qnReceived,begin,end,false);
                        sendExeRtn(qnReceived);
                    }
                    break;
                default:

                    break;

            }


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

    private void saveUploadConfig(){
        try {
            GetProtocols.getInstance().getInfoProtocol().saveUploadingConfig(
                    format.getConfigString());
        } catch (JSONException e) {
            e.printStackTrace();
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
            //case 1041:
            //case 1042:
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
