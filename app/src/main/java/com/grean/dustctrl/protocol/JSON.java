package com.grean.dustctrl.protocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
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
