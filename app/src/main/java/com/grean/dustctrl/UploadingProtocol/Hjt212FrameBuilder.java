package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.tools;

/**
 * Created by weifeng on 2018/6/28.
 */

public class Hjt212FrameBuilder {
    private final static String tag = "Hjt212FrameBuilder";
    protected static String qn,st,cn,pw,mn,flag,content="";

    public void cleanContent(){
        qn="";
        st="";
        cn="";
        mn="";
        flag="";
        pw="";
        content="";
    }

    public Hjt212FrameBuilder(){
        qn="";
        st="";
        cn="";
        mn="";
        flag="";
        pw="";
        content="";
    }

    public String insertOneFrame(){

        String body = qn+st+cn+pw+mn+flag+"CP=&&"+content+"&&";
        byte [] bodyBuff = body.getBytes();
        int crc = tools.getCrc16CheckOut(bodyBuff);
        // int crc = tools.calcCrc16(bodyBuff);
        byte [] crcBuff = tools.int2bytes(crc);
        byte [] crcFormatBuff = new byte[2];
        crcFormatBuff[0] = crcBuff[2];
        crcFormatBuff[1] = crcBuff[3];
        String crcString = tools.bytesToHexString(crcFormatBuff,crcFormatBuff.length);
        String lenString = String.format("%04d",bodyBuff.length);
        //Log.d(tag,"##"+lenString+body+crcString+"\r\n");
        return "##"+lenString+body+crcString+"\r\n";
    }

    public Hjt212FrameBuilder setQn(String string){
        qn="QN="+string+";";
        return this;
    }

    public Hjt212FrameBuilder setSt(String string){
        st="ST="+string+";";
        return this;
    }

    public Hjt212FrameBuilder setCn(String string){
        cn="CN="+string+";";
        return this;
    }

    public Hjt212FrameBuilder setMn(String string){
        mn="MN="+string+";";
        return this;
    }

    public Hjt212FrameBuilder setPw(String string){
        pw="PW="+string+";";
        return this;
    }

    public Hjt212FrameBuilder setFlag(String string){
        flag = "Flag="+string+";";
        return this;
    }

    public Hjt212FrameBuilder addContentFactor(String factor,String value,String flag){
        if(content.equals("")){
            content = factor+"="+value+","+factor+"-Flag="+flag;
        }else {
            content = content+ ";" + factor + "=" + value + "," + factor + "-Flag=" + flag ;
        }
        return this;
    }

    public Hjt212FrameBuilder addContentFactor(String factor,String key,String value,String flag){
        if(content.equals("")){
            content = factor+"-"+key+"="+value+","+factor+"-Flag="+flag;
        }else{
            content = content+";"+factor+"-"+key+"="+value+","+factor+"-Flag="+flag;
        }

        return this;
    }

    public Hjt212FrameBuilder addContentField(String field,String value){
        if(content.equals("")){
            content = field + "="+value;
        }else{
            content = content+";" + field + "="+value;
        }
        return this;
    }

    public Hjt212FrameBuilder addContentInfo(String polId,String field,String info){
        if(content.equals("")){
            content = "PolId="+ polId+","+field+"-Info="+info;
        }else{
            content = content+";" + "PolId="+ polId+","+field+"-Info="+info;
        }
        return this;
    }

    public Hjt212FrameBuilder contentQnRtn(){
        content = "QnRtn=1";
        return this;
    }

    public Hjt212FrameBuilder contentExeRtn(){
        content = "ExeRtn=1";
        return this;
    }

}
