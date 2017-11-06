package com.grean.dustctrl.protocol;

import android.util.Log;

import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;

/**
 * Created by weifeng on 2017/10/24.
 */

public class TcpClientDB12t725 implements GeneralClientProtocol,GeneralReturnProtocol{
    private float [] realTimeData = new float[7];
    private String mnCode = "88888";
    private static final String tag = "TcpClientDB12t725";
    private TcpClientCallBack callBack;
    private GeneralCommandProtocol commandProtocol;
    private GeneralInfoProtocol infoProtocol;
    private boolean heartRun = false;
    private long lastMinDate,lastHourDate;
    private HeartThread thread;
    public TcpClientDB12t725 (TcpClientCallBack callBack){
        this.callBack = callBack;
        commandProtocol = GetProtocols.getInstance().getGeneralCommandProtocol();
        infoProtocol =GetProtocols.getInstance().getInfoProtocol();
        commandProtocol.setPassWord("123456");
        commandProtocol.setMnCode(mnCode);
    }

    @Override
    public void handleProtocol(byte[] rec, int count) {
        Log.d(tag,new String(rec,0,count));
        if(commandProtocol.checkRecString(rec,count)) {
            String recString = new String(rec, 6, count - 14);
            String[] item = recString.split(";");
            for(int i=0;i<item.length;i++) {

                if(commandProtocol.handleString(item[i])){
                    break;
                }
            }
            commandProtocol.executeProtocol(this,callBack,infoProtocol);
        }

        /*String[] minutia = item[2].split("=");
        if(minutia[1].equals("2011")){//实时数据

        }else if(minutia[1].equals("1012")){//同步时间

        }else if(minutia[1].equals("2051")){//分钟数据

        }else if(minutia[1].equals("2061")){//小时数据

        }else if(minutia[1].equals("2062")){//小时数据应答

        }*/

    }


    private String insertSensorData (float[] data){
        String string = "A01-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+",A01-Flag=N;";
        return string;
    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpStringWithoutMs(now)+";";
        return  "ST=51;CN=2011;PW=123456;MN="+mnCode+";CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    /**
     * 获取粉尘浓度均值
     * @param format
     * @return
     */
    private float getMeanData(GeneralHistoryDataFormat format){
        float result = 0f;
        int size = format.getSize();
        for(int i=0;i<size;i++){
            ArrayList<Float> item = format.getItem(i);
            result += item.get(0)/size;
        }
        return result;
    }

    private float[] getMinAvgMaxData(GeneralHistoryDataFormat format){
        float[] result = {0f,0f,0f};
        int size = format.getSize();
        if(format.getSize()>0) {
            result[0] = format.getItem(0).get(0);
            result[2] = result[0];
            for (int i = 0; i < size; i++) {
                ArrayList<Float> item = format.getItem(i);
                float temp = item.get(0);
                result[1] += temp / size;
                if (result[0] > temp) {
                    result[0] = temp;
                }
                if (result[2] < temp) {
                    result[2] = temp;
                }
            }
        }
        return result;
    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
        commandProtocol.setMnCode(string);
    }

    @Override
    public boolean addSendBuff(String string) {
        if(string!=null) {
            return callBack.addOneFrame(string.getBytes());
        }
        return false;
    }

    @Override
    public void startHeartBeatPacket() {
        if(thread != null) {
            if(!thread.isAlive()) {
                thread = new HeartThread();
                thread.start();
            }
        }else {
            thread = new HeartThread();
            thread.start();
        }
    }

    @Override
    public void stopHeartBeatPacket() {
        heartRun = false;
    }

    @Override
    public void setRealTimeData(SensorData data) {
        realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
    }

    @Override
    public void setRealTimeAlarm(int alarm) {

    }

    /**
     * 装帧头 数据长度 CRC 帧尾
     * @param body
     * @return
     */
    private String insertOneFrame(String body){
        byte [] bodyBuff = body.getBytes();
        int crc = tools.calcCrc16(bodyBuff);
        byte [] crcBuff = tools.int2bytes(crc);
        byte [] crcFormatBuff = new byte[2];
        crcFormatBuff[0] = crcBuff[2];
        crcFormatBuff[1] = crcBuff[3];
        String crcString = tools.bytesToHexString(crcFormatBuff,crcFormatBuff.length);
        String lenString = String.format("%04d",bodyBuff.length);
        return "##"+lenString+body+crcString+"\r\n";
    }

    private String getMinDataString(long lastMinDate,long nexMinDate){
        float [] data = getMinAvgMaxData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate));
        return "ST=51;CN=2051;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertMinData(data[0],data[1],data[2])+"&&";
    }

    private String getHOurDataString(long lastMinDate,long nexMinDate){
        float [] data = getMinAvgMaxData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate));
        return "ST=51;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertMinData(data[0],data[1],data[2])+"&&";
    }

    private String insertMinData(float min,float avg,float max){
        String string = "A01-Min="+tools.float2String3(min)+",A01-Avg="+tools.float2String3(avg)+",A01-Max="+tools.float2String3(max);
        return string;
    }

    @Override
    public String getRealTimeData() {
        long now = tools.nowtime2timestamp();
        return insertOneFrame(getRealTimeDataString(now));
    }

    @Override
    public String getMinData(String qn,long begin, long end) {
        return insertOneFrame(getMinDataString(begin,end));
    }

    @Override
    public String getHourData(String qn,long begin, long end) {
        return insertOneFrame(getHOurDataString(begin,end));
    }


    @Override
    public String getSystemResponse(String qn) {
        return insertOneFrame("ST=91;CN=9011;PW=123456;MN="+mnCode+";Flag=1;CP=&&QN="+qn+";QnRtn=1&&");
    }

    @Override
    public String getSystemOk(String qn) {
        return insertOneFrame("ST=91;CN=9011;PW=123456;MN="+mnCode+";CP=&&QN="+qn+";ExeRtn=1&&");
    }

    private class HeartThread extends Thread{

        @Override
        public void run() {
            GeneralDataBaseProtocol dataBaseProtocol = GetProtocols.getInstance().getDataBaseProtocol();
            dataBaseProtocol.loadMinDate();
            lastMinDate = dataBaseProtocol.getNextMinDate();
            lastHourDate = dataBaseProtocol.getNextHourDate();
            dataBaseProtocol.setMinDataInterval(10*60000l);//设置为10分钟间隔
            heartRun = true;
            ClientDataBaseCtrl dataBaseCtrl = ScanSensor.getInstance();
            while (heartRun&&!interrupted()) {
                long now = tools.nowtime2timestamp();
                dataBaseCtrl.getRealTimeData(realTimeData);
                addSendBuff(insertOneFrame(getRealTimeDataString(now)));
                if(now > lastMinDate){//发送分钟数据
                    dataBaseCtrl.saveMinData(now);
                    addSendBuff(insertOneFrame(getMinDataString(dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())));
                    lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                }
                if(now > lastHourDate){
                    addSendBuff(insertOneFrame(getHOurDataString(dataBaseProtocol.getLastHourDate(),dataBaseProtocol.getNextHourDate())));
                    lastHourDate = dataBaseProtocol.calcNextHourDate(now);
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
