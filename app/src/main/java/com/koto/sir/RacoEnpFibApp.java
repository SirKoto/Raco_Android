package com.koto.sir;

import android.app.Application;
import android.content.Context;

public class RacoEnpFibApp extends Application {

    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        RacoEnpFibApp.sContext = getApplicationContext();
    }

    public static Context getAppContext(){
        return RacoEnpFibApp.sContext;
    }
}
