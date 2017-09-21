package com.grean.dustctrl.presenter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.grean.dustctrl.R;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/30.
 */

public class DialogTimeChoose {
    private Context context;
    private String title;

    public DialogTimeChoose(Context context,String title){
        this.context = context;
        this.title = title;
    }

    /**
     * 显示对话框 日期 时间
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param min 分
     * @param selected 保存的回调的代码接口
     */
    public void showDialog(int year, int month, int day, int hour, int min , final DialogTimeSelected selected){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = View.inflate(context, R.layout.date_time_dialog,null);
        final DatePicker datePicker = v.findViewById(R.id.datePicker);
        final TimePicker timePicker = v.findViewById(R.id.timePicker);
        builder.setView(v);

        datePicker.init(year,month,day,null);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(min);

        builder.setTitle(title);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d %02d:%02d",datePicker.getYear(),datePicker.getMonth()+1,datePicker.getDayOfMonth(),timePicker.getCurrentHour(),timePicker.getCurrentMinute()));
                /*autoCalTime = system.calNexTime(sb.toString());
                tvNextAutoCalTime.setText(autoCalTime);*/
                selected.onComplete(sb.toString());
                dialogInterface.cancel();
            };
        });

        Dialog dialog = builder.create();
        dialog.show();

    }

}
