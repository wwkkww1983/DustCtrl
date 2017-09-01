package com.grean.dustctrl;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.grean.dustctrl.process.ScanSensor;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Administrator on 2017/9/1.
 */

public class SystemLog implements Observer{
    private static final String tag = "SystemLog";
    private static Context context;
    private static SystemLog instance;
    private  SystemLog(){

    }
    public static SystemLog getInstance(Context con){
        context = con;
        if (instance == null){
            instance = new SystemLog();
        }
        return instance;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable.hashCode()== ScanSensor.getInstance().hashCode()){
            DbTask helperDbTask = new DbTask(context,1);
            SQLiteDatabase db = helperDbTask.getReadableDatabase();
            ContentValues values = new ContentValues();
            if(o.toString().equals("LogFormat")){
                LogFormat log = (LogFormat) o;
                values.put("date",log.getDate());

                values.put("content",log.getText());
                Log.d(tag,log.getText());
                db.insert("log",null,values);


            }

            db.close();
            helperDbTask.close();
        }
    }
}
