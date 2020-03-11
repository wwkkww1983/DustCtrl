package com.grean.dustctrl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.UploadingProtocol.UploadingConfigFormat;
import com.grean.dustctrl.device.DevicesManage;
import com.grean.dustctrl.process.SensorData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by weifeng on 2020/3/5.
 */

public class SystemSettingStore implements ReadWriteConfig{
    private static final String tag = "SystemSettingStore";
    private DbTask helper;
    SQLiteDatabase db;

    public SystemSettingStore (Context context){
        helper = new DbTask(context,4);
        db = helper.getReadableDatabase();
    }

    private void saveValuesToDatabase(ContentValues values){
        db.beginTransaction();
        try {
            db.update("device_setting", values,"factory_setting=?",new String[]{String.valueOf(1)});
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }

    public static void setDeviceSettingContent(DevicesManage manage,String string){
        try {
            JSONObject object  = new JSONObject(string);
            manage.setCameraDirectionOffset(object.getInt("camera_offset"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getDeviceSettingContent(DevicesManage manage){
        JSONObject object = new JSONObject();
        try {
            object.put("camera_offset",manage.getCameraOffset());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String getDefaultDeviceSettingContent(){
        JSONObject object = new JSONObject();
        try {
            object.put("camera_offset",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public void loadDeviceSetting(DevicesManage manage){
        Cursor cursor = db.rawQuery("SELECT * FROM device_setting",new String[]{});
        if(cursor.getCount() == 0){//无记录
            ContentValues values = new ContentValues();
            values.put("factory_setting",1);
            values.put("camera_name",0);
            values.put("dust_meter_name",0);
            values.put("led_display_name",0);
            values.put("noise_name",0);
            values.put("peripheral_name",0);
            values.put("weather_name",0);
            values.put("dust_name",0);
            values.put("dust_para_k",0.0012f);
            values.put("dust_para_b",0f);
            values.put("dust_alarm",0.3f);
            values.put("motor_rounds",1600);
            values.put("motor_time",2000);
            values.put("temp_para_k",1f);
            values.put("temp_para_b",0f);
            values.put("humi_para_k",1f);
            values.put("humi_para_b",0f);
            values.put("auto_calibration_enable",1);
            values.put("auto_calibration_date",1483200000000l);
            values.put("auto_calibration_interval",86400000l);
            values.put("ClientProtocol",0);
            values.put("content",getDefaultDeviceSettingContent());
            db.beginTransaction();
            try {
                db.insert("device_setting", null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {

            } finally {
                db.endTransaction();
            }
            manage.setCameraName(0);
            manage.setDustName(0);
            manage.setLedDisplayName(0);
            manage.setNoiseName(0);
            manage.setDustName(0);
            setDeviceSettingContent(manage,getDefaultDeviceSettingContent());
            SensorData data = manage.getData();
            data.setParaK(0.0012f);
            data.setParaB(0f);
            data.setDustAlarm(0.3f);
            data.setMotorRounds(1600);
            data.setMotorTime(2000);
            data.setParaTempSlope(1f);
            data.setParaTempIntercept(0f);
            data.setParaHumiSlope(1f);
            data.setParaHumiIntercept(0f);
        }else{
            cursor.moveToLast();
            manage.setCameraName(cursor.getInt(cursor.getColumnIndex("camera_name")));
            manage.setDustMeterName(cursor.getInt(cursor.getColumnIndex("dust_meter_name")));
            manage.setLedDisplayName(cursor.getInt(cursor.getColumnIndex("led_display_name")));
            manage.setNoiseName(cursor.getInt(cursor.getColumnIndex("noise_name")));
            manage.setDustName(cursor.getInt(cursor.getColumnIndex("dust_name")));
            setDeviceSettingContent(manage,cursor.getString(cursor.getColumnIndex("content")));
            SensorData data = manage.getData();
            data.setParaK(cursor.getFloat(cursor.getColumnIndex("dust_para_k")));
            data.setParaB(cursor.getFloat(cursor.getColumnIndex("dust_para_b")));
            data.setDustAlarm(cursor.getFloat(cursor.getColumnIndex("dust_alarm")));
            data.setMotorRounds(cursor.getInt(cursor.getColumnIndex("motor_rounds")));
            data.setMotorTime(cursor.getInt(cursor.getColumnIndex("motor_time")));
            data.setParaTempSlope(cursor.getFloat(cursor.getColumnIndex("temp_para_k")));
            data.setParaTempIntercept(cursor.getFloat(cursor.getColumnIndex("temp_para_b")));
            data.setParaHumiSlope(cursor.getFloat(cursor.getColumnIndex("humi_para_k")));
            data.setParaHumiIntercept(cursor.getFloat(cursor.getColumnIndex("humi_para_b")));
        }
        cursor.close();
    }

    public void loadUploadSetting(ProtocolTcpServer protocolTcpServer){
        Cursor cursor = db.rawQuery("SELECT * FROM upload_setting",new String[]{});
        UploadingConfigFormat format = new UploadingConfigFormat();
        if(cursor.getCount()==0){
            ContentValues values = new ContentValues();
            values.put("factory_setting",1 );
            values.put("last_min_date",1505923200000l );
            values.put("last_hour_date",1505923200000l );
            values.put("min_interval",300000l );
            try {
                values.put("content", UploadingConfigFormat.getDefaultConfig());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                format.loadConfig(UploadingConfigFormat.getDefaultConfig());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.beginTransaction();
            try {
                db.insert("upload_setting", null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {

            } finally {
                db.endTransaction();
            }

        }else{
            cursor.moveToLast();
            String string = cursor.getString(cursor.getColumnIndex("content"));
            try {
                format.loadConfig(string);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        protocolTcpServer.setConfig(format);
    }


    public void saveSetting(String name,int value){
        ContentValues values= new ContentValues();
        values.put(name,value);
        saveValuesToDatabase(values);
    }

    public void saveSetting(String name,float value){
        ContentValues values= new ContentValues();
        values.put(name,value);
        saveValuesToDatabase(values);
    }
    public void saveSetting(String name,String value){
        ContentValues values= new ContentValues();
        values.put(name,value);
        saveValuesToDatabase(values);
    }
    public void saveSetting(String name,long value){
        ContentValues values= new ContentValues();
        values.put(name,value);
        saveValuesToDatabase(values);
    }

    public int getSettingInt(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM device_setting",new String[]{});
        cursor.moveToLast();
        int data = cursor.getInt(cursor.getColumnIndex(name));
        cursor.close();
        return data;
    }

    public float getSettingFloat(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM device_setting",new String[]{});
        cursor.moveToLast();
        float data = cursor.getFloat(cursor.getColumnIndex(name));
        cursor.close();
        return data;
    }

    public long getSettingLong(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM device_setting",new String[]{});
        cursor.moveToLast();
        long data = cursor.getLong(cursor.getColumnIndex(name));
        cursor.close();
        return data;
    }

    public String getSettingString(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM device_setting",new String[]{});
        cursor.moveToLast();
        String data = cursor.getString(cursor.getColumnIndex(name));
        cursor.close();
        return data;
    }



    @Override
    public void saveConfig(String key, long data) {
        saveSetting(key,data);
    }

    @Override
    public void saveConfig(String key, int data) {
        saveSetting(key,data);
    }

    @Override
    public void saveConfig(String key, float data) {
        saveSetting(key,data);
    }

    @Override
    public void saveConfig(String key, String data) {
        saveSetting(key,data);
    }

    @Override
    public void saveConfig(String key, boolean data) {
        if(data) {
            saveSetting(key, 1);
        }else{
            saveSetting(key, 0);
        }
    }

    @Override
    public boolean getConfigBoolean(String key) {
        if(getSettingInt(key)==0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public float getConfigFloat(String key) {
        return getSettingFloat(key);
    }

    @Override
    public String getConfigString(String key) {
        return getSettingString(key);
    }

    @Override
    public long getConfigLong(String key) {
        return getSettingLong(key);
    }

    @Override
    public int getConfigInt(String key) {
        return getSettingInt(key);
    }

    @Override
    public void saveUploadSetting(String key) {
        ContentValues values = new ContentValues();
        values.put("content", key);

        db.beginTransaction();
        try {
            db.insert("upload_setting", null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void saveDeviceSetting(DevicesManage manage) {
        ContentValues values = new ContentValues();
        String string = getDeviceSettingContent(manage);
        values.put("content", string);
        saveValuesToDatabase(values);
    }
}
