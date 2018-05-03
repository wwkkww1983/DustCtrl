package com.grean.dustctrl.protocol;

import android.util.Log;

import com.tools;

import java.util.List;

/**
 * Created by weifeng on 2017/10/24.
 */

public class Hjt212CommandProtocol implements GeneralCommandProtocol{
    private final static String tag = "Hjt212CommandProtocol";
    String qn,st,cn,pw,mn,flag,cp,systemTime,beginTime,endTime,mnCode,passWord;
    long begin,end;

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
    }

    @Override
    public void setPassWord(String string) {
        this.passWord = string;
    }


    @Override
    public boolean handleString(String string) {
        String[] item = string.split("=");
        if(item.length>=2){
          if(item[0].equals("QN")){
             qn=item[1];
          }else if(item[0].equals("ST")){
              st=item[1];
              if(!st.equals("91")){
                  return true;
              }
          }else if(item[0].equals("CN")){
              cn=item[1];
          }else if(item[0].equals("PW")){
              pw=item[1];
              if(!pw.equals(passWord)){
                  return true;
              }
          }else if(item[0].equals("MN")){
              mn=item[1];
              if(!mn.equals(mnCode)){
                  return true;
              }
          }else if(item[0].equals("Flag")){
              flag=item[1];
          }else if(item[0].equals("CP")){
              if(item.length>=3){
                  if(item[1].equals("&&SystemTime")){
                      cp="SystemTime";
                      systemTime = item[2];
                  }else if(item[1].equals("&&BeginTime")){
                      cp="BeginTime";
                      beginTime = item[2].substring(0,14);
                      endTime = item[3];
                  }else{

                  }
              }
          }else{

          }

        }
        return false;
    }

    @Override
    public void executeProtocol(GeneralReturnProtocol returnProtocol, TcpClientCallBack callBack, GeneralInfoProtocol infoProtocol) {
        if(cn.equals("2011")){//提取实时数据
            callBack.addOneFrame(returnProtocol.getRealTimeData(qn).getBytes());
        }else if(cn.equals("1012")){//修改系统时间
            Log.d(tag,"修改时间");
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            int year,month,day,hour,min,second;
            year=Integer.valueOf(systemTime.substring(0,4));
            month=Integer.valueOf(systemTime.substring(4,6));
            day=Integer.valueOf(systemTime.substring(6,8));
            hour=Integer.valueOf(systemTime.substring(8,10));
            min=Integer.valueOf(systemTime.substring(10,12));
            second=Integer.valueOf(systemTime.substring(12,14));
            Log.d(tag,String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day)+" "+String.valueOf(hour)+":"+String.valueOf(min)+":"+String.valueOf(second));
            infoProtocol.setSystemDate(year,month,day,hour,min,second);
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2051")){//提取分钟数据
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            begin = tools.tcpTimeString2timestamp(beginTime);
            end = tools.tcpTimeString2timestamp(endTime);
            List<String> list = returnProtocol.getMinData(qn,begin,end);
            for(int i=0;i<list.size();i++){
                callBack.addOneFrame(list.get(i).getBytes());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2061")){//提取小时数据
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            begin = tools.tcpTimeString2timestamp(beginTime);
            end = tools.tcpTimeString2timestamp(endTime);
            List<String> list = returnProtocol.getHourData(qn,begin,end);
            for(int i=0;i<list.size();i++){
                callBack.addOneFrame(list.get(i).getBytes());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2062")){//反馈小时数据


        }else{

        }

    }

    @Override
    public boolean checkRecString(byte[] rec, int count) {
        int len = Integer.valueOf(new String(rec,2,4));
        if((len+12)!=count){//头 + 数据块长 + crc + 尾
            return false;
        }

        if((rec[0]!='#')||(rec[1]!='#')||(rec[count-2]!='\r')||(rec[count-1]!='\n')){
            return false;
        }

        return true;
    }
}
