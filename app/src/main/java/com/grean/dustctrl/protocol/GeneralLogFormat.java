package com.grean.dustctrl.protocol;

import java.util.ArrayList;

/**
 * Created by weifeng on 2018/7/6.
 */

public class GeneralLogFormat {
    private ArrayList<Long> date = new ArrayList<>();
    private ArrayList<String> log = new ArrayList<>();

    public void addOneLog(long time,String logString){
        date.add(time);
        log.add(logString);
    }

    public long getDate(int index){
        return date.get(index);
    }

    public String getLog(int index){
        return log.get(index);
    }

    public int getSize(){
        int size = date.size();
        if(size < log.size()){
            return size;
        }else{
            return log.size();
        }
    }
}
