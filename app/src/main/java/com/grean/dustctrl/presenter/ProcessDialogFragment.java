package com.grean.dustctrl.presenter;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.grean.dustctrl.R;

/**
 * Created by Administrator on 2017/8/29.
 */

public class ProcessDialogFragment extends DialogFragment implements NotifyProcessDialogInfo{
    private TextView tvInfo;
    private String infoString;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if ((infoString!=null)&&(msg.what == 1)){
                tvInfo.setText(infoString);
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN);
        getDialog().setTitle("处理中");
        View view = inflater.inflate(R.layout.fragment_process_dialog,container);
        tvInfo = view.findViewById(R.id.tvProcessDialog);

        return view;
    }

    @Override
    public void showInfo(String string) {
        infoString = string;
        handler.sendEmptyMessage(1);
    }
}
