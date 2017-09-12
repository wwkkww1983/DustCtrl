package com.grean.dustctrl.protocol;

import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * 处理JSON数据数据格式
 * 实时数据
 * 收到{"protocolType":"realTimeData"，}
 * 返回{"protocolType":"realTimeData","state":"string","realTimeData"[{"name":"dust","value":"1.0"},{"name":"temperature","value":"1.0"},{"name":"humidity","value":"1.0"},
 * {"name":"pressure","value":"1.0"},{"name":"windForce","value":"1.0"},{"name":"windDirection","value":"1.0"},{"name":"noise","value":"1.0"},
 * {"name":"value","value":"1.0"}] }
 * 下载设置，从控制器下载当前设置
 * {"protocolType":"downloadSetting"}
 * 上传设置设置，从现实终端
 * {"protocolType":"uploadSetting"}
 * 单步操作
 * {"protocolType":"operate"}
 * 历史数据
 * {"protocolType":"historyData"}
 * 日志
 * {"protocolType":"log"}
 *
 * Created by weifeng on 2017/9/5.
 */

public class JSON {
    //private static final String tag="JSON";

    public static byte[] createJsonObject(int total,boolean success, List<Map<String,Object>> list) throws JSONException {
        Map<String,Object> map;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total",total);
        jsonObject.put("success",success);
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<list.size();i++){
            JSONObject item = new JSONObject();
            map = list.get(i);
            item.put("id", map.get("id"));
            item.put("value",map.get("value"));
            jsonArray.put(item);
        }
        jsonObject.put("arrayData",jsonArray);
        return jsonObject.toString().getBytes();
    }

    private static JSONObject putItem(String name,float data) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("name",name);
        item.put("value",data);
        return item;
    }

    private static byte[] handleUploadSetting(JSONObject jsonObject,GeneralInfoProtocol infoProtocol) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("protocolType","uploadSetting");

        boolean enable = jsonObject.getBoolean("autoCalEnable");
        long date = jsonObject.getLong("autoCalTime");
        long interval = jsonObject.getLong("autoCalInterval");
        object.put("success",infoProtocol.setAutoCal(enable,date,interval));
        infoProtocol.setServer(jsonObject.getString("serverIp"),jsonObject.getInt("serverPort"));
        return object.toString().getBytes();
    }

    private static byte[] handleDownloadSetting(GeneralInfoProtocol infoProtocol) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("protocolType","downloadSetting");
        object.put("autoCalEnable",infoProtocol.getAutoCalEnable());
        object.put("autoCalTime",infoProtocol.getAutoCalTime());
        object.put("autoCalInterval",infoProtocol.getAutoCalInterval());
        object.put("serverIp",infoProtocol.getServerIp());
        object.put("serverPort",infoProtocol.getServerPort());
        object.put("dustParaK",infoProtocol.getParaK());
        return object.toString().getBytes();
    }

    private static byte[] handleOperate(JSONObject jsonObject,GeneralInfoProtocol infoProtocol) throws JSONException {
        if(jsonObject.getBoolean("DustCal")){

        }

        if(jsonObject.getBoolean("DustMeterCal")){

        }

        if(jsonObject.getBoolean("DustMeterCalResult")){

        }

        if(jsonObject.getBoolean("DustMeterInfo")){//获取粉尘仪信息

        }

        if(jsonObject.getBoolean("DustMeterCalProcess")){

        }

        JSONObject object = new JSONObject();
        object.put("protocolType","operate");
        return object.toString().getBytes();
    }

    private static byte[] handleRealTimeData(GeneralInfoProtocol infoProtocol) throws JSONException {
        SensorData data = infoProtocol.getSensorData();
        JSONObject object = new JSONObject();
        object.put("protocolType","realTimeData");
        object.put("state",infoProtocol.getSystemState());
        JSONArray array = new JSONArray();
        array.put(putItem("dust",data.getDust()));
        array.put(putItem("temperature",data.getAirTemperature()));
        array.put(putItem("humidity",data.getAirHumidity()));
        array.put(putItem("pressure",data.getAirPressure()));
        array.put(putItem("windForce",data.getWindForce()));
        array.put(putItem("windDirection",data.getWindDirection()));
        array.put(putItem("noise",data.getNoise()));
        array.put(putItem("value",data.getValue()));
        object.put("realTimeData",array);
        return object.toString().getBytes();
    }

    /**
     * 处理接收的JSO数组
     * @param string
     * @return
     */
    public static byte[] handleJsonString(String string,GeneralInfoProtocol infoProtocol) throws JSONException {
        JSONObject jsonObject = new JSONObject(string);
        if (jsonObject.getString("protocolType").equals("realTimeData")){
            return handleRealTimeData(infoProtocol);
        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleDownloadSetting(infoProtocol);

        }else if(jsonObject.getString("protocolType").equals("uploadSetting")){
            return handleUploadSetting(jsonObject,infoProtocol);

        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleRealTimeData(infoProtocol);

        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleRealTimeData(infoProtocol);

        }else {
            JSONObject object = new JSONObject();
            object.put("protocolType","error");
            return object.toString().getBytes();
        }
    }
    /**
     *数据形式：{"total":2,"success":true,"arrayData":[{"id":1,"value",123.53}]
     *
     *}
     * @param rec 接收字节流
     * @param count 接收字节数
     * @return
     * @throws JSONException
     */
    public static List<Map<String,Object>>getJsonObject(byte[] rec,int count) throws JSONException {
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        Map<String,Object> map;
        String json = new String(rec,0,count);
        JSONObject jsonObject = new JSONObject(json);
        int total = jsonObject.getInt("total");
        boolean success = (boolean) jsonObject.get("success");
        JSONArray jsonArray = jsonObject.getJSONArray("arrayData");
        for(int i=0;i<jsonArray.length();i++){
            JSONObject item = jsonArray.getJSONObject(i);
            int id = item.getInt("id");
            Double value = item.getDouble("value");
            map = new HashMap<String,Object>();
            map.put("id",id);
            map.put("value",value);
            list.add(map);
        }

        return list;

    }
}
