package com.grean.dustctrl.hardware;

import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2018/5/2.
 */

public interface MainBoardController {
    byte MainBoardAddress =(byte) 0x55,WindForceAddress = (byte) 0xe1,WindDirAddress = (byte)0xe2,AirParameter =(byte) 0xe3;
    /**
     * 处理主板的过程信息
     * @param rec 经过modbus校验过的数组
     * @param size 数组长度
     * @param data 存储数据类型
     */
    void inquireState(byte[] rec, int size, SensorData data);

    /**
     * 处理风力
     * @param rec
     * @param size
     * @param data
     */
    void inquireWindForce(byte[] rec, int size, SensorData data);

    /**
     * 处理风向
     * @param rec
     * @param size
     * @param data
     */
    void inquireWindDir(byte[] rec, int size, SensorData data);

    /**
     * 处理温湿度大气压
     * @param rec
     * @param size
     * @param data
     */
    void inquireAirParameter(byte[] rec, int size, SensorData data);

    /**
     * 发送查询命令
     * @param cmd
     */
    void sendInquireCmd(int cmd, SerialCommunicationController controller);
}
