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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by weifeng on 2017/9/21.
 */

public class TcpDataBase implements GeneralDataBaseProtocol{
    public static final int ResultMin=0,ResultHour=1;
    private static final String tag = "TcpDataBase";
    private Context context;
    long lastMinDate,minInterval=60000l,nextMinDate,lastHourDate,nextHourDate;

    public TcpDataBase (Context context){
        this.context = context;
    }

    private ArrayList<HistoryDataFormat> exportDataFormat(int tableName,long start,long end){
        ArrayList<HistoryDataFormat> list = new ArrayList<>();
        String statement;
        if (start > end){
            statement = "date <"+ String.valueOf(start)+" and date >="+String.valueOf(end);
        }else{
            statement = "date >="+ String.valueOf(start)+" and date <"+String.valueOf(end);
        }
        DbTask helperDbTask = new DbTask(context,3);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        if(tableName == ResultHour) {
            cursor = db.rawQuery("SELECT * FROM result_hour WHERE " + statement + " ORDER BY date desc", new String[]{});
        }else if(tableName == ResultMin){
            cursor = db.rawQuery("SELECT * FROM result WHERE " + statement + " ORDER BY date desc", new String[]{});
        }else{
            cursor = db.rawQuery("SELECT * FROM result WHERE " + statement + " ORDER BY date desc", new String[]{});
        }
        HistoryDataFormat format;
        long date;
        float[] data = new float[7];
        while (cursor.moveToNext()){
            date = cursor.getLong(0);
            data[0] = cursor.getFloat(1);
            for(int i = 1;i<7;i++){
                data[i] = cursor.getFloat(i+2);
            }
            format = new HistoryDataFormat(date,data);
            list.add(format);
        }
        cursor.close();
        db.close();
        helperDbTask.close();
        return list;

    }

    private ArrayList<String> exportDataBase(long start, long end){
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        if (start > end){
            statement = "date <"+ String.valueOf(start)+" and date >="+String.valueOf(end);
        }else{
            statement = "date >="+ String.valueOf(start)+" and date <"+String.valueOf(end);
        }
        DbTask helperDbTask = new DbTask(context,3);
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
        cursor.close();
        db.close();
        helperDbTask.close();
        return list;
    }


    private void addTitle(WritableSheet sheet) throws WriteException {
        Label label;
        label = new Label(0,0,"时间");
        sheet.addCell(label);
        label = new Label(1,0,"扬尘 mg/m³");
        sheet.addCell(label);
        label = new Label(2,0,"温度 ℃");
        sheet.addCell(label);
        label = new Label(3,0,"湿度 %");
        sheet.addCell(label);
        label = new Label(4,0,"气压 hPa");
        sheet.addCell(label);
        label = new Label(5,0,"风速 m/s");
        sheet.addCell(label);
        label = new Label(6,0,"风向 °");
        sheet.addCell(label);
        label = new Label(7,0,"噪声 dB");
        sheet.addCell(label);
    }

    private void addOneSheet(WritableSheet sheet,ArrayList<HistoryDataFormat> list,int index,int max) throws WriteException {
        HistoryDataFormat format;
        int row=1;
        float [] data;
        String date;
        for(int i=index;i<max;i++){
            format = list.get(i);
            date = format.getDate();
            data = format.getData();
            Label label;
            label = new Label(0,row,date);
            sheet.addCell(label);
            for(int j=0;j<7;j++){
                label = new Label(j+1,row,tools.float2String3(data[j]));
                sheet.addCell(label);
            }
            row++;
        }
    }

    @Override
    public boolean exportData2File(long start, long  end,ExportDataProcessListener listener) {
        boolean exportDataResult=true;
        String pathName = "/mnt/usbhost/Storage01/GREAN/"; // /storage/sdcard0/GREAN/
        String fileName = "数据"+tools.nowTime2FileString()+"导出.xls";
        File path = new File(pathName);
        File file = new File(path,fileName);

        try{
            if (!path.exists()) {
                //Log.d("TestFile", "Create the path:" + pathName);
                path.mkdir();
            }
            if (!file.exists()) {
                //Log.d("TestFile", "Create the file:" + fileName);
                file.createNewFile();
            }
            if(listener!=null) {
                listener.setProcess(10);
            }
            WritableWorkbook wwb;
            OutputStream os = new FileOutputStream(file);
            wwb = Workbook.createWorkbook(os);

            ArrayList<HistoryDataFormat> list = exportDataFormat(ResultMin,start,end);
            WritableSheet sheet;
            //每个sheet最多65534行
            int elementMax = list.size();
            int sheetMax;
            if(elementMax > 0) {
                sheetMax = elementMax / 65534;
                sheetMax += 1;
                int index = 0;
                for(int i=0;i<sheetMax;i++){
                    sheet = wwb.createSheet("分钟数据"+String.valueOf(i+1),i);
                    addTitle(sheet);
                    if((elementMax-index)>= 65534){
                        addOneSheet(sheet,list,index,index+65534);
                        index += 65534;
                    }else{
                        addOneSheet(sheet,list,index,elementMax);
                        break;
                    }
                }
            }else{
                sheetMax = 1;
                sheet = wwb.createSheet("分钟数据1",0);
                addTitle(sheet);
            }
            /*wwb.write();
            os.flush();*/
            //写小时数据
            Log.d(tag,"写小时数据");
            list = exportDataFormat(ResultHour,start,end);
            elementMax = list.size();
            if(elementMax > 0) {
                int hourSheetMax = elementMax / 65534;
                hourSheetMax += 1;
                int index = 0;
                for(int i=0;i<hourSheetMax;i++){
                    sheet = wwb.createSheet("小时数据"+String.valueOf(i+1),i+sheetMax);
                    addTitle(sheet);
                    if((elementMax-index)>= 65534){
                        addOneSheet(sheet,list,index,index+65534);
                        index += 65534;
                    }else{
                        addOneSheet(sheet,list,index,elementMax);
                        break;
                    }
                }
            }else{
                sheet = wwb.createSheet("小时数据1",0);
                addTitle(sheet);
            }
            wwb.write();
            os.flush();

            wwb.close();
            //需要关闭输出流，结束占用，否则系统会 结束 app
            os.close();
            if(listener!=null) {
                listener.setProcess(80);
            }
        }catch (IOException e) {
            e.printStackTrace();
            exportDataResult = false;
        } catch (RowsExceededException e) {
            e.printStackTrace();
            exportDataResult = false;
        } catch (WriteException e) {
            e.printStackTrace();
            exportDataResult = false;
        }
        return exportDataResult;

        /*String fileName = "数据"+tools.nowTime2FileString()+"导出.txt";
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
        return exportDataResult;*/
    }

    @Override
    public GeneralHistoryDataFormat getHourData(long dateStart,long dateEnd) {
        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >="+ String.valueOf(dateStart)+" and date <"+String.valueOf(dateEnd);
        DbTask helperDbTask = new DbTask(context,3);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM result_hour WHERE "+statement+" ORDER BY date asc",new String[]{});
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
        cursor.close();
        db.close();
        helperDbTask.close();
        return format;
    }

    @Override
    public ArrayList<String> getDayLog(long endDate) {
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        statement = "date >="+ String.valueOf(endDate - 3600000l*24)+" and date <="+String.valueOf(endDate);

        DbTask helperDbTask = new DbTask(context,3);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM log WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index=0;
        while ((cursor.moveToNext())&&(index < 100)){
            list.add(cursor.getString(2));
            index++;
        }
        cursor.close();
        db.close();
        helperDbTask.close();
        return list;
    }

    @Override
    public GeneralHistoryDataFormat getData(long start, long end) {
        GeneralHistoryDataFormat format = new GeneralHistoryDataFormat();
        String statement;
        statement = "date >"+ String.valueOf(start)+" and date <="+String.valueOf(end);
        DbTask helperDbTask = new DbTask(context,3);
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
        cursor.close();
        db.close();
        helperDbTask.close();
        return format;
    }

    @Override
    public ArrayList<String> getLog(long start, long end) {
        ArrayList<String> list = new ArrayList<String>();
        String statement;
        statement = "date >="+ String.valueOf(start)+" and date <"+String.valueOf(end);

        DbTask helperDbTask = new DbTask(context,3);
        SQLiteDatabase db = helperDbTask.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM log WHERE "+statement+" ORDER BY date desc",new String[]{});
        int index=0;
        while ((cursor.moveToNext())&&(index < 100)){
            list.add(cursor.getString(2));
            index++;
        }
        cursor.close();
        db.close();
        helperDbTask.close();
        return list;
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
    public long getLastHourDate() {
        return lastHourDate;
    }

    @Override
    public long getNextHourDate() {
        return nextHourDate;
    }

    @Override
    public long calcNextHourDate(long now) {
        lastHourDate = nextHourDate;
        nextHourDate = tools.calcNextTime(now,lastHourDate,60*60000l);
        //myApplication.getInstance().saveConfig("LastHourDate",lastHourDate);
        Log.d(tag,"calcNextHourDate"+tools.timestamp2string(nextHourDate));
        return nextHourDate;
    }

    @Override
    public long calcNextMinDate(long now) {
       // Log.d(tag,"now="+tools.timestamp2string(now)+";plan="+tools.timestamp2string(lastMinDate)+";interval = "+String.valueOf(minInterval/1000l));
        lastMinDate = nextMinDate;
        nextMinDate = tools.calcNextTime(now,lastMinDate,minInterval);
        //myApplication.getInstance().saveConfig("LastMinDate",lastMinDate);
        return nextMinDate;
    }

    @Override
    public void loadMinDate() {
        lastMinDate = myApplication.getInstance().getConfigLong("LastMinDate");
        minInterval = myApplication.getInstance().getConfigLong("MinInterval");
        lastHourDate = myApplication.getInstance().getConfigLong("LastHourDate");
        if(lastMinDate == 0l){
            lastMinDate = 1505923200000l;
        }
        if(lastHourDate == 0l){
            lastHourDate = 1505923200000l;
        }
        if(minInterval == 0l){
            minInterval = 300000l;
        }
        nextMinDate = lastMinDate + minInterval;
        nextHourDate = lastHourDate + 3600000l;
        Log.d(tag,"nextHourDate"+tools.timestamp2string(nextHourDate));
       // Log.d(tag,"next ="+tools.timestamp2string(nextMinDate)+";plan="+tools.timestamp2string(lastMinDate)+";interval = "+String.valueOf(minInterval/1000l));
    }

    @Override
    public void saveMinDate() {
        myApplication.getInstance().saveConfig("LastMinDate",lastMinDate);
    }

    @Override
    public void saveHourDate() {
        myApplication.getInstance().saveConfig("LastHourDate",lastHourDate);
    }
}
