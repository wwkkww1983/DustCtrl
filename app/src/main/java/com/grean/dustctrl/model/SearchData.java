package com.grean.dustctrl.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.params.StreamConfigurationMap;

import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.presenter.InsertString;
import com.tools;

/**
 * Created by Administrator on 2017/8/30.
 */

public class SearchData {
    private static final String[] name ={"时间","扬尘 mg/L","温度 ℃","湿度 %","大气压 hPa","风力 级","风向 °","噪声 db"};
    private InsertString insert;
    private Context context;

    public SearchData(InsertString insertString,Context context){
        insert = insertString;
        this.context = context;
    }

    public void initTitle(){
        insert.insertStrings(name);
    }

    public void searchLog(long start,long end){
        String  string;
        String statement;
        if (start > end){
            statement = "date <"+ String.valueOf(start)+" and date >"+String.valueOf(end);
        }else{
            statement = "date >"+ String.valueOf(start)+" and date <"+String.valueOf(end);
        }

        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM log WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index=0;
        while ((cursor.moveToNext())&&(index < 100)){
            string = cursor.getString(2);
            insert.insertLog(string);
            index++;
        }
        db.close();
        helperDbTask.close();
    }

    /**
     * 搜索数据
     * @param start 起始时间戳
     * @param end 终止时间戳
     */
    public void searchData(long start , long end){
        String [] data=new String[8];
        String statement;
        if (start > end){
            statement = "date <"+ String.valueOf(start)+" and date >"+String.valueOf(end);
        }else{
            statement = "date >"+ String.valueOf(start)+" and date <"+String.valueOf(end);
        }

        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index = 0;
        while ((cursor.moveToNext())&&index < 100){
            data[0] = tools.timestamp2string(cursor.getLong(0));
            data[1] = String.valueOf(cursor.getFloat(1));
            data[2] = String.valueOf(cursor.getFloat(3));
            data[3] = String.valueOf(cursor.getFloat(4));
            data[4] = String.valueOf(cursor.getFloat(5));
            data[5] = String.valueOf(cursor.getFloat(6));
            data[6] = String.valueOf(cursor.getFloat(7));
            data[7] = String.valueOf(cursor.getFloat(2));
            insert.insertStrings(data);
            index++;
        }
        db.close();
        helperDbTask.close();

    }

    public void searchData(String startString,String endString){
        long start = tools.string2timestamp(startString),end = tools.string2timestamp(endString);
        insert.clearAll();
        searchData(start,end);
    }

    public void searchLog(String startString,String endString){
        long start = tools.string2timestamp(startString),end = tools.string2timestamp(endString);
        insert.clearContent();
        searchLog(start,end);
    }

}
