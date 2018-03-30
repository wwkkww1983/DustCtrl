package com.grean.dustctrl.protocol;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weifeng on 2018/3/28.
 */

public class TcpClientHjt212_2017 implements GeneralClientProtocol,GeneralReturnProtocol{
    private static final String tag = "TcpClientHjt212_2017";
    private String mnCode = "88888";
    GeneralDataBaseProtocol dataBaseProtocol;
    private boolean run;
    private HeartThread thread;
    private float [] realTimeData = new float[7];
    private long lastMinDate,now;
    private TcpClientCallBack callBack;
    private GeneralCommandProtocol commandProtocol;
    private GeneralInfoProtocol infoProtocol;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ClientDataBaseCtrl.UPDATE_REAL_TIME:
                    if(msg.obj!=null){
                        SensorData data = (SensorData) msg.obj;
                        realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
                        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
                        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
                        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
                        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
                        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
                        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
                    }

                    break;
                default:

                    break;
            }
        }
    };

    public TcpClientHjt212_2017(TcpClientCallBack callBack){
        this.callBack = callBack;
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
    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
        dataBaseProtocol = GetProtocols.getInstance().getDataBaseProtocol();
    }

    @Override
    public boolean addSendBuff(String string) {
        return callBack.addOneFrame(string.getBytes());
    }

    @Override
    public void startHeartBeatPacket() {
        thread = new HeartThread();
        thread.start();
    }

    @Override
    public void stopHeartBeatPacket() {
        run = false;
    }

    @Override
    public void setRealTimeData(SensorData data) {

    }

    @Override
    public void setRealTimeAlarm(int alarm) {

    }

    private String insertSensorData (float[] data){
        String string = "103-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+";103-Flag=N;";
        string += "104-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";104-Flag=N;";
        string += "105-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";105-Flag=N;";
        string += "106-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";106-Flag=N;";
        string += "107-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";107-Flag=N;";
        string += "108-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";108-Flag=N;";
        string += "109-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";109-Flag=N;";
        return string;
    }

    private String insertMinData(float[] data){
        String string = "103-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+";103-Flag=N;";//=2
        string += "104-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";104-Flag=N;";
        string += "105-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";105-Flag=N;";
        string += "106-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";106-Flag=N;";
        string += "107-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";107-Flag=N;";
        string += "108-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";108-Flag=N;";
        string += "109-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";109-Flag=N;";
        return string;
    }

    private String insertMinData(ArrayList<Float> item){
        String string = "103-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.Dust))+";103-Flag=N;";//=2
        string += "104-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.Noise))+";104-Flag=N;";
        string += "105-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.Humidity))+";105-Flag=N;";
        string += "106-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.Temperature))+";106-Flag=N;";
        string += "107-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.Pressure))+";107-Flag=N;";
        string += "108-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.WindForce))+";108-Flag=N;";
        string += "109-Cou="+tools.float2String3(item.get(GeneralHistoryDataFormat.WindDirection))+";109-Flag=N;";
        return string;
    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpStringWithoutMs(now)+";";
        String qnString = tools.timeStamp2TcpString(now);
        return  "QN="+qnString+";ST=21;CN=2011;PW=123456;MN="+mnCode+";Flag=9;CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String getRealTimeDataString(String qn){
        String timeString = qn.substring(0,13);
        return  "QN="+qn+";ST=21;CN=2011;PW=123456;MN="+mnCode+";Flag=9;CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String getMinDataString(long now,long lastMinDate,long nexMinDate){
        String qnString = tools.timeStamp2TcpString(now);
        return "QN="+qnString+";ST=31;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpStringWithoutMs(lastMinDate)+";"+
                insertMinData(getMeanData(dataBaseProtocol.getData(lastMinDate ,nexMinDate)))+"&&";
    }

    private String getMinDataString(String qn,long lastMinDate,long nexMinDate){
        return "QN="+qn+";ST=31;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpStringWithoutMs(lastMinDate)+";"+
                insertMinData(getMeanData(dataBaseProtocol.getData(lastMinDate ,nexMinDate)))+"&&";
    }

    private String getMinDateFormat (long date,ArrayList<Float> item){
        String dateString = "CP=&&DataTime="+tools.timeStamp2TcpString(date)+";";
        return dateString+insertMinData(item)+"&&";
    }

    private List<String> getHistoryMinDataString(String qn,long lastMinDate ,long nextMinDate){
        String head = "QN="+qn+";ST=31;CN=2061;PW=123456;MN="+mnCode+";";
        List<String> list = new ArrayList<>();
        GeneralHistoryDataFormat dataFormat = dataBaseProtocol.getData(lastMinDate,nextMinDate);
        for(int i=0;i<dataFormat.getSize();i++){
            String string = insertOneFrame(head+getMinDateFormat(dataFormat.getDate(i),dataFormat.getItem(i)));
            list.add(string);
        }
        return list;
    }

    /**
     * 取平均值
     * @param format 历史数据
     * @return
     */
    private float[] getMeanData(GeneralHistoryDataFormat format){
        float [] result = {0f,0f,0f,0f,0f,0f,0f};
        int size = format.getSize();
        for(int i=0;i<size;i++){
            ArrayList<Float> item = format.getItem(i);
            for(int j=0;j<7;j++){
                result[j] += item.get(j) / size;
            }
        }
        return result;
    }

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

    private String getHOurDataString(long lastMinDate,long nexMinDate){
        return "ST=51;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";";
        /*float [] data = getMinAvgMaxData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate));
        return "ST=51;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertMinData(data[0],data[1],data[2])+"&&";*/
    }

    @Override
    public String getRealTimeData(String qn) {
        return insertOneFrame(getRealTimeDataString(qn));
    }

    @Override
    public List<String> getMinData(String qn, long begin, long end) {
       return getHistoryMinDataString(qn,begin,end);
    }

    @Override
    public List<String> getHourData(String qn, long begin, long end) {
        return getHistoryMinDataString(qn,begin,end);
    }

    @Override
    public String getSystemResponse(String qn) {
        return insertOneFrame("QN="+qn+"ST=91;CN=9011;PW=123456;MN="+mnCode+";Flag=9;CP=&&QnRtn=1&&");
    }

    @Override
    public String getSystemOk(String qn) {
        return insertOneFrame("QN="+qn+";ST=91;CN=9012;PW=123456;MN="+mnCode+";Flag=8;CP=&&ExeRtn=1&&");
    }

    private class HeartThread extends Thread{

        @Override
        public void run() {
            run = true;
            dataBaseProtocol.loadMinDate();
            dataBaseProtocol.setMinDataInterval(60000l);//设置为1分钟间隔
            lastMinDate = dataBaseProtocol.getNextMinDate();
            Log.d(tag,"lastMinDate"+ tools.timestamp2string(lastMinDate));
            ClientDataBaseCtrl dataBaseCtrl = ScanSensor.getInstance();
            while (run&&!interrupted()) {
                now = tools.nowtime2timestamp();
                dataBaseCtrl.getRealTimeData(handler);
                addSendBuff(insertOneFrame(getRealTimeDataString(now)));
                if(now > lastMinDate){//发送分钟数据
                    dataBaseCtrl.saveMinData(now);
                    addSendBuff(insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())));
                    lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                }

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
