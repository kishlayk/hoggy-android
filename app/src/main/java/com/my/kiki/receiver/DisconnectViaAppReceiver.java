package com.my.kiki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.my.kiki.db.MyDatabase;
import com.my.kiki.ui.BluetoothAvailableListActivity;
import com.my.kiki.ui.BluetoothListActivity;

public class DisconnectViaAppReceiver extends BroadcastReceiver {

    private Listener mListener;

    public interface Listener{
        public void callback();
    }

    public DisconnectViaAppReceiver(Listener listener){
        mListener=listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BluetoothListActivity", " disconnectViaAppReceiver onReceive ");

        //  Toast.makeText(BluetoothListActivity.this,getString(R.string.str_device_unpaired),Toast.LENGTH_SHORT).show();

        /*db.pairedDevicesDAO().deletePairedDevice(arrPairedDevices.get(selectedPos).getDeviceName(),arrPairedDevices.get(selectedPos).getDeviceAddress());
        arrPairedDevices.remove(selectedPos);
        loadFromDb();
        startActivity(new Intent(BluetoothListActivity.this, BluetoothAvailableListActivity.class));*/

        //new code
        mListener.callback();
    }

}
