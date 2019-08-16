package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.tools;

/**
 * Created by weifeng on 2019/7/4.
 */

public class Hjt212HzFrameBuilder extends Hjt212FrameBuilder{
    private final static String tag=  "Hjt212HzFrameBuilder";
    @Override
    public String insertOneFrame() {
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

    public Hjt212FrameBuilder addContentFactor(String factor,String key1,String value1
            ,String key2,String value2,String key3,String value3,String flag){
        if(content.equals("")){
            content = factor+"-"+key1+"="+value1+","+factor+"-"+key2+"="+value2+","
                    +factor+"-"+key3+"="+value3+","+factor+"-Flag="+flag;
        }else{
            content = content+";"+factor+"-"+key1+"="+value1+","+factor+"-"+key2+"="+value2+","
                    +factor+"-"+key3+"="+value3+","+factor+"-Flag="+flag;
        }
        return this;
    }

}
