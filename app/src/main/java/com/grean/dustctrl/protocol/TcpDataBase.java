package com.grean.dustctrl.protocol;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.myApplication;
import com.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by weifeng on 2017/9/21.
 */

public class TcpDataBase implements GeneralDataBaseProtocol{
    private static final String tag = "TcpDataBase";
    private Context context;
    long lastMinDate,minInterval=300000l,nextMinDate;

    public TcpDataBase (Context context){
        this.context = context;
    }

    private ArrayList<String> exportDataBase(long start, long end){
        ArrayList<String> list = new ArrayList<String>();
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
        list.add("时间  TSP mg/m³ 温度 ℃ 湿度 % 气压 hPa 风速 m/s 风向 ° 噪声 dB");
        while (cursor.moveToNext()){
            String string = tools.timestamp2string(cursor.getLong(0))+"  ";
            string+=tools.float2String3(cursor.getFloat(1))+"  ";
            string+=tools.float2String3(cursor.getFloat(3))+"  ";
            string+=tools.float2String3(cursor.getFloat(4))+"  ";
            string+=tools.float2String3(cursor.getFloat(5))+"  ";
            string+=tools.float2String3(cursor.getFloat(6))+"  ";
            string+=tools.float2String3(cursor.getFloat(7))+"  ";
            string+=tools.float2String3(cursor.getFloat(8))+"  ";
            list.add(string);
        }
        db.close();
        helperDbTask.close();
        return list;
    }


    @Override
    public boolean exportData2File(long start, long  end,ExportDataProcessListener listener) {
        boolean exportDataResult=true;
        String pathName = "/mnt/usbhost/Storage01/GREAN/"; // /storage/sdcard0/GREAN/
        String fileName = "数据"+tools.nowTime2FileString()+"导出.txt";
        File path = new File(pathName);
        File file = new File(pathName + fileName);
        try {
            if (!path.exists()) {
                //Log.d("TestFile", "Create the path:" + pathName);
                path.mkdir();
            }
            if (!file.exists()) {
                // Log.d("TestFile", "Create the file:" + fileName);
                file.createNewFile();
            }
            if(listener!=null) {
                listener.setProcess(10);
            }
            // 导出日志
            BufferedWriter bw = new BufferedWriter(new FileWriter(file,false)); // true// 是添加在后面// false// 是每次写新的
            bw.write("历史数据 \r\n");
            ArrayList<String> list = exportDataBase(start,end);
            for (String tmp : list) {
                bw.write(tmp + "\r\n");
                //Log.d("写入SD", tmp);
            }
            bw.flush();
            bw.close();
            if(listener!=null) {
                listener.setProcess(80);
            }
        } catch (IOException e) {
            e.printStackTrace();
            exportDataResult = false;
        }
        return exportDataResult;
    }

    @Override
    public GeneralHistoryDataFormat getHourData(long dateStart) {
        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >"+ String.valueOf(dateStart)+" and date <"+String.valueOf(dateStart + 3600000l);
        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date asc",new String[]{});
        int index = 0;
        ArrayList<Float> item;
        while ((cursor.moveToNext())&&index < 100){
            format.addDate(cursor.getLong(0));
            item = new ArrayList<Float>();
            item.add(cursor.getFloat(1));
            item.add(cursor.getFloat(3));
            item.add(cursor.getFloat(4));
            item.add(cursor.getFloat(5));
            item.add(cursor.getFloat(6));
            item.add(cursor.getFloat(7));
            item.add(cursor.getFloat(8));
            format.addItem(item);
            index++;
        }
        db.close();
        helperDbTask.close();
        return format;
    }

    @Override
    public ArrayList<String> getDayLog(long endDate) {
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        statement = "date >"+ String.valueOf(endDate - 3600000l*24)+" and date <"+String.valueOf(endDate);

        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM log WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index=0;
        while ((cursor.moveToNext())&&(index < 100)){
            list.add(cursor.getString(2));
            index++;
        }
        db.close();
        helperDbTask.close();
        return list;
    }

    @Override
    public GeneralHistoryDataFormat getData(long start, long end) {
        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >"+ String.valueOf(start)+" and date <"+String.valueOf(end);
        DbTask helperDbTask = new DbTask(context,1);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result WHERE "+statement+" ORDER BY date asc",new String[]{});
        int index = 0;
        ArrayList<Float> item;
        while ((cursor.moveToNext())&&index < 100){
            format.addDate(cursor.getLong(0));
            item = new ArrayList<Float>();
            item.add(cursor.getFloat(1));
            item.add(cursor.getFloat(3));
            item.add(cursor.getFloat(4));
            item.add(cursor.getFloat(5));
            item.add(cursor.getFloat(6));
            item.add(cursor.getFloat(7));
            item.add(cursor.getFloat(8));
            format.addItem(item);
            index++;
        }
        db.close();
        helperDbTask.close();
        return format;
    }

    @Override
    public long getLastMinDate() {
        return lastMinDate;
    }

    @Override
    public long getNextMinDate() {
        return  nextMinDate;
    }

    @Override
    public void setMinDataInterval(long min) {
        this.minInterval = min;
        myApplication.getInstance().saveConfig("MinInterval",min);
    }

    @Override
    public long calcNextMinDate(long now) {
       // Log.d(tag,"now="+tools.timestamp2string(now)+";plan="+tools.timestamp2string(lastMinDate)+";interval = "+String.valueOf(minInterval/1000l));
        lastMinDate = nextMinDate;
        nextMinDate = tools.calcNextTime(now,lastMinDate,minInterval);
        myApplication.getInstance().saveConfig("LastMinDate",lastMinDate);
        return nextMinDate;
    }

    @Override
    public void loadMinDate() {
        lastMinDate = myApplication.getInstance().getConfigLong("LastMinDate");
        minInterval = myApplication.getInstance().getConfigLong("MinInterval");
        if(lastMinDate == 0l){
            lastMinDate = 1505923200000l;
        }
        if(minInterval == 0l){
            minInterval = 300000l;
        }
        nextMinDate = lastMinDate + minInterval;

       // Log.d(tag,"next ="+tools.timestamp2string(nextMinDate)+";plan="+tools.timestamp2string(lastMinDate)+";interval = "+String.valueOf(minInterval/1000l));
    }
}
