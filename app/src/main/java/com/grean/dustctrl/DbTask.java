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
        //V0.0-->V2.0
        sqLiteDatabase.execSQL("CREATE TABLE result (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT," +
                "dust_1 FLOAT,value_1 FLOAT,dust_2 FLOAT,value_2 FLOAT)");//结果
        //V0.0-->V1.0 sqLiteDatabase.execSQL("CREATE TABLE result (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT)");//结果
        sqLiteDatabase.execSQL("CREATE TABLE detail (date LONG ,acin BOOLEAN,batterylow BOOLEAN,hitemp FLOAT,lotemp FLOAT,hihumidity FLOAT,lohumidity FLOAT,pwm INTEGER)");//辅助参数
        sqLiteDatabase.execSQL("CREATE TABLE log(id INTEGER PRIMARY KEY AUTOINCREMENT,date LONG,content TEXT)");//日志
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //v1.0-->v2.0
        if((i==1)&&(i1==2)){
            sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_1 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD dust_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE result ADD value_2 FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD block_pos BOOLEAN");//滑块位置
            sqLiteDatabase.execSQL("ALTER TABLE detail ADD pipe_temp FLOAT");//采样管温度
            Log.d("DbTask","升级数据库 v1->v2");
        }
    }


}
