package com.grean.dustctrl;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.grean.dustctrl.UploadingProtocol.ProtocolTcpServer;
import com.grean.dustctrl.device.DevicesManage;
import com.grean.dustctrl.presenter.CalcNextAutoCalibration;
import com.grean.dustctrl.presenter.FragmentData;
import com.grean.dustctrl.presenter.FragmentMain;
import com.grean.dustctrl.presenter.FragmentOperate;
import com.grean.dustctrl.presenter.FragmentVideo;
import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.protocol.GetProtocols;
import com.taobao.sophix.SophixManager;
import com.tools;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,CalcNextAutoCalibration {
    private final static  String tag = "MainActivity";
    private View layoutMain;
    private View layoutOperate;
    private View layoutData;
    private View layoutVideo;
    private FragmentMain fragmentMain;
    private FragmentOperate fragmentOperate;
    private FragmentData fragmentData;
    private FragmentVideo fragmentVideo;
    private android.app.FragmentManager fragmentManager;
    private Timer autoCalibrationTimer,autoPatchTimer;
    private static final int msgAutoCalibration = 1,msgAutoPatch =2;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case msgAutoCalibration:
                    ScanSensor.getInstance().calibrationDustMeterWithAuto(MainActivity.this);
                    break;
                case msgAutoPatch:
                    SophixManager.getInstance().queryAndLoadNewPatch();
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//定时自动校准
            if(intent.getAction().equals("autoCalibration")){
                Intent mainFragmentIntent = new Intent();
                mainFragmentIntent.setAction("autoCalNextString");
                ReadWriteConfig config = new SystemSettingStore(MainActivity.this);
                if (intent.getBooleanExtra("enable",true)){
                   config.saveConfig("auto_calibration_enable",true);
                    cancelAutoCalibrationTimer();
                    autoCalibrationTimer = new Timer();
                    Date when = new Date(intent.getLongExtra("date",0l));
                    mainFragmentIntent.putExtra("content",tools.timestamp2string(intent.getLongExtra("date",0l)));
                    autoCalibrationTimer.schedule(new AutoCalibrationTimerTask(),when);
                }else {
                    config.saveConfig("auto_calibration_enable",false);
                    cancelAutoCalibrationTimer();
                    mainFragmentIntent.putExtra("content","-");
                }
                sendBroadcast(mainFragmentIntent);
            }

        }
    };

    @Override
    public void onComplete() {
        Log.d(tag,"计算下次测量时间");
        ReadWriteConfig config = new SystemSettingStore(this);
        long now = tools.nowtime2timestamp();
        long plan = config.getConfigLong("auto_calibration_date");
        long interval = config.getConfigLong("auto_calibration_interval");
        long next = tools.calcNextTime(now,plan,interval);
        config.saveConfig("auto_calibration_date",next);
        cancelAutoCalibrationTimer();
        if(config.getConfigBoolean("auto_calibration_enable")) {
            autoCalibrationTimer = new Timer();
            Date when = new Date(next);
            autoCalibrationTimer.schedule(new AutoCalibrationTimerTask(), when);
            Intent intent = new Intent();
            intent.setAction("autoCalNextString");
            intent.putExtra("content", tools.timestamp2string(next));
            sendBroadcast(intent);
            Log.d(tag, "计算下次测量时间" + tools.timestamp2string(next));
        }
    }


    private class AutoCalibrationTimerTask extends TimerTask{

        @Override
        public void run() {
            handler.sendEmptyMessage(msgAutoCalibration);
        }
    }

    private class AutoPatchTimer extends TimerTask{

        @Override
        public void run() {
            handler.sendEmptyMessage(msgAutoPatch);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    private void cancelAutoCalibrationTimer(){
        if(autoCalibrationTimer!=null){
            autoCalibrationTimer.cancel();
            autoCalibrationTimer = null;
        }
    }

    // private Button btnTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        fragmentManager = getFragmentManager();
        setTabSelection(0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("autoCalibration");
        registerReceiver(broadcastReceiver,intentFilter);
        SystemSettingStore settingStore = new SystemSettingStore(this);
        settingStore.loadDeviceSetting(DevicesManage.getInstance());
        settingStore.loadUploadSetting(ProtocolTcpServer.getInstance());
        Log.d(tag,"test 4");

        GetProtocols.getInstance().setContext(this);
        GetProtocols.getInstance().setClientProtocol(settingStore.getSettingInt("ClientProtocol"));
        GetProtocols.getInstance().getInfoProtocol().loadSetting(settingStore);
        GetProtocols.getInstance().getInfoProtocol().setContext(this);
        DevicesManage.getInstance().initDevice();

        ScanSensor.getInstance().addObserver(SystemLog.getInstance(this));
        ScanSensor.getInstance().startScan(this);
        ProtocolTcpServer.getInstance().setState(GetProtocols.getInstance().getProtocolState());
        ProtocolTcpServer.getInstance().connectServer(this);//启动外网服务

        SocketServerTask.getInstance().startSocketServer(GetProtocols.getInstance().getServerProtocol(),8888);//启动内网服务

        autoPatchTimer = new Timer();
        long now = tools.nowtime2timestamp();
        long next = tools.calcNextTime(now,1524069000000l,4*3600000l);
        Date when = new Date(next);
        autoPatchTimer.schedule(new AutoPatchTimer(),when,4*3600000l);

        ScanSensor.getInstance().setProtocolState(GetProtocols.getInstance().getProtocolState());
        if(ProtocolTcpServer.getInstance().getFormat().isBackupServerEnable()){
            Log.d(tag,"启动backup服务");
            ProtocolTcpServer.getInstance().setBackupState(GetProtocols.getInstance().getBackupProtocolState());
            ProtocolTcpServer.getInstance().connectBackupServer(this);
            ScanSensor.getInstance().setBackupProtocolState(GetProtocols.getInstance().getBackupProtocolState());
        }
    }

    private void initView(){
        layoutData = findViewById(R.id.dataLayout);
        layoutMain = findViewById(R.id.mainLayout);
        layoutOperate = findViewById(R.id.operateLayout);
        layoutVideo = findViewById(R.id.videoLayout);
        layoutVideo.setOnClickListener(this);
        layoutOperate.setOnClickListener(this);
        layoutMain.setOnClickListener(this);
        layoutData.setOnClickListener(this);
    }

    /**
     * 清除选中状态
     */
    private void clearSelection(){


    }

    private void  hideFragment(FragmentTransaction transaction){
        if(fragmentMain!=null){
            transaction.hide(fragmentMain);
        }
        if(fragmentOperate!=null){
            transaction.hide(fragmentOperate);
        }
        if(fragmentData!=null){
            transaction.hide(fragmentData);
        }
        if(fragmentVideo!=null){
            transaction.hide(fragmentVideo);
        }
    }

    private void setTabSelection(int index){
        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragment(transaction);
        switch (index){
            case 0:
            default:
                if(fragmentMain == null){
                    fragmentMain = new FragmentMain();
                    transaction.add(R.id.content,fragmentMain).commit();
                }else{
                    if (fragmentMain.isAdded()){
                        transaction.show(fragmentMain).commit();
                    }else{
                        transaction.add(R.id.content,fragmentMain).commit();
                    }
                }
                break;
            case 1:
                if(fragmentOperate==null){
                    fragmentOperate = new FragmentOperate();
                    transaction.add(R.id.content,fragmentOperate).commit();
                }else{
                    if(fragmentOperate.isAdded()){
                        transaction.show(fragmentOperate).commit();
                    }else{
                        transaction.add(R.id.content,fragmentOperate).commit();
                    }
                }
                break;
            case 2:
                if(fragmentData==null){
                    fragmentData = new FragmentData();
                    transaction.add(R.id.content,fragmentData).commit();
                }else{
                    if(fragmentData.isAdded()){
                        transaction.show(fragmentData).commit();
                    }else{
                        transaction.add(R.id.content,fragmentData).commit();
                    }
                }
                break;
            case 3:
                if(fragmentVideo==null){
                    fragmentVideo = new FragmentVideo();
                    transaction.add(R.id.content,fragmentVideo).commit();
                }else{
                    if(fragmentVideo.isAdded()){
                        transaction.show(fragmentVideo).commit();
                    }else {
                        transaction.add(R.id.content,fragmentVideo).commit();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
       // return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mainLayout:
                setTabSelection(0);
                break;
            case R.id.operateLayout:
                setTabSelection(1);
                break;
            case R.id.dataLayout:
                setTabSelection(2);
                break;
            case R.id.videoLayout:
                setTabSelection(3);
                break;
            default:
                break;
        }
    }

}
