package com.my.kiki.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.my.kiki.R;
import com.my.kiki.main.MainApplication;
import com.my.kiki.ui.ConnectedToyActivity;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;

import java.io.Closeable;
import java.io.IOException;

import static com.my.kiki.utils.Utils.isInternetAvailable;
import static com.my.kiki.utils.Utils.toyCorrectlyConnected;

public class ACLReceiver extends BroadcastReceiver implements Closeable {

    public interface Listener{
        void onConnected();
        void onDisconnected();
    }

    private Context activityContext;
    private Listener callbackListener;
    public ACLReceiver(Context ctx, Listener listener){
        activityContext=ctx;
        callbackListener=listener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        activityContext.registerReceiver(this, filter);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (!toyCorrectlyConnected(device, activityContext)){
            Toast.makeText(activityContext, "Toy not connected. Connect to Pet Signer", Toast.LENGTH_SHORT);
            return;
        }

        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
            Log.d("home","bluetooth strength:" + rssi);
            LogUtils.i("BluetoothListActivity"+" aclReceiver onReceive Device found "+device.getAddress()+ " "+device.getName());
        }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())){
            if(isInternetAvailable()) {
                LogUtils.i("BluetoothListActivity" + "onReceive Device is now connected "+device.getAddress()+ " "+device.getName());
                 if (MainApplication.isActivityVisible() && toyCorrectlyConnected(device, activityContext)) {
                        Utils.insertToyifNew(activityContext, device.getName(), device.getAddress());
                        Utils.preferencesOnConnect(activityContext, device.getName(), device.getAddress());
                        callbackListener.onConnected();
                 }
            } else {
                Toast.makeText(activityContext, activityContext.getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
            LogUtils.i("BluetoothListActivity"+" aclReceiver onReceive Done searching "+device.getAddress()+ " "+device.getName());
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(intent.getAction())) {
            LogUtils.i("BluetoothListActivity"+" aclReceiver onReceive Device is about to disconnect "+device.getAddress()+ " "+device.getName());
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            LogUtils.i("BluetoothListActivity"+" mReceiverfgh BT onReceive Device has disconnected "+device.getAddress()+ " "+device.getName());
            callbackListener.onDisconnected();
            Utils.preferencesOndisconnect(activityContext);
        }
    }

    @Override
    public void close() throws IOException {
        activityContext.unregisterReceiver(this);
    }
}
