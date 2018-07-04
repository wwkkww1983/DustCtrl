package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.grean.dustctrl.SystemConfig;
import com.grean.dustctrl.protocol.GeneralHistoryDataFormat;
import com.grean.dustctrl.protocol.GetProtocols;
import com.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by weifeng on 2018/6/29.
 */

public class HJT212_2017ProtocolState implements ProtocolState{
    private static final String tag = "HJT212_2017ProtocolState";
    private UploadingConfigFormat format;
    private ProtocolCommand command;
    private Hjt212FrameBuilder frameBuilder;
    private String qnSend,qnReceived;
    private int cn,noResponseTimes=0;
    private byte[] receiveBuff = new byte[1];
    private HashMap<String ,String> content = new HashMap<>();
    private boolean hasReceived,hasSendPowerMessage=false,hasSendMinData = false,
            hasSendHourData,hasSendHeartPackage;
    private long lastUploadMinDate,uploadMinDate,lastUploadHourDate,uploadHourDate;
    private RequestHandle rhStart;
    private interface RequestHandle{
        void handleRequest(HashMap<String,String>map);
    }

    private class MnRequestHandle implements RequestHandle{
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

    private class FlagRequestHandle implements RequestHandle{
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

    private class PwRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public PwRequestHandle(RequestHandle rh){
            this.rh = rh;
        }

        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("PW")!=null) {
                if (map.get("PW").equals("123456")) {
                    rh.handleRequest(map);
                } else {
                    Log.d(tag, "PW错误");
                }
            }
        }
    }

    private class QnRequestHandle implements RequestHandle{
        private RequestHandle rh;

        public QnRequestHandle(RequestHandle rh){
            this.rh = rh;
        }
        @Override
        public void handleRequest(HashMap<String, String> map) {
            if(map.get("QN").equals(qnSend)){
                Log.d(tag,"接收到返回帧");
                hasReceived = true;
            }else{
                qnReceived = map.get("QN");
            }
            rh.handleRequest(map);
        }
    }

    private boolean isCnAvailable(int num){
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
            case 3040:
            case 3041:
            case 3042:
            case 9013:
            case 9014:
                return true;
            default:
                return false;
        }
    }

    private class CnRequestHandle implements RequestHandle{
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
                    Log.d(tag,"不支持CN命令");
                }

            }
        }
    }

    private class CpRequestHandle implements RequestHandle{
        private void handleParameter(int num,String string){
            switch (num){
                case 1000:

                    break;
                case 1011:

                    break;
                case 1012:

                    break;
                case 1014:

                    break;
                case 1015:

                    break;
                case 1061:

                    break;
                case 1062:

                    break;
                case 1072:

                    break;
            }
        }

        private void handleControl(int num,String string){
            switch (num){
                case 3020:

                    break;
                case 3021:

                    break;
                case 3040:

                    break;
                case 3041:

                    break;
                default:

                    break;
            }
        }

        private void handleData(int num,String string){
            long begin=0,end=0;
            String[] factors;
            switch (num){
                case 2011://提取分钟数据
                    factors = string.split(";");
                    for(int i=0;i<factors.length;i++){
                        String[] fields = factors[i].split("=");
                        if(fields.length == 2){
                            if(fields[0].equals("BeginTime")){
                                begin = Long.valueOf(fields[1])*1000l;
                            }else if(fields[0].equals("EndTime")){
                                end = Long.valueOf(fields[1])*1000l;
                            }else{

                            }
                        }
                    }
                    if((begin!=0)&&(end!=0)){
                        sendQnRtn(qnReceived);
                        sendMinData(qnReceived,begin,end);
                        sendExeRtn(qnReceived);
                    }
                    break;
                case 2061://提取小时数据
                    factors = string.split(";");
                    for(int i=0;i<factors.length;i++){
                        String[] fields = factors[i].split("=");
                        if(fields.length == 2){
                            if(fields[0].equals("BeginTime")){
                                begin = Long.valueOf(fields[1])*1000l;
                            }else if(fields[0].equals("EndTime")){
                                end = Long.valueOf(fields[1])*1000l;
                            }else{

                            }
                        }
                    }
                    if((begin!=0)&&(end!=0)){
                        sendQnRtn(qnReceived);
                        sendHourData(qnReceived,begin,end);
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
                            Log.d(tag,"接收到分钟数据");
                            lastUploadMinDate = uploadMinDate;
                        }

                        if(hasSendHourData){
                            hasSendHourData = false;
                            lastUploadHourDate = uploadHourDate;
                        }

                        if(hasSendHeartPackage){
                            Log.d(tag,"接收到心跳包");
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
                Log.d(tag, "处理内容:"+map.get("CP"));
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
        return Integer.parseInt(new String(buff,index+2,4));
    }

    private boolean checkFrame(byte[] buff,int length){
        if(length < 12){
            return false;
        }
        String string = new String(buff,2,4);
        int len = Integer.parseInt(string);
        if(len!=(length-12)){//帧长
            Log.d(tag,"length error");
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
        String[] strings = string.split(";");
        for(int i=0;i<strings.length;i++){
            String [] miniStrings = strings[i].split("=");
            if(miniStrings.length == 2){
                map.put(miniStrings[0],miniStrings[1]);
            }else{
                if(miniStrings[0].equals("CP")){//提取CP内容
                    String[] nanoStrings = strings[i].split("&&");
                    if(nanoStrings.length>1) {
                        map.put("CP", nanoStrings[1]);
                    }
                }
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

    @Override
    public void handleReceiveBuff(byte[] buff, int length) {
        Log.d(tag,"size="+String.valueOf(length)+":"+new String(buff,0,length));
        if(checkFrame(buff,length)){//合规帧
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
        }
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

    private void sendMinData(String qn,long begin,long end){
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
            command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2011").setPw(format.getPassword())
                    .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
        }
    }

    @Override
    public void uploadMinDate(long now, long date) {
        if(command.isConnected()) {
            if(lastUploadMinDate <= date) {
                uploadMinDate = date;
                hasSendMinData = true;
                qnSend = tools.timeStamp2TcpString(now);
                sendMinData(qnSend,lastUploadMinDate,date);
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
            command.reconnect();
        }

    }

    private void sendHourData(String qn,long begin,long end){
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
            command.executeSendTask(frameBuilder.setQn(qn).setSt("21").setCn("2061").setPw(format.getPassword())
                    .setMn(format.getMnCode()).setFlag("9").insertOneFrame().getBytes());
        }
    }

    @Override
    public void uploadHourDate(long now, long date) {
        if(command.isConnected()) {
            if(lastUploadHourDate <= date) {
                uploadHourDate = date;
                hasSendHourData = true;
                qnSend = tools.timeStamp2TcpString(now);
                sendHourData(qnSend,lastUploadHourDate,date);
            }
        }
    }

    @Override
    public void setConfig(UploadingConfigFormat format) {
        format.addFactor("a01010",GeneralHistoryDataFormat.Dust);
        format.addFactor("a01011",GeneralHistoryDataFormat.Noise);
        format.addFactor("a01012",GeneralHistoryDataFormat.Humidity);
        format.addFactor("a01013",GeneralHistoryDataFormat.Temperature);
        format.addFactor("a01014",GeneralHistoryDataFormat.Pressure);
        format.addFactor("a01015",GeneralHistoryDataFormat.WindForce);
        format.addFactor("a01016",GeneralHistoryDataFormat.WindDirection);
        this.format = format;
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
