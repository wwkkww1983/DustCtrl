package com.grean.dustctrl.device;

import android.util.Log;

import com.grean.dustctrl.ComFourCommunication;
import com.grean.dustctrl.ComOneCommunication;
import com.grean.dustctrl.ComThreeCommunication;
import com.grean.dustctrl.ComTwoCommunication;
import com.grean.dustctrl.NoiseCalibrationListener;
import com.grean.dustctrl.presenter.NotifyProcessDialogInfo;
import com.grean.dustctrl.process.NotifyMainFragment;
import com.grean.dustctrl.process.NotifySystemLog;
import com.grean.dustctrl.process.SensorData;
import com.grean.dustctrl.protocol.GeneralInfoProtocol;

import java.util.Observable;

/**
 * Created by weifeng on 2020/3/2.
 */

public class DevicesManage {
    private static DevicesManage instance = new DevicesManage();
    private SensorData data = new SensorData();
    private final String tag = "DevicesManage";
    public static final String[] DustMeterNames ={"LD-8-G","LPM-1000 V517.a","LPM-1000 V519.w","LPM-1000 V519.a"};
    public static final String[] DustNames = {"TSP","PM10","PM2.5"};
    public static final String[] CameraNames ={"DS-2DF8225IH-黑光","iDS-2DE7223-白光","Other"};
    public static final String[] NoiseNames = {"None","AWA5636-7"};
    public static final String[] LedDisplayNames = {"None","灵信T2 64*32"};

    private int dustMeterName=0,cameraName=0,noiseName=0,ledDisplayName = 0,dustName=0,
            defaultCameraDirectionOffset = 0;
    private boolean dustMeterRun = true;

    private DustMeterControl dustMeterControl = null;
    private CameraControl cameraControl = null;
    private LedDisplayControl ledDisplayControl = null ;
    private NoiseControl noiseControl = null;
    private PeripheralControl peripheralControl = null;
    private WeatherControl weatherControl = null;

    private DevicesManage(){

    }

    public void setMotor(int fun){
        if(dustMeterControl!=null){
            dustMeterControl.setMotor(fun);
        }
    }

    public void setLedDisplayName(int ledDisplayName) {
        this.ledDisplayName = ledDisplayName;
    }

    public int getDustName() {
        return dustName;
    }

    public void setDustName(int dustName) {
        this.dustName = dustName;
    }

    public int getDustMeterName() {
        return dustMeterName;
    }

    public int getCameraName() {
        return cameraName;
    }

    public int getNoiseName() {
        return noiseName;
    }

    public int getLedDisplayName() {
        return ledDisplayName;
    }

    public void setDustMeterName(int dustMeterName) {
        this.dustMeterName = dustMeterName;
    }

    public void setCameraName(int cameraName) {
        this.cameraName = cameraName;
    }

    public void setNoiseName(int noiseName) {
        this.noiseName = noiseName;
    }

    public static DevicesManage getInstance() {
        return instance;
    }

    /**
     * 依据配置启动对应设备的实例
     */
    public void initDevice(){
        if(dustMeterControl==null) {
            if (dustMeterName == 0) {
                dustMeterControl = new DustMeterSibataLd8(data, ComOneCommunication.getInstance());
                weatherControl = null;
            }else if(dustMeterName == 1){
                dustMeterControl = new DustMeterLyjdLpm1000(ComFourCommunication.getInstance(),data);
                weatherControl = new WeatherMiniUltrasonic(ComThreeCommunication.getInstance(),data);
                peripheralControl = new PeripheralModBusIo(ComOneCommunication.getInstance(),data);
            }else if(dustMeterName == 2){
                dustMeterControl = new DustMeterLyjdLpm1000V519(ComFourCommunication.getInstance(),data);
                weatherControl = new WeatherWs5pUltrasonic(ComThreeCommunication.getInstance(),data);
                peripheralControl = new PeripheralModBusIo(ComOneCommunication.getInstance(),data);
            }else if(dustMeterName == 3){
                dustMeterControl = new DustMeterLyjdLpm1000V519(ComFourCommunication.getInstance(),data);
                weatherControl = new WeatherMiniUltrasonic(ComThreeCommunication.getInstance(),data);
                peripheralControl = new PeripheralModBusIo(ComOneCommunication.getInstance(),data);
            }else {
                dustMeterControl = new DustMeterSibataLd8(data, ComOneCommunication.getInstance());
                weatherControl = null;
            }
        }
        if(cameraControl == null) {
            if (cameraName == 0) {
                cameraControl = new CameraBlack(ComThreeCommunication.getInstance(), data);
            } else if (cameraName == 1) {
                cameraControl = new CameraWhite(data);
            } else {
                cameraControl = new CameraWhite(data);
            }
            cameraControl.startServer();
            cameraControl.setDirectionOffset(defaultCameraDirectionOffset);
        }
        if(ledDisplayControl == null){
            if(ledDisplayName == 1){
                ledDisplayControl = new LedDisplayListenVisionT2();
                ledDisplayControl.startServer();
            }
        }
        if(noiseControl==null){
            if(noiseName==1){
                noiseControl = new NoiseAwa5636_7(ComTwoCommunication.getInstance(),data);
            }
        }

        if(weatherControl==null){
            if(dustMeterName == 1){
                weatherControl = new WeatherMiniUltrasonic(ComThreeCommunication.getInstance(),data);
            }
        }

    }

    public int getCameraOffset(){
        if(cameraControl!=null){
            return cameraControl.getDirectionOffset();
        }else{
            return 0;
        }
    }

    public void calibrationNoise(NoiseCalibrationListener listener){
        if(noiseControl!=null){
            noiseControl.calibrationNoise(listener);
        }
    }

    public SensorData getData() {
        return data;
    }

    public void inquire(){
        if(dustMeterControl!=null){
            dustMeterControl.inquire();
        }
        if(noiseControl!=null){
            noiseControl.inquire();
        }
        if(weatherControl!=null){
            weatherControl.inquire();
        }
        if(peripheralControl!=null){
            peripheralControl.fanRelay();
        }
        if(weatherControl!=null){
            weatherControl.inquire();
        }
    }

    public void onMinDataResult(SensorData sensorData){
        if(ledDisplayControl!=null){
            ledDisplayControl.onResult(sensorData);
        }
    }

    public void setCameraDirectionOffset( int directionOffset){
        if(cameraControl!=null){
            cameraControl.setDirectionOffset(directionOffset);
        }else{
            defaultCameraDirectionOffset = directionOffset;
        }
    }

    public void onMinResultAlarm(float minDust){
        if(peripheralControl!=null){
            if(minDust > data.getDustAlarm()){
                peripheralControl.minDataRelay(true);
            }else{
                peripheralControl.minDataRelay(false);
            }
        }
    }

    public  void calibrationDustMeter(GeneralInfoProtocol infoProtocol, NotifySystemLog systemLog,
                                      NotifyProcessDialogInfo dialogInfo, NotifyMainFragment notifyMainFragment){
        if(dustMeterControl!=null){
            dustMeterControl.calibrationDustMeter(infoProtocol, systemLog, dialogInfo, notifyMainFragment);
        }
    }

    public void inquireDustMeterWorkedTime(){
        if(dustMeterControl!=null){
            dustMeterControl.inquireDustMeterWorkedTime();
        }

    }

    public void setAlarmRelay(boolean key){
        if(dustMeterControl!=null){
            dustMeterControl.setAlarmRelay(key);
        }
        if(peripheralControl!=null){
            peripheralControl.realTimeDataRelay(key);
        }
    }

    public void setDo(int num , boolean key){
        if(dustMeterControl!=null){
            dustMeterControl.setDo(num,key);
        }

        if(peripheralControl!=null){
            peripheralControl.controlRelays(num-1,key);
        }
    }

    public boolean isSibataLd8(){
        if(dustMeterName == 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean isLyjdLpm1000(){
        if(dustMeterName == 1){
            return true;
        }else{
            return false;
        }
    }

    public boolean isWindDirLinkageEnable(){
        if (cameraName < 2){
            return true;
        }else{
            return false;
        }
    }

    public boolean isNoiseMeterEnable(){
        if(noiseName !=0){
            return true;
        }else{
            return false;
        }
    }

    public void setDustMeterRun(boolean key){
        dustMeterRun = key;
        if(dustMeterControl != null){
            dustMeterControl.setDustMeterRun(key);
        }
    }

    public boolean isDustMeterRun() {
        return dustMeterRun;
    }

    public boolean isLedDisplayEnable(){
        if(ledDisplayName > 0){
            return true;
        }else{
            return false;
        }
    }
}
