package com.github.ivbaranov.rxbluetooth.example;

import android.app.Application;

/**
 * Created by chm on 2018/7/24.
 */

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
//        Utils.init(this);

    }

    public static App getApp() {
        return app;
    }

}
