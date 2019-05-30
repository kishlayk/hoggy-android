package com.my.kiki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {

    private String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.i(TAG, action);
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
