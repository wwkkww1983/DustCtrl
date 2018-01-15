package com.grean.dustctrl.protocol;

import com.tools;

/**
 * Created by weifeng on 2018/1/15.
 */

public class HistoryDataFormat {
    private String date;
    float [] data = new float[7];

    public HistoryDataFormat(long date,float [] data){
        if(data.length == 7){
            this.date = tools.timestamp2string(date);
            for(int i=0;i<7;i++){
                this.data[i] = data[i];
            }
        }
    }

    public String getDate() {
        return date;
    }

    public float[] getData() {
        return data;
    }
}
