package com.grean.dustctrl.protocol;

import android.os.Handler;
import android.os.Message;

import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weifeng on 2017/10/25.
 */

public class TcpClientShanghaiLocal implements GeneralClientProtocol,GeneralReturnProtocol{
    private String dustFlag="N",windSpeedFlag="N";
    private float [] realTimeData = new float[7];
    private TcpClientCallBack callBack;
    private HeartThread thread;
    private boolean heartRun = false;
    private String mnCode;
    private long lastMinDate,lastHourDate;
    private GeneralInfoProtocol infoProtocol;
    private GeneralCommandProtocol commandProtocol;
    private Handler handler = new Handler(){
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

    public TcpClientShanghaiLocal(TcpClientCallBack callBack){
        this.callBack = callBack;
        commandProtocol = GetProtocols.getInstance().getGeneralCommandProtocol();
        commandProtocol.setPassWord("123456");
        commandProtocol.setMnCode(mnCode);
        infoProtocol =GetProtocols.getInstance().getInfoProtocol();
    }
    @Override
    public void handleProtocol(byte[] rec, int count) {
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

    private float[] getMinAvgMaxData(int num,GeneralHistoryDataFormat format){
        float[] result = {0f,0f,0f};
        int size = format.getSize();
        if(format.getSize()>0) {
            result[0] = format.getItem(0).get(num);
            result[2] = result[0];
            for (int i = 0; i < size; i++) {
                ArrayList<Float> item = format.getItem(i);
                float temp = item.get(num);
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

    private String getMinDataString(long lastMinDate,long nexMinDate){
        GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate);
        float [] data = getMinAvgMaxData(GeneralHistoryDataFormat.Dust,dataFormat);
        String string =  "ST=51;CN=2051;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertFactorData("a34001",data[0],data[1],data[2])+"&&";
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Temperature,dataFormat);
        string += insertFactorData("a01001",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Humidity,dataFormat);
        string += insertFactorData("a01002",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Pressure,dataFormat);
        string += insertFactorData("a01006",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.WindForce,dataFormat);
        string += insertFactorData("a01007",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.WindDirection,dataFormat);
        string += insertFactorData("a01008",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Noise,dataFormat);
        string += insertFactorData("a50001",data[0],data[1],data[2]);
        string += "&&";
        return string;
    }

    private String getHOurDataString(long lastMinDate,long nexMinDate){
        GeneralHistoryDataFormat dataFormat = GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate);
        float [] data = getMinAvgMaxData(GeneralHistoryDataFormat.Dust,dataFormat);
        String string =  "ST=51;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertFactorData("a34001",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Temperature,dataFormat);
        string += insertFactorData("a01001",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Humidity,dataFormat);
        string += insertFactorData("a01002",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Pressure,dataFormat);
        string += insertFactorData("a01006",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.WindForce,dataFormat);
        string += insertFactorData("a01007",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.WindDirection,dataFormat);
        string += insertFactorData("a01008",data[0],data[1],data[2]);
        data = getMinAvgMaxData(GeneralHistoryDataFormat.Noise,dataFormat);
        string += insertFactorData("a50001",data[0],data[1],data[2]);
        string += "&&";
        return string;

    }

    private String insertFactorData(String factor,float min,float avg,float max){
        String string = factor+"-Min="+tools.float2String3(min)+","+factor+"-Avg="+tools.float2String3(avg)+","+factor+"-Max="+tools.float2String3(max)+";";
        return string;
    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpStringWithoutMs(now)+";";
        return  "ST=51;CN=2011;PW=123456;MN="+mnCode+";CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String insertSensorData (float[] data){
        String string = "a34001-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+",a34001-Flag="+dustFlag+";";
        string += "a01001-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+",a01001-Flag=N;";
        string += "a01002-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+",a01002-Flag=N;";
        string += "a01006-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+",a01006-Flag=N;";
        string += "a01007-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+",a01007-Flag="+windSpeedFlag+";";
        string += "a01008-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+",a01008-Flag=N;";
        string += "a50001-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+",a50001-Flag=N";
        return string;
    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
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
        switch (alarm){
            case GeneralClientProtocol.AlARM_N:
                dustFlag = "N";
                break;
            case GeneralClientProtocol.ALARM_C:
                dustFlag = "C";
                break;
            case GeneralClientProtocol.ALARM_D:
                dustFlag = "D";
                break;
            case GeneralClientProtocol.ALARM_P:
                dustFlag = "P";
                break;
            case GeneralClientProtocol.ALARM_L:
                dustFlag = "L";
                break;
            case GeneralClientProtocol.ALARM_H:
                dustFlag = "H";
                break;
            case GeneralClientProtocol.ALARM_SUB:
                dustFlag = "-";
                break;
            case GeneralClientProtocol.ALARM_ADD:
                dustFlag = "+";
                break;
            case GeneralClientProtocol.ALARM_LESS_THAN:
                dustFlag = "<";
                break;
            case GeneralClientProtocol.ALARM_GREAT_THAN:
                dustFlag = ">";
                break;
            case GeneralClientProtocol.ALARM_S:
                windSpeedFlag = "S";
                break;
            case GeneralClientProtocol.ALARM_R:
                dustFlag = "R";
                break;
            case GeneralClientProtocol.ALARM_A:
                dustFlag = "A";
                break;
            default:

                break;
        }
    }

    @Override
    public String getRealTimeData(String qn) {
        long now = tools.nowtime2timestamp();
        return insertOneFrame(getRealTimeDataString(now));
    }

    @Override
    public List<String> getMinData(String qn, long begin, long end) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getHourData(String qn, long begin, long end) {
        return new ArrayList<>();
    }

    @Override
    public String getSystemResponse(String qn) {
        return insertOneFrame("ST=91;CN=9011;PW=123456;MN="+mnCode+";Flag=0;CP=&&QN="+qn+";QnRtn=1&&");
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
            dataBaseProtocol.setMinDataInterval(60000l);//设置为1分钟间隔
            ClientDataBaseCtrl dataBaseCtrl = ScanSensor.getInstance();
            heartRun = true;
            while (heartRun&&!interrupted()) {
                long now = tools.nowtime2timestamp();
                dataBaseCtrl.getRealTimeData(handler);
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
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
