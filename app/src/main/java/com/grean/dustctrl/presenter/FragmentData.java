package com.grean.dustctrl.presenter;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grean.dustctrl.R;
import com.grean.dustctrl.model.SearchData;
import com.tools;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/25.
 */

public class FragmentData extends Fragment implements View.OnClickListener , InsertString , NotifyDataInfo{
    private LinearLayout mainLinearLayout;
    private RelativeLayout relativeLayout;
    private TextView tvStartTime,tvEndTime;
    private Button btnSearchData,btnSearchLog,btnExportData;
    private String start,end;
    private SetSearchStartTime startTime;
    private SetSearchEndTime endTime;
    private SearchData data;
    private ProcessDialogFragment dialogFragment;
    private String exportDataResult;

    private static final int msgCancelDialog = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case msgCancelDialog:
                    dialogFragment.dismiss();
                    Toast.makeText(getActivity(),exportDataResult,Toast.LENGTH_SHORT).show();
                    break;
                default:

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_data,container,false);
        mainLinearLayout = messageLayout.findViewById(R.id.myTable);
        initView(messageLayout);
        startTime = new SetSearchStartTime();
        endTime = new SetSearchEndTime();
        data = new SearchData(this,getActivity());
        data.initTitle();
        long l= tools.nowtime2timestamp();
        end = tools.timestamp2string(l);
        start = tools.timestamp2string(l-3600000l);
        data.searchData(l-3600000l,l);
        tvStartTime.setText(start);
        tvEndTime.setText(end);
        return messageLayout;
    }

    private void initView (View v){
        tvStartTime = v.findViewById(R.id.tvDataStartTime);
        tvEndTime = v.findViewById(R.id.tvDataEndTime);
        btnSearchData = v.findViewById(R.id.btnDataInquire);
        btnSearchLog = v.findViewById(R.id.btnLogInquire);
        btnExportData = v.findViewById(R.id.btnDataExport);
        tvEndTime.setOnClickListener(this);
        tvStartTime.setOnClickListener(this);
        btnSearchData.setOnClickListener(this);
        btnSearchLog.setOnClickListener(this);
        btnExportData.setOnClickListener(this);
    }

    @Override
    public void insertStrings(String[] strings) {
        if (strings.length == 8){
            relativeLayout = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.table,null);
            TextView title = relativeLayout.findViewById(R.id.list_1_1);
            title.setText(strings[0]);
            title = relativeLayout.findViewById(R.id.list_1_2);
            title.setText(strings[1]);
            title = relativeLayout.findViewById(R.id.list_1_3);
            title.setText(strings[2]);
            title = relativeLayout.findViewById(R.id.list_1_4);
            title.setText(strings[3]);
            title = relativeLayout.findViewById(R.id.list_1_5);
            title.setText(strings[4]);
            title = relativeLayout.findViewById(R.id.list_1_6);
            title.setText(strings[5]);
            title = relativeLayout.findViewById(R.id.list_1_7);
            title.setText(strings[6]);
            title = relativeLayout.findViewById(R.id.list_1_8);
            title.setText(strings[7]);
            mainLinearLayout.addView(relativeLayout);
        }
    }

    @Override
    public void clearAll() {
        mainLinearLayout.removeAllViews();
        data.initTitle();
    }

    @Override
    public void insertLog(String string) {
        if(string!=null){
            relativeLayout = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.table_text,null);
            TextView text = relativeLayout.findViewById(R.id.logText);
            text.setText(string);
            mainLinearLayout.addView(relativeLayout);
        }
    }


    public void clearContent() {
        mainLinearLayout.removeAllViews();
    }

    @Override
    public void exportDataResult(boolean success) {
        if(success){
            exportDataResult = "导出成功！";
        }else{
            exportDataResult = "导出失败，请检查外部存储器！";
        }
        handler.sendEmptyMessage(msgCancelDialog);
    }

    private class SetSearchStartTime implements DialogTimeSelected{

        @Override
        public void onComplete(String string) {
            start = string;
            tvStartTime.setText(string);
        }
    }

    private class SetSearchEndTime implements DialogTimeSelected{

        @Override
        public void onComplete(String string) {
            end = string;
            tvEndTime.setText(string);
        }
    }

    @Override
    public void onClick(View view) {
        DialogTimeChoose choose;
        Calendar calendar;
        switch (view.getId()){
            case R.id.tvDataStartTime:
                choose = new DialogTimeChoose(getActivity(),"设置起始查询时间");
                calendar = Calendar.getInstance();
                choose.showDialog(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,startTime);
                break;
            case R.id.tvDataEndTime:
                choose = new DialogTimeChoose(getActivity(),"设置截止查询时间");
                calendar = Calendar.getInstance();
                choose.showDialog(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,endTime);
                break;
            case R.id.btnDataInquire:
                data.searchData(start,end);
                break;
            case R.id.btnLogInquire:
                data.searchLog(start,end);
                break;
            case R.id.btnDataExport:
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(getFragmentManager(),"ExportData");
                data.exportData(tools.string2timestamp(tvStartTime.getText().toString()),tools.string2timestamp(tvEndTime.getText().toString()),this);
                break;
            default:

                break;
        }
    }
}
