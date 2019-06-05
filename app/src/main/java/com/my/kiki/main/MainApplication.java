package com.my.kiki.main;

import android.app.Application;
import android.content.Context;

import com.my.kiki.utils.Utils;


public class MainApplication extends Application {

    private static Context context;
    private static boolean activityVisible;
    @Override
    public void onCreate() {
        super.onCreate();

        context = null;
        context = getApplicationContext();

        Utils.refreshState(context);
        activityVisible=true;

    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }


    public static MainApplication create(Context context) {
        return MainApplication.get(context);
    }

    private static MainApplication get(Context context) {
        return (MainApplication) context.getApplicationContext();
    }




    public static Context getGlobalContext() {
        return context;
    }


}
