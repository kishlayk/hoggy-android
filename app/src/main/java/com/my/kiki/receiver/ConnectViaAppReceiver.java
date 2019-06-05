package com.my.kiki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    public interface Listener {
        void callback1();
        void callback2();
        void callback3();
    }

    public ConnectViaAppReceiver(Context ctx, Listener listener) {
        mActivityContext =ctx;
        mListener = listener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BROADCAST_INTENT_DEVICE_CONNECTED);
        filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION);
        mActivityContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String action = intent.getAction();
//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
//                if(isInternetAvailable()){
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.getInstance(mActivityContext).setBoolean(PREF_IS_TOY_SPEAKING, false);
//                            mListener.callback1();
//                        }
//                    }, 500);
//
//                }else{
//                    Toast.makeText(mActivityContext, mActivityContext.getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
//                }

//            }

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
                        } else {
                            Toast.makeText(mActivityContext, mActivityContext.getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                        }
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
}

