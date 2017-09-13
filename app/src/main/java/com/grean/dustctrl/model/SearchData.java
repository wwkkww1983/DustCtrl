package com.grean.dustctrl.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;

import com.grean.dustctrl.DbTask;
import com.grean.dustctrl.presenter.InsertString;
import com.grean.dustctrl.presenter.NotifyDataInfo;
import com.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/30.
 */

public class SearchData {
    private static final String[] name ={"时间","扬尘 mg/Lm/m³","温度 ℃","湿度 %","大气压 hPa","风速 m/s","风向 °","噪声 d"};
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
            data[7] = String.valueOf(cursor.getFloat(8));
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

    private ArrayList<String> exportData(long start,long end){
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
            String string = tools.timestamp2string(cursor.getLong(0));
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

    public void exportData(long start,long end,NotifyDataInfo dataInfo){
        new ExportDataThread(start,end,dataInfo).start();
    }

    private class ExportDataThread extends Thread{
        long start,end;
        NotifyDataInfo dataInfo;
        public ExportDataThread(long start, long  end, NotifyDataInfo dataInfo){
            this.start = start;
            this.end = end;
            this.dataInfo = dataInfo;
        }

        @Override
        public void run() {
            boolean success=true;
            String pathName = "/nmt/usbhost/Storage01/GREAN/"; // /storage/sdcard0/GREAN/
            String fileName = "数据"+tools.nowtime2string()+"导出.txt";
            File path = new File(pathName);
            File file = new File(pathName + fileName);
            try {
                if (!path.exists()) {
                    Log.d("TestFile", "Create the path:" + pathName);
                    path.mkdir();
                }
                if (!file.exists()) {
                    Log.d("TestFile", "Create the file:" + fileName);
                    file.createNewFile();
                }

                // 导出日志
                BufferedWriter bw = new BufferedWriter(new FileWriter(file,false)); // true// 是添加在后面// false// 是每次写新的
                bw.write("历史数据 \r\n");


                ArrayList<String> list = exportData(start,end);
                for (String tmp : list) {
                    bw.write(tmp + "\r\n");
                    //Log.d("写入SD", tmp);
                }
                bw.flush();
                bw.close();


            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dataInfo.exportDataResult(success);

        }
    }



}
