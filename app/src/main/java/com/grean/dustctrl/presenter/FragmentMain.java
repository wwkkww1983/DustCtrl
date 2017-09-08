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
    private TextView tvDust;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvPressure;
    private TextView tvWindForce;
    private TextView tvWindDirection;
    private TextView tvNoise,tvNextCal;
    private SensorData data;
    private OperateInit operateInit;

    private String nextCalString;
    private static final int msgUpdateSensor = 1;
    private static final int msgUpdateNextCal =2;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("autoCalNextString")){
                nextCalString = intent.getStringExtra("content");
                handler.sendEmptyMessage(msgUpdateNextCal);
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
                    tvHumidity.setText(tools.float2String3(data.getAirTemperature()));
                    tvPressure.setText(tools.float2String3(data.getAirPressure()));
                    tvWindForce.setText(tools.float2String3(data.getWindForce()));
                    tvWindDirection.setText(tools.float2String3(data.getWindDirection()));
                    tvNoise.setText(tools.float2String3(data.getValue()));
                    break;
                case msgUpdateNextCal:
                    tvNextCal.setText("自动校准:"+nextCalString);
                    break;

                default:

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_main,container,false);
        initView(messageLayout);
        ScanSensor.getInstance().setNotifyScanSensor(this);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("autoCalNextString");
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
        operateInit = new OperateInit(getActivity());
        tvNextCal.setText(operateInit.getAutoNextTime());
        operateInit.setAutoCalTime();
        return messageLayout;
    }

    void initView(View v){
        tvDust = v.findViewById(R.id.tvMainDust);
        tvTemperature = v.findViewById(R.id.tvMainTemperature);
        tvHumidity = v.findViewById(R.id.tvMainHumidity);
        tvPressure = v.findViewById(R.id.tvMainPressure);
        tvWindForce = v.findViewById(R.id.tvMainWindForce);
        tvWindDirection = v.findViewById(R.id.tvMainWindDirection);
        tvNoise = v.findViewById(R.id.tvMainNoise);
        tvNextCal = v.findViewById(R.id.tvMainNextCal);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResult(SensorData data) {
        this.data = data;
        handler.sendEmptyMessage(msgUpdateSensor);
    }
}
