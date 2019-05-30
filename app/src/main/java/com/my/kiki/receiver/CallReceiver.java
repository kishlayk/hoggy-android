package com.my.kiki.receiver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.my.kiki.main.MainApplication;
import com.my.kiki.service.Connector;
import com.my.kiki.service.SpeechService;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;

import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.my.kiki.utils.Utils.PREF_IS_SPEAKING;
import static com.my.kiki.utils.Utils.PREF_NAME;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        SharedPreferences.Editor editor = null;
        editor = MainApplication.getGlobalContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_IS_SPEAKING, true);
        editor.commit();
        //
        LogUtils.i("CallReceiver"+" onIncomingCallReceived ");
       // Toast.makeText(ctx,"onIncomingCallReceived",Toast.LENGTH_SHORT).show();
       connectDisconnectDevice(ctx);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        SharedPreferences.Editor editor = null;
        editor = MainApplication.getGlobalContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_IS_SPEAKING, true);
        editor.commit();
        //
        LogUtils.i("CallReceiver"+" onIncomingCallAnswered ");
     //   Toast.makeText(ctx,"onIncomingCallAnswered",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIncomingCallEnded(final Context ctx, String number, Date start, Date end)
    {

        LogUtils.i("CallReceiver"+" onIncomingCallEnded ");
     //   Toast.makeText(ctx,"onIncomingCallEnded",Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDisconnectDevice(ctx);
            }
        },1000);
       /* editor.putBoolean(PREF_IS_SPEAKING, true);
        editor.commit();*/
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        SharedPreferences.Editor editor = null;
        editor = MainApplication.getGlobalContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_IS_SPEAKING, true);
        editor.commit();
        //
        LogUtils.i("CallReceiver"+" onOutgoingCallStarted ");
     //   Toast.makeText(ctx,"onOutgoingCallStarted",Toast.LENGTH_SHORT).show();
       connectDisconnectDevice(ctx);
    }

    @Override
    protected void onOutgoingCallEnded(final Context ctx, String number, Date start, Date end)
    {

        //
        LogUtils.i("CallReceiver"+" onOutgoingCallEnded ");
     //   Toast.makeText(ctx,"onOutgoingCallEnded",Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDisconnectDevice(ctx);

            }
        },3000);
    }

    @Override
    protected void onMissedCall(final Context ctx, String number, Date start)
    {

        //
       /* editor.putBoolean(PREF_IS_SPEAKING, false);
        editor.commit();*/
        LogUtils.i("CallReceiver"+" onMissedCall ");
     //   Toast.makeText(ctx,"onMissedCall",Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDisconnectDevice(ctx);

            }
        },3000);

    }

    private void connectDisconnectDevice(Context ctx) {
        LogUtils.i("CallReceiver"+" connectDisconnectDevice PREF_CONNECTED_DEVICE_MAC "+Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_MAC));
        LogUtils.i("CallReceiver"+" connectDisconnectDevice PREF_CONNECTED_DEVICE_NAME "+Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_NAME));
        if (Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_MAC) != null && !Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_MAC).equals("")
                && Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
            Intent intent = new Intent(ctx, Connector.class);
            intent.putExtra("ID", 100);
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_MAC, Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_MAC));
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_NAME, Utils.getInstance(ctx).getString(Utils.PREF_CONNECTED_DEVICE_NAME));
            intent.putExtra(Utils.EXTRA_IS_FROM_CALL_RECEIVER,true);

            LogUtils.i("CallReceiver"+" connectDisconnectDevice isAppOnForeground "+isAppOnForeground(ctx)+" "+isMyServiceRunning(SpeechService.class));
            try {
                if (isMyServiceRunning(SpeechService.class))
                    ctx.startService(intent);
            } catch (Exception e) {
                Log.e("iserrorcheck", "Error shutting down the gRPC channel.", e);
            }

          /*  if (isAppOnForeground(ctx)) {

            }*/
        }
    }


    private boolean isAppOnForeground(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            Log.v("is_call_check",appProcess+"");
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MainApplication.getGlobalContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
