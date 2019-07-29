package com.grean.dustctrl.UploadingProtocol;

import android.content.pm.PackageManager;
import android.util.Log;

import com.grean.dustctrl.process.SensorData;
import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GeneralLogFormat;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by weifeng on 2018/6/29.
 */

public class HJT212_2017ProtocolState implements ProtocolState{
    private static final String tag = "HJT212_2017ProtocolState";
    protected UploadingConfigFormat format;
    protected ProtocolCommand command;
    private Hjt212FrameBuilder frameBuilder;
    protected String qnSend,qnReceived;
    protected int cn,noResponseTimes=0;
    protected SensorData realTimeData;
    private byte[] receiveBuff;
    private HashMap<String ,String> content = new HashMap<>();
    protected boolean hasReceived,hasSendPowerMessage=false,hasSendMinData = false,
            hasSendHourData,hasSendHeartPackage;
    protected long lastUploadMinDate,uploadMinDate,lastUploadHourDate,uploadHourDate;
    protected RequestHandle rhStart;
    protected interface RequestHandle{
        void handleRequest(HashMap<String,String>map);
    }

    protected class MnRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public MnRequestHandle(RequestHandle rh){
            this.rh = rh;
        }

        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("MN")!=null) {
                if (map.get("MN").equals(format.getMnCode())) {
                    rh.handleRequest(map);
                } else {
                    Log.d(tag, "MN码错误");
                }
            }
        }
    }

    protected class FlagRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public FlagRequestHandle(RequestHandle rh){
            this.rh = rh;
        }

        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("Flag")!=null){
                if(map.get("Flag").equals("9")||map.get("Flag").equals("8")) {
                    this.rh.handleRequest(map);
                }
            }else{
                Log.d(tag,"Flag异常");
            }
        }
    }

    protected class PwRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public PwRequestHandle(RequestHandle rh){
            this.rh = rh;
        }

        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("PW")!=null) {
                if (map.get("PW").equals(format.getPassword())) {
                    rh.handleRequest(map);
                } else {
                    Log.d(tag, "PW错误");
                }
            }
        }
    }

    protected class QnRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public QnRequestHandle(RequestHandle rh){
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

    protected boolean isCnAvailable(int num){
        switch (num){
            case 1000:
            case 1011:
            case 1012:
            case 1014:
            case 1015:
            case 1061:
            case 1062:
            case 1072:
            case 2011:
            case 2061:
            case 3020:
            case 3021:
            //case 3040:
            case 3041:
            //case 3042:
            case 9013:
            case 9014:
                return true;
            default:
                return false;
        }
    }

    protected class CnRequestHandle implements RequestHandle{
        private RequestHandle rh;
        public CnRequestHandle(RequestHandle rh){
            this.rh = rh;
        }


        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("CN")!=null){
                cn = Integer.valueOf(map.get("CN"));
                if(isCnAvailable(cn)){
                    rh.handleRequest(map);
                }else{
                    Log.d(tag,"不支持的CN命令");
                }

            }
        }
    }

    protected void getField(String string,HashMap<String,String> map){
        String[] strings = string.split(";");
        for(int i=0;i<strings.length;i++){
            String [] miniStrings = strings[i].split("=");
            if(miniStrings.length == 2){
                //Log.d(tag,miniStrings[0]+"="+miniStrings[1]);
                map.put(miniStrings[0],miniStrings[1]);
            }
        }
    }

    protected HashMap<String , String> getCode(String string){
        HashMap<String,String> hashMap = new HashMap<>();
        String[] strings = string.split(";");
        for(int i=0;i<strings.length;i++){
            String[] strings1 = strings[i].split(",");
            for(int j=0;j<strings1.length;j++){
                String[] strings2 = strings1[j].split("=");
                if(strings2.length==2){
                    Log.d(tag,strings2[0]+","+strings2[1]);
                    hashMap.put(strings2[0],strings2[1]);
                }
            }
        }
        return hashMap;
    }

    protected class CpRequestHandle implements RequestHandle{
        private void handleParameter(int num,String string){
            HashMap<String,String> hashMap = new HashMap<>();
            switch (num){
                case 1000://设置超时及重发次数
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
                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1011://提取仪表时间
                    getField(string,hashMap);
                    if(hashMap.get("PolId")!=null){
                        sendQnRtn(qnReceived);
                        frameBuilder.cleanContent();
                        frameBuilder.addContentField("SystemTime",tools.timeStamp2TcpStringWithoutMs(tools.nowtime2timestamp()));
                        command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                                .setCn("1011").setPw(format.getPassword()).setMn(format.getMnCode())
                                .setFlag("8").insertOneFrame().getBytes());
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1012:
                case 1015:
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
                case 1014:
                    sendQnRtn(qnReceived);
                    frameBuilder.cleanContent();
                    frameBuilder.addContentField("SystemTime",tools.timeStamp2TcpStringWithoutMs(tools.nowtime2timestamp()));
                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                            .setCn("1014").setPw(format.getPassword()).setMn(format.getMnCode())
                            .setFlag("8").insertOneFrame().getBytes());
                    sendExeRtn(qnReceived);
                    break;
                case 1061:
                    sendQnRtn(qnReceived);
                    frameBuilder.cleanContent();
                    frameBuilder.addContentField("RtdInterval",String.valueOf(format.getRealTimeInterval()));
                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                            .setCn("1061").setPw(format.getPassword()).setMn(format.getMnCode())
                            .setFlag("8").insertOneFrame().getBytes());
                    sendExeRtn(qnReceived);
                    break;
                case 1062:
                    getField(string,hashMap);
                    if(hashMap.get("RtdInterval")!=null){
                        try {
                            int rtdInterval = Integer.valueOf(hashMap.get("RtdInterval"));
                            format.setRealTimeInterval(rtdInterval);
                        }catch (NumberFormatException e){
                            Log.d(tag,"数字转换错误");
                        }

                        sendQnRtn(qnReceived);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 1072:
                    //设置数采仪访问密码
                    sendQnRtn(qnReceived);
                    sendExeRtn(qnReceived);
                    break;
            }
        }





        private void sendPolIdInfo(String polId,String code,String info){
            sendQnRtn(qnReceived);
            frameBuilder.cleanContent();
            frameBuilder.addContentInfo(polId,code,info);
            command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                    .setCn("3020").setPw(format.getPassword()).setMn(format.getMnCode())
                    .setFlag("8").insertOneFrame().getBytes());
            sendExeRtn(qnReceived);
        }

        private void handleControl(int num,String string){
            HashMap<String,String> hashMap;
            switch (num){
                case 3020://提取信息/日志
                    hashMap = getCode(string);
                    if((hashMap.get("InfoId")!=null)&&(hashMap.get("PolId")!=null)){
                        String code = hashMap.get("InfoId");
                        String polId = hashMap.get("PolId");
                        if(code.equals("i21001")){//日志
                            //Log.d(tag,"日志");
                            if((hashMap.get("BeginTime")!=null)&&(hashMap.get("EndTime")!=null)) {
                                long begin = tools.tcpTimeString2timestamp(hashMap.get("BeginTime"));
                                long end = tools.tcpTimeString2timestamp(hashMap.get("EndTime"));
                                GeneralLogFormat logFormat = GetProtocols.getInstance().getDataBaseProtocol().getLogFormat(begin,end );
                                sendQnRtn(qnReceived);
                                for (int i=0;i<logFormat.getSize();i++){
                                    frameBuilder.cleanContent();
                                    frameBuilder.addContentField("DataTime",tools.timeStamp2TcpStringWithoutMs(logFormat.getDate(i)));
                                    String logString = logFormat.getLog(i).replace(';','.');//替代;避免服务器端解析错误
                                    frameBuilder.addContentInfo(hashMap.get("PolId"),hashMap.get("InfoId"),"//"+logString+"//");
                                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                                            .setCn("3020").setPw(format.getPassword()).setMn(format.getMnCode())
                                            .setFlag("8").insertOneFrame().getBytes());
                                }
                                sendExeRtn(qnReceived);
                            }
                        }else if(code.equals("i13007")){//截距
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getParaB()));
                        }else if(code.equals("i13008")){//斜率
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getParaK()));
                        }else if(code.equals("i13100")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getValue()));
                        }else if(code.equals("i13101")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getHiTemp()));
                        }else if(code.equals("i13102")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getHiHumidity()));
                        }else if(code.equals("i13103")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getLoTemp()));
                        }else if(code.equals("i13104")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getLoHumidity()));
                        }else if(code.equals("i13105")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getLoDewPoint()));
                        }else if(code.equals("i13106")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getHiDewPoint()));
                        }else if(code.equals("i13107")){
                            sendPolIdInfo(polId,code,String.valueOf(GetProtocols.getInstance().getInfoProtocol().getSensorData().getHeatPwm()));
                        }else if(code.equals("i13108")){
                            if(GetProtocols.getInstance().getInfoProtocol().getSensorData().isCalPos()) {
                                sendPolIdInfo(polId, code, "1");
                            }else{
                                sendPolIdInfo(polId, code, "0");
                            }
                        }else if(code.equals("i13109")){
                            if(GetProtocols.getInstance().getInfoProtocol().getSensorData().isMeasurePos()) {
                                sendPolIdInfo(polId, code, "1");
                            }else{
                                sendPolIdInfo(polId, code, "0");
                            }
                        }else if(code.equals("i13110")){
                            if(GetProtocols.getInstance().getInfoProtocol().getSensorData().isAcIn()) {
                                sendPolIdInfo(polId, code, "1");
                            }else{
                                sendPolIdInfo(polId, code, "0");
                            }
                        }else if(code.equals("i13111")){
                            if(GetProtocols.getInstance().getInfoProtocol().getSensorData().isBatteryLow()) {
                                sendPolIdInfo(polId, code, "1");
                            }else{
                                sendPolIdInfo(polId, code, "0");
                            }
                        }
                    }

                    break;
                case 3021://设置参数
                    hashMap = getCode(string);
                    if((hashMap.get("InfoId")!=null)&&(hashMap.get("PolId")!=null)){
                        if(hashMap.get("PolId").equals("a01010")){
                            String infoId = hashMap.get("InfoId");
                            if(infoId.equals("i13007")){
                                if(hashMap.get("i13007-Info")!=null) {
                                    float para = Float.valueOf(hashMap.get("i13007-Info"));
                                    Log.d(tag,"设置截距"+String.valueOf(para));
                                    GetProtocols.getInstance().getInfoProtocol().setDustParaB(para);
                                    sendQnRtn(qnReceived);
                                    sendExeRtn(qnReceived);
                                }
                            }else if(infoId.equals("i13008")){
                                if(hashMap.get("i13008-Info")!=null) {
                                    float para = Float.valueOf(hashMap.get("i13008-Info"));
                                    Log.d(tag,"设置斜率"+String.valueOf(para));
                                    GetProtocols.getInstance().getInfoProtocol().setDustParaK(para);
                                    sendQnRtn(qnReceived);
                                    sendExeRtn(qnReceived);
                                }
                            }else {

                            }

                        }
                    }

                    break;
                //case 3040://提取现场信息 状态 时间

                  //  break;
                case 3041://提取经纬度及环境信息
                    Log.d(tag,"提取经纬度及环境信息");
                    sendQnRtn(qnReceived);
                    frameBuilder.cleanContent();
                    frameBuilder.addContentField("DataTime",tools.timeStamp2TcpStringWithoutMs(tools.nowtime2timestamp()));
                    frameBuilder.addContentField("Lng",String.valueOf(format.getLng()));
                    frameBuilder.addContentField("Lat",String.valueOf(format.getLat()));
                    command.executeSendTask(frameBuilder.setQn(qnReceived).setSt("21")
                            .setPw(format.getPassword()).setMn(format.getMnCode()).setFlag("8")
                            .insertOneFrame().getBytes());
                    sendExeRtn(qnReceived);
                    break;
                default:
                    break;
            }
        }

        private void handleData(int num,String string){
            long begin=0,end=0;
            HashMap<String,String> hashMap = new HashMap<>();
            switch (num){
                case 2011://提取分钟数据
                    getField(string,hashMap);

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
                    getField(string,hashMap);
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

        private void handleInteraction(int num,String string){
            switch (num){
                case 9013:

                    break;
                case 9014:
                    if(hasReceived) {
                        hasReceived = false;
                        if(hasSendMinData) {
                            hasSendMinData = false;
                            //Log.d(tag,"接收到分钟数据");
                            lastUploadMinDate = uploadMinDate;
                        }

                        if(hasSendHourData){
                            //Log.d(tag,"接收到小时数据");
                            hasSendHourData = false;
                            lastUploadHourDate = uploadHourDate;
                        }

                        if(hasSendHeartPackage){
                            //Log.d(tag,"接收到心跳包");
                            hasSendHeartPackage = false;

                        }
                    }
                    break;

                default:
                    break;
            }

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


    public HJT212_2017ProtocolState(ProtocolCommand command){
        this.command = command;
        initConfig();
    }

    protected void initConfig(){
        frameBuilder = new Hjt212FrameBuilder();
        RequestHandle cp = new CpRequestHandle();
        RequestHandle cnRh = new CnRequestHandle(cp);
        RequestHandle qn = new QnRequestHandle(cnRh);
        RequestHandle flag = new FlagRequestHandle(qn);
        RequestHandle pw = new PwRequestHandle(flag);
        RequestHandle mn = new MnRequestHandle(pw);
        rhStart = mn;
    }

    private boolean checkFrameLength(byte[] buff,int length){
        if(length < 12){
            return false;
        }
        String string = new String(buff,2,4);
        int len = Integer.parseInt(string);
        if(len!=(length-6)){//帧长
            return false;
        }
        return true;
    }

    private boolean checkFrameHead(byte[] buff,int length){
        String string = new String(buff,0,2);
        if(length < 6){
            return false;
        }

        if(!string.equals("##")){//帧头
            return false;
        }else{
            return true;
        }
    }

    private boolean checkFrameTail(byte[] buff,int length){
        String string = new String(buff,length-2,2);
        if(!string.equals("\r\n")){//帧尾
            return false;
        }else {
            return true;
        }
    }

    private int getFrameProtocolLength(byte[] buff,int index){
        try {
            return Integer.valueOf(new String(buff, index + 2, 4));
        }catch (NumberFormatException e){
            return 0;
        }
    }

    private boolean checkFrame(byte[] buff,int length){
        if(length < 12){
            return false;
        }
        String string = new String(buff,2,4);
        try {
            int len = Integer.valueOf(string);
            if (len != (length - 12)) {//帧长
                Log.d(tag, "length error");
                return false;
            }
        }catch (NumberFormatException e){
            return false;
        }


        string = new String(buff,0,2);
        if(!string.equals("##")){//帧头
            Log.d(tag,"head error");
            return false;
        }
        string = new String(buff,length-2,2);
        if(!string.equals("\r\n")){//帧尾
            Log.d(tag,"tail error");
            return false;
        }
        return true;
    }

    private String getContent(byte[] buff,int length){
        return new String(buff,6,length-12);
    }

    private HashMap<String ,String> getString(String string){
        HashMap<String,String> map = new HashMap<>();
        String[] bigStrings = string.split("&&");
        if(bigStrings.length>0){
            //Log.d(tag,"BigString="+String.valueOf(bigStrings.length));
            getField(bigStrings[0],map);
            if(bigStrings.length>1){
                map.put("CP",bigStrings[1]);
            }else{
                map.put("CP","");
            }
        }
        return map;
    }

    private void handleProtocol(String content){
        //Log.d(tag,"正常帧:"+content);
        this.content = getString(content);
        /*Iterator it = this.content.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            Log.d(tag,(String)key+"="+(String)val);
        }*/
        rhStart.handleRequest(this.content);
    }


    /**
     * 分割数据包
     * @param buff
     * @param length
     * @param frameLength
     */
    private void partitionFrame(byte[] buff,int length,int frameLength){
        int restLength = length-frameLength;//余下数据
        int firstLength = frameLength;
        int index=0;
        byte[] tempBuff = new byte[firstLength];
        while (restLength>0){
            Log.d(tag,String.valueOf(index)+";"+String.valueOf(firstLength));
            System.arraycopy(buff,index,tempBuff,0,firstLength);
            if(checkFrame(tempBuff,firstLength)){
                handleProtocol(getContent(buff,firstLength));
                index+=firstLength;//迁移地址
                if((length - index)<6){
                    restLength=0;
                }else{
                    //需要检查frame头
                    firstLength = getFrameProtocolLength(buff,index)+6 ;
                    restLength = length - firstLength-index;//小于则退出循环，大于等则继续
                }
            }else{
                index+=2;
                while(!((buff[index]=='#')&&(buff[index+1]=='#'))){
                    index++;
                    if(index >= (length-2)){

                        break;
                    }
                }
                firstLength = length - index;
                if(firstLength<=2){
                    restLength = 0;
                }else {
                    restLength = length - firstLength;
                }
            }
        }
        Log.d(tag,"处理完"+String.valueOf(restLength));
        if(restLength > 0){
            buff = new byte[restLength];
            System.arraycopy(tempBuff,0,buff,0,restLength);
        }else{
            buff = new byte[1];
        }

    }

    private int getFrameTail(byte[] buff,int length){
        int i;
        for(i=(length-1);i>=1;i--){
            if((buff[i-1]=='\r')&&(buff[i]=='\n')){
                break;
            }
        }
        return i+1;
    }

    @Override
    public void handleReceiveBuff(byte[] buff, int length) {
        //Log.d(tag,"size="+String.valueOf(length)+":"+new String(buff,0,length));
        int len = getFrameTail(buff,length);
        if(len==length){

            if(checkFrame(buff,length)){//合规帧处理，不合规舍弃
                //Log.d(tag,"合规");
                handleProtocol(getContent(buff,length));
                receiveBuff = null;
            }else{
                if(receiveBuff!=null){//
                    //Log.d(tag,"需要拼接");
                    receiveBuff = tools.copyArray(receiveBuff,receiveBuff.length,buff,length);
                    //Log.d(tag,"size="+String.valueOf(receiveBuff.length)+":"+new String(receiveBuff,0,receiveBuff.length));
                    if(checkFrame(receiveBuff,receiveBuff.length)){//合规帧处理，不合规舍弃
                        //Log.d(tag,"合规");
                        handleProtocol(getContent(receiveBuff,receiveBuff.length));
                    }
                    receiveBuff = null;
                }
            }

        }else if(len > 1){
            //Log.d(tag,"结束符在中间，处理粘包 size="+String.valueOf(len));
            if(checkFrame(buff,len)){
                //Log.d(tag,"合规");
                handleReceiveBuff(buff,len);
            }
            receiveBuff = new byte[length-len];
            System.arraycopy(buff,len,receiveBuff,0,length - len);
        }else{
            //Log.d(tag,"无结束符");
            if(receiveBuff != null){
                receiveBuff = tools.copyArray(receiveBuff,receiveBuff.length,buff,length);
                if(receiveBuff.length > 9999){
                    receiveBuff = null;
                }
            }else{
                receiveBuff = new byte[length];
                System.arraycopy(buff,0,receiveBuff,0,length);
            }
        }


        /*if(checkFrame(buff,length)){//合规帧
            handleProtocol(getContent(buff,length));
        }else{//处理异常帧
            if(checkFrameHead(buff,length)){
                int frameLength = getFrameProtocolLength(buff,0)+12;
                receiveBuff = new byte[length];
                System.arraycopy(buff,0,receiveBuff,0,length);
                if(frameLength <= length){//过长需要分割
                    Log.d(tag,"过长,需要分割");
                    partitionFrame(receiveBuff,length,frameLength);
                    Log.d(tag,new String(receiveBuff,0,receiveBuff.length));
                }else {//过短需要拼接
                    Log.d(tag,"过短,需要拼接");
                }

            }else{
                if(receiveBuff.length>1){
                    int frameLength = length + receiveBuff.length;
                    byte[] tempBuff = new byte[frameLength];
                    System.arraycopy(receiveBuff,0,tempBuff,0,receiveBuff.length);
                    System.arraycopy(buff,0,tempBuff,receiveBuff.length,length);
                    if(checkFrame(tempBuff,frameLength)){
                        handleProtocol(getContent(tempBuff,frameLength));
                    }else {
                        receiveBuff = new byte[frameLength];
                        System.arraycopy(tempBuff,0,receiveBuff,0,frameLength);
                        int tempLength = getFrameProtocolLength(tempBuff,frameLength);
                        if(tempLength <= frameLength){
                            partitionFrame(receiveBuff,frameLength,tempLength);
                        }else{
                            Log.d(tag,"帧长"+String.valueOf(tempLength)+"/"+String.valueOf(frameLength)+"过短需要拼接");
                        }
                    }
                }else{
                    Log.d(tag,"首次接收异常帧头，舍弃");
                }
                if(receiveBuff.length > 9999){
                    Log.d(tag,"累计超长帧，舍弃");
                    receiveBuff=new byte[1];
                }
            }
            receiveBuff=new byte[1];//清空
        }*/
    }

    @Override
    public void uploadSystemTime(long now, long lastMinDate, long lastHourDate) {
        this.lastUploadHourDate = lastHourDate;
        this.lastUploadMinDate = lastMinDate;
        uploadMinDate = lastMinDate;
        uploadHourDate = lastHourDate;
        if(command.isConnected()){
            hasSendPowerMessage = true;
            frameBuilder.cleanContent();
            qnSend = tools.timeStamp2TcpString(now);
            frameBuilder.addContentField("DataTime",tools.timeStamp2TcpStringWithoutMs(now));
            command.executeSendTask(frameBuilder.setSt("21").setCn("2018").setPw(format.getPassword())
                    .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
        }
    }

    protected void sendMinData(String qn,long begin,long end,boolean response){
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
                frameBuilder.addContentFactor(key,"Rtd", tools.float2String3(item.get(val)), "N");
            }
            if(response) {
                command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2011").setPw(format.getPassword())
                        .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
            }else{
                command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2011").setPw(format.getPassword())
                        .setMn(format.getMnCode()).setFlag("8").insertOneFrame().getBytes());
            }
        }
    }

    @Override
    public void uploadMinDate(long now, long date) {
        if(command.isConnected()) {
            if(lastUploadMinDate <= date) {
                uploadMinDate = date;
                hasSendMinData = true;
                qnSend = tools.timeStamp2TcpString(now);
                sendMinData(qnSend,lastUploadMinDate,date,true);
                lastUploadMinDate = uploadMinDate;//测试用
            }
        }
    }

    @Override
    public void uploadSecondDate(long now) {
        if(command.isConnected()) {
            if (!hasSendPowerMessage) {
                hasSendPowerMessage = true;
                frameBuilder.cleanContent();
                qnSend = tools.timeStamp2TcpString(now);
                frameBuilder.addContentField("DataTime", tools.timeStamp2TcpStringWithoutMs(now));
                command.executeSendTask(frameBuilder.setSt("21").setCn("2018").setPw(format.getPassword())
                        .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
            } else {
                if(hasSendHeartPackage){
                   noResponseTimes++;
                }
                qnSend = tools.timeStamp2TcpString(now);
                hasSendHeartPackage = true;
                sendHeartPackage(qnSend);
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

    protected void sendHourData(String qn,long begin,long end,boolean response){
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
                frameBuilder.addContentFactor(key,"Avg", tools.float2String3(item.get(val)), "N");
            }
            if(response) {
                command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2061").setPw(format.getPassword())
                        .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
            }else{
                command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2061").setPw(format.getPassword())
                        .setMn(format.getMnCode()).setFlag("8").insertOneFrame().getBytes());
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
                lastUploadHourDate = uploadHourDate;//测试用
            }
        }
    }

    @Override
    public void setConfig(UploadingConfigFormat format) {
        format.addFactor("a01010",GeneralHistoryDataFormat.Dust);
        format.addFactor("a01011",GeneralHistoryDataFormat.Noise);
        format.addFactor("a01002",GeneralHistoryDataFormat.Humidity);
        format.addFactor("a01001",GeneralHistoryDataFormat.Temperature);
        format.addFactor("a01006",GeneralHistoryDataFormat.Pressure);
        format.addFactor("a01007",GeneralHistoryDataFormat.WindForce);
        format.addFactor("a01008",GeneralHistoryDataFormat.WindDirection);
        this.format = format;
    }

    @Override
    public void setRealTimeData(SensorData data) {
        this.realTimeData = data;
    }

    private void sendQnRtn(String qn){
        frameBuilder.cleanContent();
        frameBuilder.contentQnRtn();
        command.executeSendTask(frameBuilder.setQn(qn).setSt("91").setCn("9011")
                .setPw(format.getPassword()).setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
    }

    private void sendExeRtn(String qn){
        frameBuilder.cleanContent();
        frameBuilder.contentExeRtn();
        command.executeSendTask(frameBuilder.setQn(qn).setSt("91").setCn("9012")
                .setPw(format.getPassword()).setMn(format.getMnCode()).setFlag("8").insertOneFrame().getBytes());
    }

    private void sendHeartPackage(String qn){
        frameBuilder.cleanContent();
        command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("9015")
                .setPw(format.getPassword()).setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());

    }
}
