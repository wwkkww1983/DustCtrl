package com.grean.dustctrl.UploadingProtocol;

import android.util.Log;

import com.tools;

/**
 * Created by weifeng on 2018/8/17.
 */

public class DefaultFrameBuilder extends Hjt212FrameBuilder{
    private final static String tag = "DefaultFrameBuilder";
    @Override
    public String insertOneFrame() {
        String body = qn+st+cn+pw+mn+flag+"CP=&&"+content+"&&";
        byte [] bodyBuff = body.getBytes();
        //int crc = tools.getCrc16CheckOut(bodyBuff);
        int crc = tools.calcCrc16(bodyBuff);
        byte [] crcBuff = tools.int2bytes(crc);
        byte [] crcFormatBuff = new byte[2];
        crcFormatBuff[0] = crcBuff[2];
        crcFormatBuff[1] = crcBuff[3];
        String crcString = tools.bytesToHexString(crcFormatBuff,crcFormatBuff.length);
        String lenString = String.format("%04d",bodyBuff.length);
        Log.d(tag,"##"+lenString+body+crcString+"\r\n");
        return "##"+lenString+body+crcString+"\r\n";
    }
}
