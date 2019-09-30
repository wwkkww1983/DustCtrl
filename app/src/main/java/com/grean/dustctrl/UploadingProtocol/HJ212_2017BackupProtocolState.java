package com.grean.dustctrl.UploadingProtocol;

/**
 * Created by weifeng on 2019/9/30.
 */

public class HJ212_2017BackupProtocolState extends HJT212_2017ProtocolState{
    public HJ212_2017BackupProtocolState(ProtocolCommand command) {
        super(command);

    }

    @Override
    public void setConfig(UploadingConfigFormat format) {
        super.setConfig(format);
        this.format.setMnCode(format.getBackupMnCode());//将协议内的地址码修改为后备地址码
    }
}
