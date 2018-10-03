package com.inducesmiles.projects.lightscanner;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lekan Adigun on 1/28/2018.
 */

public class LightScannerApplication extends Application {


    private static LightScannerApplication application;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static LightScannerApplication getApplication() {
        return application;
    }
}
