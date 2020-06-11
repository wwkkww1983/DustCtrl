package com.grean.dustctrl.presenter;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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
import com.grean.dustctrl.SystemSettingStore;
import com.grean.dustctrl.device.DevicesManage;
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
    private Button btnSaveAutoCal,btnMotorTestUp,btnMotorTestDown,btnNoiseCal,
                btnSetCameraOffset,btnMotorSetting;
    private TextView tvDustMeterInfo,tvNextAutoCalTime,tvLocalIp,tvSoftwareVersion,
                tvCameraTitle,tvMotorSettingTitle,tvMotorSettingContent1,tvMotorSettingContent2;
    private EditText etMotorRounds,etMotorTime,etAutoCalInterval,etServerIp,
            etServerPort,etUpdateSoftwareUrl,etMnCode,etAlarm,etSetParaK,
            etSetParaB,etLng,etLat,etCameraDirectionOffset,etTempSlope,etTempIntercept,
            etHumiSlope,etHumiIntercept,etBackupServerAddress,etBackupServerPort,etBackupMnCode;
    private Switch swDustMeterRun,swValve,swFan,swExt1,swExt2,swBackup,
            swAutoCalibrationEnable,swBackupServer,swRhCorrectionEnable;
    private Spinner spProtocol,spDustName,spDustMeter,spCameraName,spNoiseName,spLedDisplayName;
    private int clientProtocolName,dustName,dustMeterName,noiseName,ledDisplayName,cameraName;



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
        dustMeter = new OperateDustMeter(this,new SystemSettingStore(getActivity()));
        system = new OperateSystem(getActivity(),new SystemSettingStore(getActivity()));
        operateTcp = new OperateTcp();
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

        ArrayAdapter<String>dustNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,DevicesManage.DustNames);
        spDustName.setOnItemSelectedListener(this);
        spDustName.setAdapter(dustNames);
        dustName = dustMeter.getDustName();
        spDustName.setSelection(dustName,true);


        ArrayAdapter<String>dustMeters = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,DevicesManage.DustMeterNames);
        spDustMeter.setOnItemSelectedListener(this);
        spDustMeter.setAdapter(dustMeters);
        dustMeterName = dustMeter.getDustMeter();
        spDustMeter.setSelection(dustMeterName,true);

        ArrayAdapter<String>cameraNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,DevicesManage.CameraNames);
        spCameraName.setOnItemSelectedListener(this);
        spCameraName.setAdapter(cameraNames);
        cameraName = system.getCameraName();
        spCameraName.setSelection(cameraName,true);

        ArrayAdapter<String>ledDisplayNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,DevicesManage.LedDisplayNames);
        spLedDisplayName.setOnItemSelectedListener(this);
        spLedDisplayName.setAdapter(ledDisplayNames);
        ledDisplayName = system.getLedDisplayName();
        spLedDisplayName.setSelection(ledDisplayName,true);

        ArrayAdapter<String>noiseNames = new ArrayAdapter<String>(getActivity(),R.layout.my_spnner,DevicesManage.NoiseNames);
        spNoiseName.setOnItemSelectedListener(this);
        spNoiseName.setAdapter(noiseNames);
        noiseName = system.getNoiseName();
        spNoiseName.setSelection(noiseName,true);

        etAlarm.setText(system.getAlarmDust());
        swValve.setChecked(dustMeter.getCtrlDo(0));
        swFan.setChecked(dustMeter.getCtrlDo(1));
        swExt1.setChecked(dustMeter.getCtrlDo(2));
        swExt2.setChecked(dustMeter.getCtrlDo(3));
        swBackup.setChecked(dustMeter.getCtrlDo(4));

        etLng.setText(operateTcp.getLng());
        etLat.setText(operateTcp.getLat());
        etCameraDirectionOffset.setText(String.valueOf(system.getCameraDirectionOffset()));

        etHumiIntercept.setText(system.getParaHumiIntercept());
        etHumiSlope.setText(system.getParaHumiSlope());
        etTempIntercept.setText(system.getParaTempIntercept());
        etTempSlope.setText(system.getParaTempSlope());

        etBackupServerPort.setText(operateTcp.getBackupServerPort());
        etBackupServerAddress.setText(operateTcp.getBackupServerAddress());
        etBackupMnCode.setText(operateTcp.getBackTcpMnCode());
        swBackupServer.setChecked(system.isBackupServerEnable());
        swRhCorrectionEnable.setChecked(system.isRhCorrectionEnable());
        tvDustMeterInfo.setText(dustMeter.getDustMeterWorkedInfo());


        if(system.isNoiseMeterEnable()){
            btnNoiseCal.setVisibility(View.VISIBLE);
        }else{
            btnNoiseCal.setVisibility(View.INVISIBLE);
        }

        if(system.isWindDirLinkageEnable()){
            tvCameraTitle.setVisibility(View.VISIBLE);
            etCameraDirectionOffset.setVisibility(View.VISIBLE);
            btnSetCameraOffset.setVisibility(View.VISIBLE);
        }else{
            tvCameraTitle.setVisibility(View.INVISIBLE);
            etCameraDirectionOffset.setVisibility(View.INVISIBLE);
            btnSetCameraOffset.setVisibility(View.INVISIBLE);
        }
        if(system.isDustMeterSibataLd8()){
            tvDustMeterInfo.setVisibility(View.VISIBLE);
            swDustMeterRun.setEnabled(true);
            tvMotorSettingTitle.setVisibility(View.VISIBLE);
            tvMotorSettingContent1.setVisibility(View.VISIBLE);
            tvMotorSettingContent1.setVisibility(View.VISIBLE);
        }else{
            tvDustMeterInfo.setVisibility(View.GONE);
            swDustMeterRun.setEnabled(false);
            swValve.setText("风扇");
            swFan.setText("备用");
            swBackup.setEnabled(false);
            tvMotorSettingTitle.setVisibility(View.GONE);
            tvMotorSettingContent1.setVisibility(View.GONE);
            tvMotorSettingContent2.setVisibility(View.GONE);
            etMotorRounds.setVisibility(View.GONE);
            etMotorTime.setVisibility(View.GONE);
            btnMotorTestDown.setVisibility(View.GONE);
            btnMotorTestUp.setVisibility(View.GONE);
            btnMotorSetting.setVisibility(View.GONE);
        }

    }

    private void initView(View v){
        spDustMeter = v.findViewById(R.id.spOperateDustMeter);
        spDustName = v.findViewById(R.id.spOperateDust);
        spCameraName = v.findViewById(R.id.spOperateCamera);
        spNoiseName = v.findViewById(R.id.spOperateNoise);
        spLedDisplayName = v.findViewById(R.id.spOperateLedDisplay);
        v.findViewById(R.id.btnOperateManCal).setOnClickListener(this);
        tvDustMeterInfo = v.findViewById(R.id.tvOperateDusterInfo);
        swDustMeterRun = v.findViewById(R.id.swOperateDusterSwitch);
        swValve = v.findViewById(R.id.swOperateSystemDo1);
        swFan = v.findViewById(R.id.swOperateSystemDo2);
        swExt1 = v.findViewById(R.id.swOperateSystemDo3);
        swExt2 = v.findViewById(R.id.swOperateSystemDo4);
        swBackup = v.findViewById(R.id.swOperateSystemDo5);
        btnMotorSetting = v.findViewById(R.id.btnOperateMotorSet);
        btnMotorSetting.setOnClickListener(this);
        etMotorRounds = v.findViewById(R.id.etOperateMotorRounds);
        etMotorTime = v.findViewById(R.id.etOperateMotorTime);
        tvNextAutoCalTime = v.findViewById(R.id.tvOperateNextAutoCal);
        etAutoCalInterval = v.findViewById(R.id.etOperateAutoCalInterval);
        btnSaveAutoCal = v.findViewById(R.id.btnOperateSaveAutoCal);
        swAutoCalibrationEnable = v.findViewById(R.id.swAutoCaliration);
        etServerIp = v.findViewById(R.id.etOperateServerIP);
        etServerPort = v.findViewById(R.id.etOperateServerPort);
        etUpdateSoftwareUrl = v.findViewById(R.id.etOperateUpdateUrl);
        v.findViewById(R.id.btnOperateSaveServer).setOnClickListener(this);
        v.findViewById(R.id.btnOperateUpdateSoftware).setOnClickListener(this);
        tvLocalIp = v.findViewById(R.id.tvOperateLocalIp);
        etSetParaK = v.findViewById(R.id.etOperateParaK);
        etSetParaB = v.findViewById(R.id.etOperateParaB);
        v.findViewById(R.id.btnOperateSetParaK).setOnClickListener(this);
        etMnCode = v.findViewById(R.id.etOperateMnCode);
        tvSoftwareVersion = v.findViewById(R.id.tvOperateSoftwareVerison);
        v.findViewById(R.id.btnOperateSaveAlarm).setOnClickListener(this);
        spProtocol = v.findViewById(R.id.spOperateProtocol);
        etAlarm = v.findViewById(R.id.etOperateAlarm);
        v.findViewById(R.id.btnOperateUpdateSetting).setOnClickListener(this);
        btnMotorTestDown = v.findViewById(R.id.btnOperateTestDown);
        btnMotorTestUp = v.findViewById(R.id.btnOperateTestUp);
        btnNoiseCal = v.findViewById(R.id.btnOperateNoiseCal);
        etLng = v.findViewById(R.id.etOperateLng);
        etLat = v.findViewById(R.id.etoperateLat);
        v.findViewById(R.id.btnOperateSaveLocation).setOnClickListener(this);
        v.findViewById(R.id.btnOperateSaveSetting).setOnClickListener(this);
        tvCameraTitle = v.findViewById(R.id.tvCameraTitle);
        tvMotorSettingTitle = v.findViewById(R.id.tvMotorSettingTitle);
        tvMotorSettingContent1 = v.findViewById(R.id.tvMotorSettingContent1);
        tvMotorSettingContent2 = v.findViewById(R.id.tvMotorSettingContent2);
        swRhCorrectionEnable = v.findViewById(R.id.swRhCorrectionEnable);
        etCameraDirectionOffset = v.findViewById(R.id.etCameraDirectionOffset);
        btnNoiseCal.setOnClickListener(this);
        btnMotorTestDown.setOnClickListener(this);
        btnMotorTestUp.setOnClickListener(this);
        swAutoCalibrationEnable.setOnClickListener(this);
        tvNextAutoCalTime.setOnClickListener(this);
        btnSaveAutoCal.setOnClickListener(this);
        swDustMeterRun.setOnClickListener(this);
        swBackup.setOnClickListener(this);
        swFan.setOnClickListener(this);
        swValve.setOnClickListener(this);
        swExt2.setOnClickListener(this);
        swExt1.setOnClickListener(this);
        swRhCorrectionEnable.setOnClickListener(this);

        btnSetCameraOffset = v.findViewById(R.id.btnCameraDirectionOffset);
        btnSetCameraOffset.setOnClickListener(this);

        etTempIntercept = v.findViewById(R.id.etTempIntercept);
        etTempSlope = v.findViewById(R.id.etTempSlope);
        etHumiIntercept = v.findViewById(R.id.etHumiIntercept);
        etHumiSlope = v.findViewById(R.id.etHumiSlope);
        v.findViewById(R.id.btnSaveHumiTempPara).setOnClickListener(this);

        etBackupMnCode = v.findViewById(R.id.etOperateBackupMnCode);
        etBackupServerAddress = v.findViewById(R.id.etOperateBackupServerIp);
        etBackupServerPort = v.findViewById(R.id.etOperateBackupServerPort);
        swBackupServer = v.findViewById(R.id.swBackupServerEnable);
        swBackupServer.setOnClickListener(this);
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
                Toast.makeText(getActivity(),"设置成功",Toast.LENGTH_SHORT).show();
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
                operateTcp.setTcpSocketClient(getActivity(),etServerIp.getText().toString(),
                        Integer.valueOf(etServerPort.getText().toString()),
                        etMnCode.getText().toString(),clientProtocolName);
                operateTcp.setBackupTcpSocketClient(getActivity(),etBackupServerAddress.getText().toString(),
                        Integer.valueOf(etBackupServerPort.getText().toString()),
                        etBackupMnCode.getText().toString());
                Toast.makeText(getActivity(),"设置成功，重启生效",Toast.LENGTH_LONG).show();
                break;
            case R.id.btnOperateUpdateSoftware:
                //SophixManager.getInstance().queryAndLoadNewPatch();
                dialogFragment = new ProcessDialogFragment();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getFragmentManager(),"DownLoadSoftware");
                system.startDownLoadSoftware(getActivity(),etUpdateSoftwareUrl.getText().toString(),dialogFragment,this);
                break;
            case R.id.btnOperateSaveAlarm:
                system.setAlarmDust(etAlarm.getText().toString());
                Toast.makeText(getActivity(),"设置成功",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(),"设置成功",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOperateNoiseCal:
                processFragment = new ProcessFragment();
                processFragment.setCancelable(false);
                processFragment.show(getFragmentManager(),"calibration noise");
                system.calNoise(this);
                break;
            case R.id.btnOperateSaveLocation:
                operateTcp.setLocation(getActivity(),etLng.getText().toString(),etLat.getText().toString());
                break;
            case R.id.btnCameraDirectionOffset:
                int offset = Integer.valueOf(etCameraDirectionOffset.getText().toString());
                if(offset > 359){
                    Toast.makeText(getActivity(),"参数超范围，不得大于359",Toast.LENGTH_LONG).show();
                }else if(offset <-359){
                    Toast.makeText(getActivity(),"参数超范围，不得小于-359",Toast.LENGTH_LONG).show();
                }else {
                    system.setCameraDirectionOffset(offset);
                    system.saveCameraName(cameraName);
                    Toast.makeText(getActivity(),"设置成功",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnSaveHumiTempPara:
                system.saveTempHumiPara(Float.valueOf(etTempSlope.getText().toString()),Float.valueOf(etTempIntercept.getText().toString()),
                        Float.valueOf(etHumiSlope.getText().toString()),Float.valueOf(etHumiIntercept.getText().toString()));
                        Toast.makeText(getActivity(),"设置成功",Toast.LENGTH_LONG).show();
                break;
            case R.id.swBackupServerEnable:
                system.setBackupServerEnable(swBackupServer.isChecked());
                Toast.makeText(getActivity(),"设置成功,重启生效！",Toast.LENGTH_LONG).show();
                break;
            case R.id.btnOperateSaveSetting:
                system.setSystemSetting(dustName,dustMeterName,cameraName,noiseName,ledDisplayName);
                Intent intent2 = new Intent();
                intent2.setAction("changeDustName");
                intent2.putExtra("name", DevicesManage.DustNames[dustName]);
                getActivity().sendBroadcast(intent2);
                Toast.makeText(getActivity(),"设置成功，重启生效",Toast.LENGTH_LONG).show();
                break;
            case R.id.swRhCorrectionEnable:
                system.setRhCorrectionEnable(swRhCorrectionEnable.isChecked());
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
                dustName = i;
                break;
            case R.id.spOperateDustMeter:
                dustMeterName = i;
                break;
            case R.id.spOperateCamera:
                cameraName = i;
                break;
            case R.id.spOperateLedDisplay:
                ledDisplayName = i;
                break;
            case R.id.spOperateNoise:
                noiseName = i;
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
