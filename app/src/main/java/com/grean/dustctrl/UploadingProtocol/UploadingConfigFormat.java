package com.grean.dustctrl.UploadingProtocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 上传协议配置格式
 * Created by weifeng on 2018/6/27.
 */

public class UploadingConfigFormat implements Cloneable {
    private String serverAddress,mnCode,password,alarmTarget,backupServerAddress,backupMnCode;
    private int serverPort,timeoutLimit,timeoutRepetition,realTimeInterval,warnTime,backupServerPort;
    private double lng,lat;//经纬度
    private HashMap<String,Integer> factorMap = new HashMap<>();//因子，数据库位置

    public void loadConfig(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        serverAddress = object.getString("ServerAddress");
        mnCode = object.getString("MnCode");
        password = object.getString("Password");
        if(object.has("BackupServerAddress")){
            backupServerAddress = object.getString("BackupServerAddress");
        }else{
            backupServerAddress = "117.149.30.221";
        }
        if(object.has("BackupServerMnCode")) {
            backupMnCode = object.getString("BackupServerMnCode");
        }else{
            backupMnCode =  "3301000001";
        }
        if(object.has("BackupServerPort")) {
            backupServerPort = object.getInt("BackupServerPort");
        }else{
            backupServerPort = 210;
        }
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
        object.put("ServerAddress","39.107.94.109");
        object.put("MnCode","LJHJ0HZ0010005");
        object.put("Password","123456");
        object.put("ServerPort",9998);
        object.put("TimeoutLimit",5);
        object.put("RealTimeInterval",3);
        object.put("Lng",0);
        object.put("Lat",0);
        object.put("WarnTime",5);
        object.put("AlarmTarget","3882566");
        object.put("BackupServerAddress","117.149.30.221");
        object.put("BackupServerMnCode","3301000001");
        object.put("BackupServerPort",210);
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
        object.put("BackupServerAddress",backupServerAddress);
        object.put("BackupServerMnCode",backupMnCode);
        object.put("BackupServerPort",backupServerPort);
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

    /**
     * 深拷贝
     * @return
     */
    @Override
    protected Object clone()  {
        UploadingConfigFormat format = null;
        try {
            format = (UploadingConfigFormat) super.clone();
        }catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return format;

    }

    public String getBackupServerAddress() {
        return backupServerAddress;
    }

    public void setBackupServerAddress(String backupServerAddress) {
        this.backupServerAddress = backupServerAddress;
    }

    public String getBackupMnCode() {
        return backupMnCode;
    }

    public void setBackupMnCode(String backupMnCode) {
        this.backupMnCode = backupMnCode;
    }

    public int getBackupServerPort() {
        return backupServerPort;
    }

    public void setBackupServerPort(int backupServerPort) {
        this.backupServerPort = backupServerPort;
    }
}
