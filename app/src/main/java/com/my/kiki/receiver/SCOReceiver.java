package com.my.kiki.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.my.kiki.service.SpeechService;
import com.my.kiki.utils.Utils;

import java.io.Closeable;
import java.io.IOException;

public class SCOReceiver extends BroadcastReceiver implements Closeable {
    private Context activityContext;
    private String TAG="SCO Receiver";
    private Listener mListener;

    public interface Listener{
        void audioStateConnected();
        void audioStateConnecting();
        void audioStateDisconnected();
        void audioStateUnavailable();
        void audioStateError();
    }

    public SCOReceiver(Context ctx, Listener listener){
        activityContext=ctx;
        mListener = listener;
        ctx.registerReceiver(this, new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
        switch (state) {
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                Log.i(TAG, "Bluetooth HFP Headset is connected");
                mListener.audioStateConnected();
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                Log.i(TAG, "Bluetooth HFP Headset is connecting");
                mListener.audioStateConnecting();
                break;
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                mListener.audioStateDisconnected();
                break;
            case AudioManager.SCO_AUDIO_STATE_ERROR:
                Log.i(TAG, "Bluetooth HFP Headset is in error state");
                mListener.audioStateError();
                break;
        }
    }

    @Override
    public void close() throws IOException {
        activityContext.unregisterReceiver(this);
    }
}
