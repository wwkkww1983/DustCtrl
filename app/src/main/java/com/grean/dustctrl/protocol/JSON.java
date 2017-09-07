package com.grean.dustctrl.protocol;

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
 * 返回{"protocolType":"realTimeData","realTimeData"[{"name":"dust","value":"1.0"},{"name":"temperature","value":"1.0"},{"name":"humidity","value":"1.0"},
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
        item.put("name","dust");
        item.put("value",data);
        return item;
    }

    private static byte[] handleRealTimeData(GeneralRealTimeDataProtocol realTimeDataProtocol) throws JSONException {
        SensorData data = realTimeDataProtocol.getRealTimeData();
        JSONObject object = new JSONObject();
        object.put("protocolType","realTimeData");
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
    public byte[] handleJsonString(String string) throws JSONException {
        JSONObject jsonObject = new JSONObject(string);
        if (jsonObject.getString("protocolType").equals("realTimeData")){
            return handleRealTimeData(ScanSensor.getInstance());
        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleRealTimeData(ScanSensor.getInstance());

        }else if(jsonObject.getString("protocolType").equals("uploadSetting")){
            return handleRealTimeData(ScanSensor.getInstance());

        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleRealTimeData(ScanSensor.getInstance());

        }else if(jsonObject.getString("protocolType").equals("downloadSetting")){
            return handleRealTimeData(ScanSensor.getInstance());

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
