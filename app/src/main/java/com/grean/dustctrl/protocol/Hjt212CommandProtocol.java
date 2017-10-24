package com.grean.dustctrl.protocol;

import com.tools;

/**
 * Created by weifeng on 2017/10/24.
 */

public class Hjt212CommandProtocol implements GeneralCommandProtocol{
    String qn,st,cn,pw,mn,flag,cp,systemTime,beginTime,endTime,mnCode,passWord;
    long begin,end;

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
    }

    @Override
    public void setPassWord(String stirng) {
        this.passWord = stirng;
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
        if(cn.equals("2011")){//实时数据
            callBack.addOneFrame(returnProtocol.getRealTimeData().getBytes());
        }else if(cn.equals("1012")){//修改系统时间
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            int year,month,day,hour,min,second;
            year=Integer.valueOf(systemTime.substring(0,3));
            month=Integer.valueOf(systemTime.substring(4,5));
            day=Integer.valueOf(systemTime.substring(6,7));
            hour=Integer.valueOf(systemTime.substring(8,9));
            min=Integer.valueOf(systemTime.substring(10,11));
            second=Integer.valueOf(systemTime.substring(12,13));
            infoProtocol.setSystemDate(year,month,day,hour,min,second);
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2051")){//获取分钟数据
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            begin = tools.tcpTimeString2timestamp(beginTime);
            end = tools.tcpTimeString2timestamp(endTime);
            callBack.addOneFrame(returnProtocol.getMinData(qn,begin,end).getBytes());
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2061")){//获取小时数据
            callBack.addOneFrame(returnProtocol.getSystemResponse(qn).getBytes());
            begin = tools.tcpTimeString2timestamp(beginTime);
            end = tools.tcpTimeString2timestamp(endTime);
            callBack.addOneFrame(returnProtocol.getHourData(qn,begin,end).getBytes());
            callBack.addOneFrame(returnProtocol.getSystemOk(qn).getBytes());
        }else if(cn.equals("2062")){//反馈小时数据


        }else{

        }

    }
}
