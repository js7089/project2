package com.example.q.project2;

import android.app.Application;
import android.content.Context;


public class project2 extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        project2.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return project2.context;
    }
}
