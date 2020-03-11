package com.grean.dustctrl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * 处理数据库方法
 * Created by Administrator on 2017/8/25.
 */

public class DbTask extends SQLiteOpenHelper{
    public DbTask(Context context, int version){
        super(context,"data.db",null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //V0.0-->V3.0
        sqLiteDatabase.execSQL("CREATE TABLE result_hour (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT," +
                "dust_1 FLOAT,value_1 FLOAT,dust_2 FLOAT,value_2 FLOAT)");
        //V0.0-->V2.0
        sqLiteDatabase.execSQL("CREATE TABLE result (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT," +
                "dust_1 FLOAT,value_1 FLOAT,dust_2 FLOAT,value_2 FLOAT)");//结果
        //V0.0-->V1.0 sqLiteDatabase.execSQL("CREATE TABLE result (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT)");//结果
        sqLiteDatabase.execSQL("CREATE TABLE detail (date LONG ,acin BOOLEAN,batterylow BOOLEAN,hitemp FLOAT,lotemp FLOAT,hihumidity FLOAT,lohumidity FLOAT,pwm INTEGER)");//辅助参数
        sqLiteDatabase.execSQL("CREATE TABLE log(id INTEGER PRIMARY KEY AUTOINCREMENT,date LONG,content TEXT)");//日志
        //V0.0-->V4.0
        sqLiteDatabase.execSQL("CREATE TABLE device_setting(factory_setting INTEGER,camera_name INTEGER ,dust_meter_name INTEGER ,led_display_name INTEGER,noise_name INTEGER,peripheral_name INTEGER,weather_name INTEGER," +
                "dust_name INTEGER,dust_para_k FLOAT,dust_para_b FLOAT,dust_alarm FLOAT,motor_rounds INTEGER,motor_time INTEGER," +
                "temp_para_k FLOAT,temp_para_b FLOAT,humi_para_k FLOAT,humi_para_b FLOAT,auto_calibration_enable INTEGER,auto_calibration_date LONG,auto_calibration_interval LONG," +
                "ClientProtocol INTEGER, content TEXT)");//设置
        sqLiteDatabase.execSQL("CREATE TABLE upload_setting(factory_setting INTEGER,last_min_date LONG ,last_hour_date LONG ,min_interval LONG," +
                "content TEXT)");//网络设置

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //v1.0-->v2.0
        Log.d("DbTask","升级数据库"+String.valueOf(i)+"-"+String.valueOf(i1));
        if((i==1)&&(i1==2)){
            sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD block_pos BOOLEAN");//滑块位置
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD pipe_temp FLOAT");//采样管温度
            Log.d("DbTask","升级数据库 v1->v2");
        }

        if((i==2)&&(i1==3)){
            sqLiteDatabase.execSQL("CREATE TABLE result_hour (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT," +
                    "dust_1 FLOAT,value_1 FLOAT,dust_2 FLOAT,value_2 FLOAT)");
            Log.d("DbTask","数据库升级v2->v3");
        }

        if((i==1)&&(i1==3)){
            /*sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD block_pos BOOLEAN");//滑块位置
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD pipe_temp FLOAT");//采样管温度*/
            sqLiteDatabase.execSQL("CREATE TABLE result_hour (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT," +
                    "dust_1 FLOAT,value_1 FLOAT,dust_2 FLOAT,value_2 FLOAT)");
            Log.d("DbTask","数据库升级v1->v3");
        }

        if(i1==4){
            sqLiteDatabase.execSQL("CREATE TABLE device_setting(factory_setting INTEGER,camera_name INTEGER ,dust_meter_name INTEGER ,led_display_name INTEGER,noise_name INTEGER,peripheral_name INTEGER,weather_name INTEGER," +
                    "dust_name INTEGER,dust_para_k FLOAT,dust_para_b FLOAT,dust_alarm FLOAT,motor_rounds INTEGER,motor_time INTEGER," +
                    "temp_para_k FLOAT,temp_para_b FLOAT,humi_para_k FLOAT,humi_para_b FLOAT,auto_calibration_enable INTEGER,auto_calibration_date LONG,auto_calibration_interval LONG," +
                    "ClientProtocol INTEGER, content TEXT)");//设置
            sqLiteDatabase.execSQL("CREATE TABLE upload_setting(factory_setting INTEGER,last_min_date LONG ,last_hour_date LONG ,min_interval LONG," +
                    "content TEXT)");//网络设置
            Log.d("DbTask","数据库升级v1,2,3->v4");
        }
    }

}
