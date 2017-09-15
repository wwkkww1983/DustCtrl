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
 *导出数据
 * {"protocolType","exportData"}
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
        JSONObject object = new JSONObject();
        object.put("protocolType","operate");

        if(jsonObject.has("DustCal")){//校准斜率
            infoProtocol.calDust((float) jsonObject.getDouble("target"));
            object.put("DustCal",true);
            object.put("ParaK",infoProtocol.getParaK());
        }else if(jsonObject.has("DustMeterCal")){//粉尘仪校零，校跨
            object.put("DustMeterCal",true);
            infoProtocol.calDustMeter();
        }else if(jsonObject.has("DustMeterCalResult")){//查询校零校跨结果
            object.put("DustMeterCalResult",true);
            object.put("DustMeterCalBg",infoProtocol.getDustMeterBg());
            object.put("DustMeterCalSpan",infoProtocol.getDustMeterSpan());
        }else if(jsonObject.has("DustMeterInfo")){//获取粉尘仪信息
            infoProtocol.inquireDustMeterInfo();
            object.put("DustMeterInfo",true);
            object.put("DustMeterPumpTime",infoProtocol.getDustMeterPumpTime());
            object.put("DustMeterLaserTime",infoProtocol.getDustMeterLaserTime());
        }else if(jsonObject.has("DustMeterCalProcess")){//查询校零校跨进度
            object.put("DustMeterCalProcess",true);
            object.put("DustMeterCalProcessInt",infoProtocol.getDustMeterCalProcess());
            object.put("DustMeterCalInfo",infoProtocol.getSystemState());
        }else if(jsonObject.has("ExportData")){
            object.put("ExportData",true);
            object.put("process",0);
            object.put("result",false);
            infoProtocol.exportData(jsonObject.getLong("start"),jsonObject.getLong("end"));
        }else if(jsonObject.has("ExportDataProcess")){
            object.put("ExportDataProcess",true);
            object.put("process",infoProtocol.getExportDataProcess());
            object.put("result",infoProtocol.getExportDataResult());
        }else{
            object.put("ErrorCommand",true);
        }
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

    private static byte[] handleLog(JSONObject jsonObject,GeneralInfoProtocol infoProtocol) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("protocolType","log");
        ArrayList<String> list = infoProtocol.getLog(jsonObject.getLong("Date"));
        JSONArray array = new JSONArray();
        for(int i=0;i<list.size();i++){
            String string = list.get(i);
            array.put(string);
        }
        object.put("ArrayData",array);
        return object.toString().getBytes();
    }

    private static byte[] handleHistoryData(JSONObject jsonObject,GeneralInfoProtocol infoProtocol) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("protocolType","historyData");
        GeneralHistoryDataFormat format = infoProtocol.getHistoryData(jsonObject.getLong("Date"));
        int size = format.getSize();
        object.put("DateSize",size);
        JSONArray array = new JSONArray();
        ArrayList<Float> itemData;
        for(int i=0;i<size;i++){
            itemData = format.getItem(i);
            long date = format.getDate(i);
            JSONObject item = new JSONObject();
            item.put("date",date);
            item.put("dust",itemData.get(0));
            item.put("temperature",itemData.get(1));
            item.put("humidity",itemData.get(2));
            item.put("pressure",itemData.get(3));
            item.put("windForce",itemData.get(4));
            item.put("windDirection",itemData.get(5));
            item.put("noise",itemData.get(6));
            array.put(item);
        }
        object.put("ArrayData",array);
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

        }else if(jsonObject.getString("protocolType").equals("operate")){
            return handleOperate(jsonObject,infoProtocol);

        }else if(jsonObject.getString("protocolType").equals("historyData")){
            return handleHistoryData(jsonObject,infoProtocol);

        }else if(jsonObject.getString("protocolType").equals("log")){
            return handleLog(jsonObject,infoProtocol);
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
