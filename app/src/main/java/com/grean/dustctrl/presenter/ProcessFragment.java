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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.grean.dustctrl.R;

/**
 * Created by weifeng on 2018/3/23.
 */

public class ProcessFragment extends DialogFragment{
    private TextView tvContent;
    private ProgressBar pb;
    private static final int MSG_CONTENT = 1,MSG_PROCESS = 2;
    private String content;
    private int process;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_CONTENT:
                    tvContent.setText(content);
                    break;
                case MSG_PROCESS:
                    pb.setProgress(process);
                    break;
                default:

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_process,container);
        tvContent = view.findViewById(R.id.tvProcessDialogContent);
        pb = view.findViewById(R.id.pbProcessDialog);

        return view;
    }

    public void showInfo(String string) {
        content = string;
        handler.sendEmptyMessage(MSG_CONTENT);
    }

    public void showProcess(int process) {
        this.process = process;
        handler.sendEmptyMessage(MSG_PROCESS);
    }
}
