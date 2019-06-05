package com.my.kiki.receiver;

import android.animation.Animator;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.common.eventbus.EventBus;
import com.my.kiki.R;
import com.my.kiki.databinding.ActivityHomeBinding;
import com.my.kiki.main.MainApplication;
import com.my.kiki.ui.HomeActivity;
import com.my.kiki.utils.Utils;

import java.io.Closeable;
import java.io.IOException;

import static com.my.kiki.utils.Utils.PREF_IS_TOY_SPEAKING;
import static com.my.kiki.utils.Utils.isInternetAvailable;

public class ConnectViaAppReceiver extends BroadcastReceiver implements Closeable {

    private Context mActivityContext;
    private Listener mListener;
    private RelativeLayout alertLayout;

    public interface Listener {
        void callback1();
        void callback2();
        void callback3();
        void getAlertTv(String msg);
        void getdisconnectDevice();
    }

    public ConnectViaAppReceiver(Context ctx, RelativeLayout alertLayout, Listener listener) {
        mActivityContext =ctx;
        mListener = listener;
        this.alertLayout=alertLayout;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BROADCAST_INTENT_DEVICE_CONNECTED);
        filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION);
        mActivityContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                if(isInternetAvailable()){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Utils.getInstance(mActivityContext).setBoolean(PREF_IS_TOY_SPEAKING, false);
                            mListener.callback1();
                        }
                    }, 500);
                    showAlert();

                }else if (!isInternetAvailable()) {
                    showAlert(mActivityContext.getString(R.string.msg_no_internet));
//                        Toast.makeText(HomeActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                } else hideAlert();

            }


            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d("home", "bluetooth strength:" + rssi);
            }

            if (action.equals(Utils.DISCONNECT_TOY_RECEIVER)) {
                Log.d("homeActivity", "disconnect");
                mListener.getdisconnectDevice();
                return;
            }

            if(Utils.BROADCAST_INTENT_DEVICE_CONNECTED.equals(action)) {
                if (intent.getExtras()==null) return;
                if (!intent.getExtras().getBoolean(Utils.EXTRA_DEVICE_IS_CONNECTED)) {
                    if (!intent.getExtras().getBoolean(Utils.EXTRA_IS_TEMP_DISCONNECT)) {
                        Utils.preferencesOndisconnect(mActivityContext);
                    }
                    mListener.callback2();
                } else {
                    AudioManager audioManager = (AudioManager) MainApplication.getGlobalContext().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.isBluetoothA2dpOn()) {
                        if (isInternetAvailable() && MainApplication.isActivityVisible() && audioManager.isBluetoothA2dpOn()) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("is_data_body1", "123");
                                    mListener.callback3();
                                }
                            }, 500);
                            if (isSignalWeak())
                                showAlert("Weak Network Signal");

                        }  else if (!isInternetAvailable()) {
                            showAlert(mActivityContext.getString(R.string.msg_no_internet));
//                            Toast.makeText(HomeActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                        } else hideAlert();
                    } else {
                        Toast.makeText(mActivityContext,mActivityContext.getString(R.string.str_device_unpaired),Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

    }

    @Override
    public void close() throws IOException {
        mActivityContext.unregisterReceiver(this);
    }


    public int getWifiLevel() {
        WifiManager wifiManager = (WifiManager)MainApplication.getGlobalContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(linkSpeed, 5);
        return level;
    }

    private boolean isSignalWeak() {
        boolean isWeak = false;
        ConnectivityManager cm = (ConnectivityManager) MainApplication.getGlobalContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            if (getWifiLevel() < 2)
                isWeak = true;
            isWeak = false;
        }else{
            if(info != null && info.getType() == ConnectivityManager.TYPE_MOBILE){
                Log.d("network type",info.getSubtypeName());
                switch(info.getSubtype()){
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        isWeak = true; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        isWeak = true; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        isWeak = true; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        isWeak = false;// ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        isWeak = false; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        isWeak = true; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        isWeak = false; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        isWeak = false;// ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        isWeak = false; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        isWeak = false; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        isWeak = false; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        isWeak = false; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        isWeak = false; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        isWeak = true; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        isWeak = false; // ~ 10+ Mbps
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        isWeak = true;
                    default:
                        isWeak = false;
                }
            }
        }
        return isWeak;
    }

    public void showAlert(){
        if(isSignalWeak())
            showAlert("Weak Network Signal");
        else if(isToyConnectedPrefs())
            showAlert(mActivityContext.getString(R.string.connected));
        else showAlert(mActivityContext.getString(R.string.toy_not_connected));
    }

    public void showAlert(final String msg){
        alertLayout.setAlpha(0f);
        alertLayout.setVisibility(View.VISIBLE);
        alertLayout.animate().alpha(1f).setDuration(400).setListener(null) ;
        mListener.getAlertTv(msg);
    }

    boolean isToyConnectedPrefs(){
        return Utils.getInstance(mActivityContext).getBoolean(Utils.PREF_IS_TOY_CONNECTED);
    }


    public void hideAlert(){
        alertLayout.animate().alpha(0f).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                alertLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

}

