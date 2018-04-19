package com.grean.dustctrl.presenter;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.grean.dustctrl.NoiseCalibrationListener;
import com.grean.dustctrl.R;
import com.grean.dustctrl.model.OperateDustMeter;
import com.grean.dustctrl.model.OperateSystem;
import com.grean.dustctrl.model.OperateTcp;
import com.taobao.sophix.SophixManager;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/25.
 */

public class FragmentOperate extends Fragment implements NotifyOperateInfo ,View.OnClickListener , DialogTimeSelected, AdapterView.OnItemSelectedListener,UpDateProcessFragment{
    private static final int CancelDialog = 1;
    private static final int ShowDustMeterInfo = 2;
    private static final int CancelDialogWithToast = 3,showNoiseCalResult = 4;

    private static final String tag = "FragmentOperate";

    private ProcessDialogFragment dialogFragment;
    private ProcessFragment processFragment;
    private String dustMeterInfo,autoCalTime,toastString,NoiseCalibrationInfo;
    private Button btnDustMeterManCal,btnDustMeterInquire,btnMotorSet,btnSaveAutoCal,btnSaveServer,btnUpdateSoftware,btnCalcParaK,btnSetAlarm
            ,btnDustMeterManCalZero,btnMotorTestUp,btnMotorTestDown,btnUpdateSetting,btnSetParaK,btnNoiseCal,btnResetResetCom;
    private TextView tvDustMeterInfo,tvNextAutoCalTime,tvLocalIp,tvSoftwareVersion;//tvParaK
    private EditText etMotorRounds,etMotorTime,etAutoCalInterval,etServerIp,etServerPort,etUpdateSoftwareUrl,etTargetValue,etMnCode,etAlarm,etSetParaK,etSetParaB;
    private Switch swDustMeterRun,swValve,swFan,swExt1,swExt2,swBackup,swAutoCalibrationEnable;
    private Spinner spProtocol,spDustName,spDustMeter;
    private int clientProtocolName;



    private OperateDustMeter dustMeter;
    private OperateSystem system;
    private OperateTcp operateTcp;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){
                case CancelDialog:
                    dialogFragment.dismiss();
                    break;
                case ShowDustMeterInfo:
                    tvDustMeterInfo.setText(dustMeterInfo);
                    break;
                case CancelDialogWithToast:
                    dialogFragment.dismiss();
                    Toast.makeText(getActivity(),toastString,Toast.LENGTH_SHORT).show();
                    break;
                case showNoiseCalResult:
                    if(processFragment!=null){
                        processFragment.dismiss();
                    }
                    Toast.makeText(getActivity(),NoiseCalibrationInfo,Toast.LENGTH_LONG).show();
                    break;
                default:

                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_operate,container,false);
        initView(messageLayout);
        dustMeter = new OperateDustMeter(this);
        system = new OperateSystem();
        operateTcp = new OperateTcp(this);
        onShow();
        return messageLayout;
    }

    private void onShow(){
        etServerIp.setText(system.getServerIp());
        etServerPort.setText(system.getServerPort());
        swDustMeterRun.setChecked(dustMeter.isDustMeterRun());
        etMotorRounds.setText(String.valueOf(system.getMotorRounds()));
        float time = system.getMotorTime() / 100.0f;
        etMotorTime.setText(String.valueOf(time));
        etAutoCalInterval.setText(system.getAutoCalInterval());
        autoCalTime = system.getAutoCalNextTime();
        tvNextAutoCalTime.setText(autoCalTime);
        tvLocalIp.setText(operateTcp.getLocalIpAddress()+":8888");
        //tvParaK.setText(dustMeter.getParaKString());
        etSetParaK.setText(dustMeter.getParaKString());
        etSetParaB.setText(dustMeter.getParaBString());
        etMnCode.setText(operateTcp.getTcpMnCode());
        if (system.getAutoCalibrationEnable()){
            swAutoCalibrationEnable.setChecked(true);
        }else {
            swAutoCalibrationEnable.setChecked(false);
            tvNextAutoCalTime.setVisibility(View.INVISIBLE);
            etAutoCalInterval.setVisibility(View.INVISIBLE);
            btnSaveAutoCal.setVisibility(View.INVISIBLE);
        }
        tvSoftwareVersion.setText("当前软件版本:"+system.getVersionName(getActivity()));
        ArrayAdapter<String> clientProtocolNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,system.getClientProtocolNames());
        spProtocol.setOnItemSelectedListener(this);
        spProtocol.setAdapter(clientProtocolNames);
        clientProtocolName = system.getClientName();
        spProtocol.setSelection(clientProtocolName);

        ArrayAdapter<String>dustNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,dustMeter.DustNames);
        spDustName.setOnItemSelectedListener(this);
        spDustName.setAdapter(dustNames);
        spDustName.setSelection(dustMeter.getDustName(),true);

        ArrayAdapter<String>dustMeters = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,dustMeter.DustMeters);
        spDustMeter.setOnItemSelectedListener(this);
        //spDustMeter.setOnItemClickListener(this);
        spDustMeter.setAdapter(dustMeters);
        spDustMeter.setSelection(dustMeter.getDustMeter(),true);


        etAlarm.setText(system.getAlarmDust());
        swValve.setChecked(dustMeter.getCtrlDo(0));
        swFan.setChecked(dustMeter.getCtrlDo(1));
        swExt1.setChecked(dustMeter.getCtrlDo(2));
        swExt2.setChecked(dustMeter.getCtrlDo(3));
        swBackup.setChecked(dustMeter.getCtrlDo(4));
    }

    private void initView(View v){
        spDustMeter = v.findViewById(R.id.spOperateDustMeter);
        spDustName = v.findViewById(R.id.spOperateDust);
        btnDustMeterInquire = v.findViewById(R.id.btnOperateInquireDuster);
        btnDustMeterManCal = v.findViewById(R.id.btnOperateManCal);
        tvDustMeterInfo = v.findViewById(R.id.tvOperateDusterInfo);
        swDustMeterRun = v.findViewById(R.id.swOperateDusterSwitch);
        swValve = v.findViewById(R.id.swOperateSystemDo1);
        swFan = v.findViewById(R.id.swOperateSystemDo2);
        swExt1 = v.findViewById(R.id.swOperateSystemDo3);
        swExt2 = v.findViewById(R.id.swOperateSystemDo4);
        swBackup = v.findViewById(R.id.swOperateSystemDo5);
        btnMotorSet = v.findViewById(R.id.btnOperateMotorSet);
        etMotorRounds = v.findViewById(R.id.etOperateMotorRounds);
        etMotorTime = v.findViewById(R.id.etOperateMotorTime);
        tvNextAutoCalTime = v.findViewById(R.id.tvOperateNextAutoCal);
        etAutoCalInterval = v.findViewById(R.id.etOperateAutoCalInterval);
        btnSaveAutoCal = v.findViewById(R.id.btnOperateSaveAutoCal);
        swAutoCalibrationEnable = v.findViewById(R.id.swAutoCaliration);
        etServerIp = v.findViewById(R.id.etOperateServerIP);
        etServerPort = v.findViewById(R.id.etOperateServerPort);
        etUpdateSoftwareUrl = v.findViewById(R.id.etOperateUpdateUrl);
        btnSaveServer = v.findViewById(R.id.btnOperateSaveServer);
        btnUpdateSoftware = v.findViewById(R.id.btnOperateUpdateSoftware);
        tvLocalIp = v.findViewById(R.id.tvOperateLocalIp);
        //tvParaK = v.findViewById(R.id.tvOperateParaK);
        etSetParaK = v.findViewById(R.id.etOperateParaK);
        etSetParaB = v.findViewById(R.id.etOperateParaB);
        btnSetParaK = v.findViewById(R.id.btnOperateSetParaK);
        etTargetValue = v.findViewById(R.id.etOperateTargetValue);
        btnCalcParaK = v.findViewById(R.id.btnOperateCalcPraraK);
        etMnCode = v.findViewById(R.id.etOperateMnCode);
        tvSoftwareVersion = v.findViewById(R.id.tvOperateSoftwareVerison);
        btnSetAlarm = v.findViewById(R.id.btnOperateSaveAlarm);
        spProtocol = v.findViewById(R.id.spOperateProtocol);
        btnDustMeterManCalZero = v.findViewById(R.id.btnOperateCalZero);
        etAlarm = v.findViewById(R.id.etOperateAlarm);
        btnUpdateSetting = v.findViewById(R.id.btnOperateUpdateSetting);
        btnMotorTestDown = v.findViewById(R.id.btnOperateTestDown);
        btnMotorTestUp = v.findViewById(R.id.btnOperateTestUp);
        btnNoiseCal = v.findViewById(R.id.btnOperateNoiseCal);
        btnResetResetCom = v.findViewById(R.id.btnOperateResetCom);
        btnResetResetCom.setOnClickListener(this);
        btnNoiseCal.setOnClickListener(this);
        btnMotorTestDown.setOnClickListener(this);
        btnMotorTestUp.setOnClickListener(this);
        btnDustMeterManCalZero.setOnClickListener(this);
        btnSetAlarm.setOnClickListener(this);
        btnCalcParaK.setOnClickListener(this);
        btnSaveServer.setOnClickListener(this);
        btnUpdateSoftware.setOnClickListener(this);
        swAutoCalibrationEnable.setOnClickListener(this);
        tvNextAutoCalTime.setOnClickListener(this);
        btnSaveAutoCal.setOnClickListener(this);
        btnDustMeterManCal.setOnClickListener(this);
        btnDustMeterInquire.setOnClickListener(this);
        swDustMeterRun.setOnClickListener(this);
        swBackup.setOnClickListener(this);
        swFan.setOnClickListener(this);
        swValve.setOnClickListener(this);
        swExt2.setOnClickListener(this);
        swExt1.setOnClickListener(this);
        btnMotorSet.setOnClickListener(this);
        btnUpdateSetting.setOnClickListener(this);
        btnSetParaK.setOnClickListener(this);
    }


    @Override
    public void showDustMeterInfo(String info) {
        dustMeterInfo = info;
        handler.sendEmptyMessage(ShowDustMeterInfo);
    }

    @Override
    public void cancelDialog() {
        handler.sendEmptyMessage(CancelDialog);
    }

    @Override
    public void cancelDialogWithToast(String string) {
        toastString = string;
        handler.sendEmptyMessage(CancelDialogWithToast);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.btnOperateManCal:
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(getFragmentManager(),"Calibration");
                dustMeter.calibrationDustMeter(dialogFragment);
                break;
            case R.id.btnOperateInquireDuster:
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(getFragmentManager(),"InquireDustMeterInfo");
                dustMeter.inquireDustMeter(dialogFragment);
                break;
            case R.id.swOperateDusterSwitch:
                dustMeter.switchDustMeter(swDustMeterRun.isChecked());
                break;
            case R.id.btnOperateMotorSet:
                int rounds = Integer.valueOf(etMotorRounds.getText().toString());
                final int time = (int) (Float.valueOf(etMotorTime.getText().toString())*100f);
                system.setMotorSetting(rounds,time);
                break;
            case R.id.swOperateSystemDo1:
                system.ctrlDo(1,swValve.isChecked());
                break;
            case R.id.swOperateSystemDo2:
                system.ctrlDo(2,swFan.isChecked());
                break;
            case R.id.swOperateSystemDo3:
                system.ctrlDo(3,swExt1.isChecked());
                break;
            case R.id.swOperateSystemDo4:
                system.ctrlDo(4,swExt2.isChecked());
                break;
            case R.id.swOperateSystemDo5:
                system.ctrlDo(5,swBackup.isChecked());
                break;
            case R.id.tvOperateNextAutoCal:
                Calendar calendar = Calendar.getInstance();
                DialogTimeChoose choose = new DialogTimeChoose(getActivity(),"设置下次自动校准时间");
                choose.showDialog(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,this);
                break;
            case R.id.btnOperateSaveAutoCal:
                system.setAutoCalInterval(etAutoCalInterval.getText().toString());
                autoCalTime = system.calNexTime(tvNextAutoCalTime.getText().toString());
                system.setAutoTime(autoCalTime);
                intent = new Intent();
                intent.setAction("autoCalibration");
                intent.putExtra("enable",true);
                intent.putExtra("date",system.getAutoTimeDate());
                getActivity().sendBroadcast(intent);
                break;
            case R.id.swAutoCaliration:
                system.setAutoCalibrationEnable(swAutoCalibrationEnable.isChecked());
                intent = new Intent();
                intent.setAction("autoCalibration");
                if (swAutoCalibrationEnable.isChecked()){
                    tvNextAutoCalTime.setVisibility(View.VISIBLE);
                    etAutoCalInterval.setVisibility(View.VISIBLE);
                    btnSaveAutoCal.setVisibility(View.VISIBLE);
                    intent.putExtra("enable",true);
                }else{
                    intent.putExtra("enable",false);
                    tvNextAutoCalTime.setVisibility(View.INVISIBLE);
                    etAutoCalInterval.setVisibility(View.INVISIBLE);
                    btnSaveAutoCal.setVisibility(View.INVISIBLE);
                }
                intent.putExtra("date",system.getAutoTimeDate());
                getActivity().sendBroadcast(intent);
                break;
            case R.id.btnOperateSaveServer:
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getFragmentManager(),"TcpSocket");
                operateTcp.setTcpSocketClient(etServerIp.getText().toString(),Integer.valueOf(etServerPort.getText().toString()),etMnCode.getText().toString(),dialogFragment,clientProtocolName);
                break;
            case R.id.btnOperateUpdateSoftware:
                SophixManager.getInstance().queryAndLoadNewPatch();
                /*dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getFragmentManager(),"DownLoadSoftware");
                system.startDownLoadSoftware(getActivity(),etUpdateSoftwareUrl.getText().toString(),dialogFragment,this);*/
                break;
            case R.id.btnOperateCalcPraraK:
                String string = dustMeter.calcParaK(etTargetValue.getText().toString());
                //tvParaK.setText(string);
                etSetParaK.setText(string);
                break;
            case R.id.btnOperateSaveAlarm:
                system.setAlarmDust(etAlarm.getText().toString());
                break;
            case R.id.btnOperateCalZero:
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(getFragmentManager(),"Calibration");
                dustMeter.calibrationDustMeterZero(dialogFragment);
                break;
            case R.id.btnOperateTestDown:
                system.testMotor(false);
                break;
            case R.id.btnOperateTestUp:
                system.testMotor(true);
                break;
            case R.id.btnOperateUpdateSetting:
                onShow();
                break;
            case R.id.btnOperateSetParaK:
                dustMeter.setParaK(etSetParaK.getText().toString());
                dustMeter.setParaB(etSetParaB.getText().toString());
                break;
            case R.id.btnOperateNoiseCal:
                processFragment = new ProcessFragment();
                processFragment.setCancelable(false);
                processFragment.show(getFragmentManager(),"calibration noise");
                system.calNoise(this);
                break;
            case R.id.btnOperateResetCom:
                system.resetComFlag();
                break;
            default:
                break;
        }
    }

    @Override
    public void onComplete(String string) {
        autoCalTime = system.calNexTime(string);
        tvNextAutoCalTime.setText(autoCalTime);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.spOperateProtocol:
                clientProtocolName = i;
                break;
            case R.id.spOperateDust:
                dustMeter.setDustName(i);
                //Toast.makeText(getActivity(),"当前扬尘参数为 "+dustMeter.DustNames[i],Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction("changeDustName");
                intent.putExtra("name",dustMeter.DustNames[i]);
                getActivity().sendBroadcast(intent);
                break;
            case R.id.spOperateDustMeter:
                dustMeter.setDustMeter(i);
                break;
            default:

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void setContent(String content) {
        if(processFragment!=null){
            processFragment.showInfo(content);
        }
    }

    @Override
    public void setProcess(int process) {
        if(processFragment!=null){
            processFragment.showProcess(process);
        }
    }

    @Override
    public void cancelFragmentWithToast(String string) {
        NoiseCalibrationInfo = string;
        handler.sendEmptyMessage(showNoiseCalResult);
    }


}
