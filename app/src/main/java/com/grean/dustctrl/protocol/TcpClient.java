package com.grean.dustctrl.protocol;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/1.
 */

public class TcpClient implements GeneralClientProtocol{
    private static final String tag = "TcpClient";
   // private String heartString = "Grean.com.cn";
    private TcpClientCallBack callBack;
    private String mnCode = "88888";
    private boolean run;
    private HeartThread thread;
    private float [] realTimeData = new float[7];
    //private static String realTimeDataBody = "&&";
    private long lastMinDate,minInterval,now;
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

    public TcpClient(TcpClientCallBack callBack){
       this.callBack =  callBack;
    }
    @Override
    public void handleProtocol(byte[] rec, int count) {
        Log.d(tag,new String(rec,0,count));

    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
    }

    @Override
    public boolean addSendBuff(String string) {
       // Log.d(tag,string);
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
        //Log.d(tag,"copy data"+String.valueOf(data.getNoise()));
        /*realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();*/
    }

    @Override
    public void setRealTimeAlarm(int alarm) {

    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpStringWithoutMs(now)+";";
        //return  "QN="+timeString+";ST=31;CN=2011;PW=123456;MN="+mnCode+";CP=&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
        return  "ST=31;CN=2011;PW=123456;MN="+mnCode+";CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String getMinDataString(long now,long lastMinDate,long nexMinDate){
        //return "QN="+tools.timeStamp2TcpString(now)+";ST=31;CN=2061;PW=123456;MN="+mnCode+";CP=&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
         //       insertMinData(getMeanData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate)))+"&&";
        return "ST=31;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpStringWithoutMs(lastMinDate)+";"+
                insertMinData(getMeanData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate)))+"&&";
    }

    /**
     * 插入报警数据
     * @param data
     * @param alarmData
     * @return
     */
    private String insetRealTimeDataAlarmData(float[] data,float[] alarmData){
        String string;
        if(data[GeneralHistoryDataFormat.Dust] >= alarmData[GeneralHistoryDataFormat.Dust]){
            string = "01-Ala="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+",AlarmType =1";
        }else {
            string = "01-Ala=" + tools.float2String3(data[GeneralHistoryDataFormat.Dust]) + ",AlarmType =0";
        }
        return string;
    };

    private String insertSensorData (float[] data){
        //Log.d(tag,"paste data"+String.valueOf(data[GeneralHistoryDataFormat.Noise]));
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

    /**
     * 获取均值
     * @param format
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

   /* private final static   String[] data = {
            "20180314010600","726.917","10.820","44.297","1002.167","0.687","310.133","0.000"
    };

    private String insertOneData(int i){


        String string = "103-Cou="+data[1+i*8]+";103-Flag=N;";//=2
        string += "106-Cou="+data[2+i*8]+";106-Flag=N;";
        string += "105-Cou="+data[3+i*8]+";105-Flag=N;";
        string += "107-Cou="+data[4+i*8]+";107-Flag=N;";
        string += "108-Cou="+data[5+i*8]+";108-Flag=N;";
        string += "109-Cou="+data[6+i*8]+";109-Flag=N;";
        string += "104-Cou="+data[7+i*8]+";104-Flag=N;";
        return string;
    }*/


    private class HeartThread extends Thread{
        private static final long INTERVAL = 60000l;//间隔时间为1分钟
        @Override
        public void run() {
            run = true;
            GeneralDataBaseProtocol dataBaseProtocol = GetProtocols.getInstance().getDataBaseProtocol();
            dataBaseProtocol.loadMinDate();
            dataBaseProtocol.setMinDataInterval(INTERVAL);//设置为1分钟间隔
            lastMinDate = dataBaseProtocol.getNextMinDate();
            Log.d(tag,"lastMinDate"+tools.timestamp2string(lastMinDate));
            ClientDataBaseCtrl dataBaseCtrl = ScanSensor.getInstance();
            long lastProtocolMinDate = lastMinDate;
            while (run&&!interrupted()) {
                now = tools.nowtime2timestamp();
                dataBaseCtrl.getRealTimeData(handler);
                addSendBuff(insertOneFrame(getRealTimeDataString(now)));
                if(now > lastProtocolMinDate){//发送分钟数据
                    dataBaseCtrl.saveMinData(now);//保存数据
                    if(addSendBuff(insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())))) {
                        //Log.d(tag,"发送分钟数据");
                        lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                        lastProtocolMinDate = lastMinDate;
                        while (now > lastMinDate){

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(addSendBuff(insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())))) {
                                Log.d(tag,"补发分钟数据"+tools.timestamp2string(lastMinDate));
                                lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                                lastProtocolMinDate = lastMinDate;
                            }else{
                                Log.d(tag,"补发中断");
                                break;
                            }

                        }

                    }else{
                        while (now > lastProtocolMinDate) {
                            lastProtocolMinDate += INTERVAL;
                        }
                    }

                }



                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /*int size = data.length;
            int len = size/8;
            String string;
            for(int i=0;i<len;i++){
                Log.d(tag,"第"+String.valueOf(i)+":"+data[i*8]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                string = "ST=31;CN=2061;PW=123456;MN="+"1000200006"+";CP=&&DataTime="+data[i*8]+";"+
                        insertOneData(i)+"&&";
                addSendBuff(insertOneFrame(string));
            }
            Log.d(tag,"over");*/
        }
    }
}
