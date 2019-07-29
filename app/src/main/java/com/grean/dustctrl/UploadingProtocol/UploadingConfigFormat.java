package com.grean.dustctrl.UploadingProtocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 上传协议配置格式
 * Created by weifeng on 2018/6/27.
 */

public class UploadingConfigFormat implements Cloneable {
    private String serverAddress,mnCode,password,alarmTarget;
    private int serverPort,timeoutLimit,timeoutRepetition,realTimeInterval,warnTime;
    private double lng,lat;//经纬度
    private HashMap<String,Integer> factorMap = new HashMap<>();//因子，数据库位置

    public void loadConfig(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        serverAddress = object.getString("ServerAddress");
        mnCode = object.getString("MnCode");
        password = object.getString("Password");
        serverPort = object.getInt("ServerPort");
        timeoutLimit = object.getInt("TimeoutLimit");
        timeoutRepetition = object.getInt("TimeoutRepetition");
        realTimeInterval = object.getInt("RealTimeInterval");
        lng = object.getDouble("Lng");
        lat = object.getDouble("Lat");
        if(object.has("WarnTime")){
            warnTime = object.getInt("WarnTime");
        }else{
            warnTime = 5;
        }
        if(object.has("AlarmTarget")){
            alarmTarget = object.getString("AlarmTarget");
        }else{
            alarmTarget = "3882566";
        }
    }

    public static String getDefaultConfig() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("ServerAddress","117.149.30.221");
        object.put("MnCode","3301000005");
        object.put("Password","123456");
        object.put("ServerPort",203);
        object.put("TimeoutLimit",5);
        object.put("RealTimeInterval",3);
        object.put("Lng",0);
        object.put("Lat",0);
        object.put("WarnTime",5);
        object.put("AlarmTarget","3882566");
        return object.toString();
    }

    public String getConfigString() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("ServerAddress",serverAddress);
        object.put("MnCode",mnCode);
        object.put("Password",password);
        object.put("ServerPort",serverPort);
        object.put("TimeoutLimit",timeoutLimit);
        object.put("TimeoutRepetition",timeoutRepetition);
        object.put("RealTimeInterval",realTimeInterval);
        object.put("Lng",lng);
        object.put("Lat",lat);
        object.put("WarnTime",warnTime);
        object.put("AlarmTarget",alarmTarget);
        return object.toString();
    }

    public HashMap<String, Integer> getFactorMap() {
        return factorMap;
    }
/*遍历hashMap
* 　　Map map = new HashMap();
　　Iterator iter = map.entrySet().iterator();
　　while (iter.hasNext()) {
　　Map.Entry entry = (Map.Entry) iter.next();
　　Object key = entry.getKey();
　　Object val = entry.getValue();
　　}
* */
    public void addFactor(String factor,int dataBasePos) {
        factorMap.put(factor,dataBasePos);

    }

    public int getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(int warnTime) {
        this.warnTime = warnTime;
    }

    public String getPassword() {
        return password;
    }

    public String getAlarmTarget() {
        return alarmTarget;
    }

    public void setAlarmTarget(String alarmTarget) {
        this.alarmTarget = alarmTarget;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getMnCode() {
        return mnCode;
    }

    public void setMnCode(String mnCode) {
        this.mnCode = mnCode;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getTimeoutLimit() {
        return timeoutLimit;
    }

    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    public int getTimeoutRepetition() {
        return timeoutRepetition;
    }

    public void setTimeoutRepetition(int timeoutRepetition) {
        this.timeoutRepetition = timeoutRepetition;
    }

    public int getRealTimeInterval() {
        return realTimeInterval;
    }

    public void setRealTimeInterval(int realTimeInterval) {
        this.realTimeInterval = realTimeInterval;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
