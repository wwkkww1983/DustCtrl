package com.grean.dustctrl.protocol;


import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/9/8.
 */

public class InformationProtocol implements GeneralInfoProtocol{
    private String stateString;
    private SensorData data = new SensorData();
    private boolean autoCalEnable;
    private long autoCalTime,autoCalInterval;
    private String serverIp;
    private int serverPort;
    @Override
    public String getSystemState() {
        return stateString;
    }

    @Override
    public SensorData getSensorData() {
        return data;
    }

    @Override
    public void notifySenorData(SensorData data) {
        this.data.setParaK(data.getParaK());
        this.data.setValue(data.getValue());
        this.data.setAirTemperature(data.getAirTemperature());
        this.data.setAirHumidity(data.getAirHumidity());
        this.data.setAirPressure(data.getAirPressure());
        this.data.setNoise(data.getNoise());
        this.data.setWindDirection(data.getWindDirection());
        this.data.setWindForce(data.getWindForce());
    }

    @Override
    public void notifySystemState(String string) {
        this.stateString = string;
    }

    public void loadSetting(ReadWriteConfig config){
        serverIp = config.getConfigString("ServerIp");
        serverPort = config.getConfigInt("ServerPort");
        autoCalEnable = config.getConfigBoolean("AutoCalibrationEnable");
        autoCalTime = config.getConfigLong("AutoCalTime");
        autoCalInterval = config.getConfigLong("AutoCalInterval");
    }

    @Override
    public String getServerIp() {
        return serverIp;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public boolean getAutoCalEnable() {
        return autoCalEnable;
    }

    @Override
    public long getAutoCalTime() {
        return autoCalTime;
    }

    @Override
    public long getAutoCalInterval() {
        return autoCalInterval;
    }
}
