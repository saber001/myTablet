package com.example.mytablet;

import android.app.Application;
import android.content.Context;
import android.os.yx.YxDeviceManager;

import com.example.mytablet.ui.model.Utils;

public class MyApplication extends Application {
    private static MyApplication instance;
    public YxDeviceManager yxDeviceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Utils.init(this);
        yxDeviceManager = YxDeviceManager.getInstance(this);
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
