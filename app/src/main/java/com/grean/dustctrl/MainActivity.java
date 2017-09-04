package com.grean.dustctrl;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.grean.dustctrl.presenter.CalcNextAutoCalibration;
import com.grean.dustctrl.presenter.FragmentData;
import com.grean.dustctrl.presenter.FragmentMain;
import com.grean.dustctrl.presenter.FragmentOperate;
import com.grean.dustctrl.presenter.FragmentVideo;
import com.grean.dustctrl.process.ScanSensor;
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
    private Timer autoCalibrationTimer;
    //private Fragment lastFragment;
    private static final int msgAutoCalibration = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case msgAutoCalibration:

                    ScanSensor.getInstance().calibrationDustMeterWithAuto(MainActivity.this);
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
                if (intent.getBooleanExtra("enable",true)){
                    cancelAutoCalibrationTimer();
                    autoCalibrationTimer = new Timer();
                    Date when = new Date(intent.getLongExtra("date",0l));
                    mainFragmentIntent.putExtra("content",tools.timestamp2string(intent.getLongExtra("date",0l)));
                    autoCalibrationTimer.schedule(new AutoCalibrationTimerTask(),when);
                }else {
                    cancelAutoCalibrationTimer();
                    mainFragmentIntent.putExtra("content","-");
                }
                sendBroadcast(mainFragmentIntent);
            }

        }
    };

    @Override
    public void onComplete() {
        long now = tools.nowtime2timestamp();
        long plan = myApplication.getInstance().getConfigLong("AutoCalTime");
        long interval = myApplication.getInstance().getConfigInt("AutoCalInterval");
        long next = tools.calcNextTime(now,plan,interval);
        myApplication.getInstance().saveConfig("AutoCalTime",next);
        cancelAutoCalibrationTimer();
        autoCalibrationTimer = new Timer();
        Date when = new Date(next);
        autoCalibrationTimer.schedule(new AutoCalibrationTimerTask(),when);
        Intent intent = new Intent();
        intent.setAction("autoCalNextString");
        intent.putExtra("content",tools.timestamp2string(next));
        sendBroadcast(intent);
    }

    class AutoCalibrationTimerTask extends TimerTask{

        @Override
        public void run() {
            handler.sendEmptyMessage(msgAutoCalibration);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        /*btnTest = (Button) findViewById(R.id.testBtn);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CtrlCommunication.getInstance().SendFrame(CtrlCommunication.Cmd.Inquire);
            }
        });*/

        ScanSensor.getInstance().addObserver(SystemLog.getInstance(this));
        ScanSensor.getInstance().startScan(this);

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

    /*public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {

            String path = "/dev/ttyS0";
            Log.d(tag,path);
            int baudrate = 9600;//Integer.decode("9600");

			// Check parameters
            if ( (path.length() == 0) || (baudrate == -1)) {
                Log.d(tag,"error");
                throw new InvalidParameterException();
            }
            Log.d(tag,"right");
			// Open the serial port
            mSerialPort = new SerialPort(new File(path), 9600, 0);
        }
        return mSerialPort;
    }*/
/*
    private class  ReadThread extends  Thread{
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()){
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) {
                        Log.d(tag,"end");
                        return;
                    }

                    while (mInputStream.available()==0){
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        //onDataReceived(buffer, size);
                        Log.d(tag, "sizeof"+String.valueOf(size)+":"+tools.bytesToHexString(buffer,size));
                    }
                    else{
                        Log.d(tag,"End Receive");
                    }
                    Log.d(tag,"rec");
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

        }
    }*/
}
