package com.grean.dustctrl;

import com.tools;

/**
 * Created by Administrator on 2017/9/1.
 */

public class LogFormat {
    private long date;
    private String text;

    public LogFormat (String text){
        date = tools.nowtime2timestamp();
        this.text = tools.timestamp2string(date)+text;
    }

    public long getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "LogFormat";
    }
}
