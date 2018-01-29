package com.grean.dustctrl.protocol;

import android.util.Log;

import com.tools;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/1.
 */

public class TcpServer implements GeneralServerProtocol{
    private static final String tag = "TcpServer";
    private static final byte[] testFrame={'G','R','E','A','N','\n'};
    private List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();

    public TcpServer(){
        Map<String,Object>map = new HashMap<String,Object>();
        map.put("id",1);
        map.put("value",1.01);
        list.add(map);
        map = new HashMap<String,Object>();
        map.put("id",2);
        map.put("value",2.02);
        list.add(map);
        map = new HashMap<String,Object>();
        map.put("id",3);
        map.put("value",3.03);
        list.add(map);
    }
    @Override
    public byte[] handleProtocol(byte[] rec, int count) {
        String string = new String(rec,0,count);
        try {
            Log.d(tag, "server receive="+ string);
            return JSON.handleJsonString(string,GetProtocols.getInstance().getInfoProtocol());
        } catch (JSONException e) {
            Log.d(tag, "error server receive="+ string);
            e.printStackTrace();
        }
        return testFrame;
    }
}
