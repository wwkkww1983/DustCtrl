package com.grean.dustctrl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        sqLiteDatabase.execSQL("CREATE TABLE result (date LONG,dust FLOAT,value FLOAT,temperature FLOAT,humidity FLOAT,pressure FLOAT,windforce FLOAT,winddirection FLOAT,noise FLOAT)");//结果
        sqLiteDatabase.execSQL("CREATE TABLE detail (date LONG ,acin BOOLEAN,batterylow BOOLEAN,hitemp FLOAT,lotemp FLOAT,hihumidity FLOAT,lohumidity FLOAT,pwm INTEGER)");//辅助参数
        sqLiteDatabase.execSQL("CREATE TABLE log(id INTEGER PRIMARY KEY AUTOINCREMENT,date LONG,content TEXT)");//日志
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
