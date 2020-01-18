package com.app.synclient;

public class GlobalSetting {
    // rest service
    public static final String PULL_REST = "https://dragino.000webhostapp.com/pull.php";
    // cara close update agar is_update jadi 0
    // https://dragino.000webhostapp.com/push.php?act=close_update&devid=ADS-123
    public static final String PUSH_REST = "https://dragino.000webhostapp.com/push.php";
    // device ID
    public static final String DEVICE_ID = "ADS-123";
}
