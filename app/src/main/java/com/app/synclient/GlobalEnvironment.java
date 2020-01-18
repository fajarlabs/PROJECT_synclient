package com.app.synclient;

import android.os.Environment;

import java.io.File;

public class GlobalEnvironment {
    public static final File path = DashboardActivity.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
}
