package com.grean.dustctrl.device;

/**
 * Created by weifeng on 2020/3/2.
 */

public interface CameraControl {
    int CameraSetDirection = 101;
    /**
     * 设置方向偏移
     * @param directionOffset
     */
    void setDirectionOffset(int directionOffset);

    /**
     * 获取偏移角
     * @return
     */
    int getDirectionOffset();

    /**
     * 启动风向联动服务
     */
    void startServer();
}
