package com.samsung.hackathon.drawtogether;

import android.app.Application;
import android.content.Context;

import com.alterego.advancedandroidlogger.implementations.DetailedAndroidLogger;
import com.alterego.advancedandroidlogger.interfaces.IAndroidLogger;

public class App extends Application {

    public static DetailedAndroidLogger L;
    public static Context m_instance;

    @Override
    public void onCreate() {
        super.onCreate();
        L = new DetailedAndroidLogger("DrawTogether", IAndroidLogger.LoggingLevel.DEBUG);
        m_instance = this;
    }
}
