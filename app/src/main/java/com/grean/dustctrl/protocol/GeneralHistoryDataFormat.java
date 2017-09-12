package com.grean.dustctrl.protocol;

import java.util.ArrayList;

/**
 * Created by weifeng on 2017/9/12.
 */

public class GeneralHistoryDataFormat {
    private ArrayList<Long> date = new ArrayList<Long>();
    private ArrayList<ArrayList<Float>> data = new ArrayList<ArrayList<Float>>();
    public GeneralHistoryDataFormat(){

    }

    public void addDate(long date){
        this.date.add(date);
    }

    public void addItem(ArrayList<Float> item){
        data.add(item);
    }
    public void cleanAll(){
        date.clear();
        data.clear();
    }

    public int getSize(){
        int size = date.size();
        if(size <= data.size()){
            return size;
        }else{
            return data.size();
        }
    }

    public long getDate(int index){
        return date.get(index);
    }

    public ArrayList<Float> getItem(int index){
        return data.get(index);
    }
}
