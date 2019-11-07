package com.grean.dustctrl.UploadingProtocol;

/**
 * Created by weifeng on 2019/11/7.
 */

public interface CameraControl {
    void setDirectionOffset(int directionOffset);
    void setWindDirection(int windDirection);
    void connectServer(String ip,int port);
}
