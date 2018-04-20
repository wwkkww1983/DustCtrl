package com.grean.dustctrl.presenter;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grean.dustctrl.R;
import com.grean.dustctrl.model.OperateInit;
import com.grean.dustctrl.myApplication;
import com.grean.dustctrl.process.NotifyScanSensor;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by Administrator on 2017/8/25.
 */

public class FragmentMain extends Fragment implements NotifyScanSensor{
    private static final String tag = "FragmentMain";
    private TextView tvDust,tvDustName;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvPressure;
    private TextView tvWindForce;
    private TextView tvWindDirection;
    private TextView tvNoise,tvNextCal,tvValue;
    private TextView tvHiTemperature,tvLoTemperature,tvHiHumidity,tvLoHumidity,tvPwm,tvBatteryOk,tvPowerIn,tvAlarm,
    tvHiDewPoint,tvLoDewPoint;
    private SensorData data;
    private OperateInit operateInit;
    private boolean alarm = false;

    private String nextCalString,dustName;
    private static final int msgUpdateSensor = 1,
            msgUpdateNextCal =2,
            msgUpdateAlarm=3,
            msgChangeDustName = 4;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("autoCalNextString")){
                nextCalString = intent.getStringExtra("content");
                handler.sendEmptyMessage(msgUpdateNextCal);
            }else if(intent.getAction().equals("changeDustName")){
                dustName = intent.getStringExtra("name")+":";
                handler.sendEmptyMessage(msgChangeDustName);
            }



        }
    };

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){
                case msgUpdateSensor:
                    tvDust.setText(tools.float2String3(data.getDust()));
                    tvTemperature.setText(tools.float2String3(data.getAirTemperature()));
                    tvHumidity.setText(tools.float2String3(data.getAirHumidity()));
                    tvPressure.setText(tools.float2String3(data.getAirPressure()));
                    tvWindForce.setText(tools.float2String3(data.getWindForce()));
                    tvWindDirection.setText(tools.float2String3(data.getWindDirection()));
                    tvNoise.setText(tools.float2String3(data.getNoise()));
                    tvValue.setText(tools.float2String3(data.getValue()));
                    tvHiHumidity.setText(tools.float2String3(data.getHiHumidity()));
                    tvHiTemperature.setText(tools.float2String3(data.getHiTemp()));
                    tvLoHumidity.setText(tools.float2String3(data.getLoHumidity()));
                    tvLoTemperature.setText(tools.float2String3(data.getLoTemp()));
                    tvHiDewPoint.setText(String.valueOf(data.getHiDewPoint()));
                    tvLoDewPoint.setText(String.valueOf(data.getLoDewPoint()));
                    tvPwm.setText(String.valueOf(data.getHeatPwm()));
                    if(data.isAcIn()){
                        tvPowerIn.setText("外接电源:正常");
                    }else{
                        tvPowerIn.setText("外接电源:异常");
                    }
                    if(data.isBatteryLow()){
                        tvBatteryOk.setText("电池电压:低");
                    }else{
                        tvBatteryOk.setText("电池电压:正常");
                    }
                    break;
                case msgUpdateNextCal:
                    tvNextCal.setText("自动校准:"+nextCalString);
                    break;
                case msgUpdateAlarm:
                    if(alarm) {
                        tvAlarm.setText("报警:颗粒物浓度高");
                    }else{
                        tvAlarm.setText("报警:无");
                    }
                    break;
                case msgChangeDustName:
                    tvDustName.setText(dustName);
                    break;
                default:

                    break;
            }
        }
    };

    /*private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
                Log.d(tag,"temperature = "+String.valueOf(intent.getIntExtra("temperature",0)));

            }
        }
    };*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_main,container,false);
        initView(messageLayout);
        ScanSensor.getInstance().setNotifyScanSensor(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("autoCalNextString");
        intentFilter.addAction("changeDustName");
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
        operateInit = new OperateInit(getActivity());
        tvDustName.setText(operateInit.getDustName());
        tvNextCal.setText(operateInit.getAutoNextTime());
        operateInit.setAutoCalTime();

        //getActivity().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        return messageLayout;
    }

    void initView(View v){
        tvDustName = v.findViewById(R.id.tvMainDustName);
        tvDust = v.findViewById(R.id.tvMainDust);
        tvTemperature = v.findViewById(R.id.tvMainTemperature);
        tvHumidity = v.findViewById(R.id.tvMainHumidity);
        tvPressure = v.findViewById(R.id.tvMainPressure);
        tvWindForce = v.findViewById(R.id.tvMainWindForce);
        tvWindDirection = v.findViewById(R.id.tvMainWindDirection);
        tvNoise = v.findViewById(R.id.tvMainNoise);
        tvNextCal = v.findViewById(R.id.tvMainNextCal);
        tvValue = v.findViewById(R.id.tvMainValue);
        tvHiHumidity = v.findViewById(R.id.tvMainHiHumidity);
        tvHiTemperature = v.findViewById(R.id.tvMainHitemperature);
        tvLoHumidity = v.findViewById(R.id.tvMainLoHumidity);
        tvLoTemperature = v.findViewById(R.id.tvMainLoTemperature);
        tvPwm = v.findViewById(R.id.tvMainPwm);
        tvBatteryOk = v.findViewById(R.id.tvMainBatteryOk);
        tvPowerIn = v.findViewById(R.id.tvMainPowerIn);
        tvAlarm = v.findViewById(R.id.tvMainAlarm);
        tvHiDewPoint = v.findViewById(R.id.tvMainHiDewPoint);
        tvLoDewPoint = v.findViewById(R.id.tvMainLoDewPoint);

        //tvAlarm.setText("new app");
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResult(SensorData data) {
        this.data = data;
        handler.sendEmptyMessage(msgUpdateSensor);
    }

    @Override
    public void setAlarmDust(boolean alarm) {
        if(alarm!=this.alarm){
            this.alarm = alarm;
            handler.sendEmptyMessage(msgUpdateAlarm);
        }
    }
}
