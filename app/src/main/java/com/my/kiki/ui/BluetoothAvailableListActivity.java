package com.my.kiki.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.my.kiki.R;
import com.my.kiki.adapter.DeviceRecyclerViewAdapter;
import com.my.kiki.bluetooth.BluetoothController;
import com.my.kiki.bluetooth.ListInteractionListener;
import com.my.kiki.databinding.ActivityBluetoothAvailableListBinding;
import com.my.kiki.db.MyDatabase;
import com.my.kiki.main.MainApplication;
import com.my.kiki.model.PairedDevices;
import com.my.kiki.service.Connector;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;

import java.util.List;

import static com.my.kiki.utils.Utils.isInternetAvailable;

public class BluetoothAvailableListActivity extends AppCompatActivity implements View.OnClickListener,  ListInteractionListener<BluetoothDevice> {

    ActivityBluetoothAvailableListBinding binding;

    private DeviceRecyclerViewAdapter recyclerViewAdapter;
    private BluetoothController bluetooth;

    private ProgressDialog bondingProgressDialog;

    private static final String TAG = "MainActivity";

    MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_available_list);
        initToolbar();

        db = MyDatabase.getDataBase(this);

        binding.lyToolBar.ivBack.setOnClickListener(this);

        recyclerViewAdapter = new DeviceRecyclerViewAdapter(this);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setEmptyView(binding.emptyList);
        binding.list.setProgressView(binding.progressBar);
        binding.list.setAdapter(recyclerViewAdapter);

        // [#11] Ensures that the Bluetooth is available on this device before proceeding.
        boolean hasBluetooth = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        if(!hasBluetooth) {
            AlertDialog dialog = new AlertDialog.Builder(BluetoothAvailableListActivity.this).create();
            dialog.setTitle(getString(R.string.bluetooth_not_available_title));
            dialog.setMessage(getString(R.string.bluetooth_not_available_message));
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Closes the dialog and terminates the activity.
                            dialog.dismiss();
                            finish();
                        }
                    });
            dialog.setCancelable(false);
            dialog.show();
        }

        // Sets up the bluetooth controller.
        this.bluetooth = new BluetoothController(this, BluetoothAdapter.getDefaultAdapter(), recyclerViewAdapter);

//        binding.fab = (FloatingActionButton) findViewById(R.id.fab);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If the bluetooth is not enabled, turns it on.
                if (!bluetooth.isBluetoothEnabled()) {
                    Snackbar.make(view, R.string.enabling_bluetooth, Snackbar.LENGTH_SHORT).show();
                    bluetooth.turnOnBluetoothAndScheduleDiscovery();
                } else {
                    //Prevents the user from spamming the button and thus glitching the UI.
                    if (!bluetooth.isDiscovering()) {
                        // Starts the discovery.
                        Snackbar.make(view, R.string.device_discovery_started, Snackbar.LENGTH_SHORT).show();
                        bluetooth.startDiscovery();
                    } else {
                        Snackbar.make(view, R.string.device_discovery_stopped, Snackbar.LENGTH_SHORT).show();
                        bluetooth.cancelDiscovery();
                    }
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//        this.registerReceiver(mReceiver, filter);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.ivBack:
                finish();
                break;
        }
    }

    public void initToolbar() {
        setSupportActionBar(binding.lyToolBar.toolbar);
        getSupportActionBar().setTitle("");

        TextView tvTitle = binding.lyToolBar.toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.lbl_available_list));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }




    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(BluetoothDevice device) {
        Log.d(TAG, "Item clicked : " + BluetoothController.deviceToString(device));
        if (bluetooth.isAlreadyPaired(device)) {
            Log.d(TAG, "Device already paired!"+device.getAddress());
            //insertPaired(device);

            Intent intent = new Intent(this, Connector.class);
            intent.putExtra("ID", 100);
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_MAC, device.getAddress());
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_NAME, device.getName());
            intent.putExtra(Utils.EXTRA_IS_FROM_CALL_RECEIVER,false);
            startService(intent);


            finish();
//            Toast.makeText(this, R.string.device_already_paired, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Device not paired. Pairing.");
            boolean outcome = bluetooth.pair(device);

            // Prints a message to the user.
            String deviceName = BluetoothController.getDeviceName(device);
            if (outcome) {
                // The pairing has started, shows a progress dialog.
                Log.d(TAG, "Showing pairing dialog");
                bondingProgressDialog = ProgressDialog.show(this, "", "Pairing with device " + deviceName + "...", true, false);

            } else {
                Log.d(TAG, "Error while pairing with device " + deviceName + "!");
                Toast.makeText(this, "Error while pairing with device " + deviceName + "!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startLoading() {



        binding.list.startLoading();

        // Changes the button icon.
        binding.fab.setImageResource(R.drawable.ic_bluetooth_searching_white_24dp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endLoading(boolean partialResults) {
        binding.list.endLoading();

        // If discovery has ended, changes the button icon.
        if (!partialResults) {
            binding.fab.setImageResource(R.drawable.ic_bluetooth_white_24dp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endLoadingWithDialog(boolean error, final BluetoothDevice device) {
        if (this.bondingProgressDialog != null) {
            View view = findViewById(R.id.main_content);
            String message;
            String deviceName = BluetoothController.getDeviceName(device);
            boolean isPaired;

//            Intent intent = getIntent();

            // Gets the message to print.
            if (error) {
                message = "Failed pairing with device " + deviceName + "!";
//                setResult(RESULT_CANCELED, intent);
                isPaired = false;
            } else {
                message = "Succesfully paired with device " + deviceName + "!";
//                setResult(RESULT_OK, intent);
                isPaired = true;
            }

            // Dismisses the progress dialog and prints a message to the user.
            this.bondingProgressDialog.dismiss();
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();


            if (isPaired) {

                insertPaired(device);
                /*PairedDevices pairedDevices = new PairedDevices(device.getName(),device.getAddress());
                if (db.pairedDevicesDAO().getPairedDevice(device.getName(),device.getAddress()).size() == 0) {
                    db.pairedDevicesDAO().insertPairedDevice(pairedDevices);
                }
                finish();*/
            }



            // Cleans up state.
            this.bondingProgressDialog = null;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        bluetooth.close();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        // Stops the discovery.
        if (this.bluetooth != null) {
            this.bluetooth.cancelDiscovery();
        }
        // Cleans the view.
        if (this.recyclerViewAdapter != null) {
            this.recyclerViewAdapter.cleanView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Stoops the discovery.
        if (this.bluetooth != null) {
            this.bluetooth.cancelDiscovery();
        }
    }
    public  void  insertPaired(final BluetoothDevice device){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.getInstance(BluetoothAvailableListActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_MAC,device.getAddress());
                    Utils.getInstance(BluetoothAvailableListActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_NAME,device.getName());
                    Utils.getInstance(BluetoothAvailableListActivity.this).setBoolean(Utils.PREF_IS_TOY_CONNECTED,true);
                    PairedDevices pairedDevices = new PairedDevices(device.getName(),device.getAddress());
                    LogUtils.i("endLoadingWithDialog"+" list size before "+db.pairedDevicesDAO().getAll().size());
                    if (db.pairedDevicesDAO().getPairedDevice(device.getName(),device.getAddress()).size() == 0) {
                        db.pairedDevicesDAO().insertPairedDevice(pairedDevices);
                        Intent intent = new Intent(BluetoothAvailableListActivity.this, Connector.class);
                        intent.putExtra("ID", 100);
                        intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_MAC, device.getAddress());
                        intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_NAME, device.getName());
                        intent.putExtra(Utils.EXTRA_IS_FROM_CALL_RECEIVER,false);
                        startService(intent);
                    }
                    LogUtils.i("endLoadingWithDialog"+" list size after "+db.pairedDevicesDAO().getAll().size());
                    startActivity(new Intent(BluetoothAvailableListActivity.this, ConnectedToyActivity.class));
//                    finish();
                }
            },2000);
        }
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected



                if(isInternetAvailable()) {

                    LogUtils.i("BluetoothListActivity"+" mReceiverfgh onReceive Device is now connected "+device.getAddress()+ " "+device.getName());
//                    if (MainApplication.isActivityVisible()) {
                        MyDatabase db;
                        db = MyDatabase.getDataBase(MainApplication.getGlobalContext());
                        List<PairedDevices> pairedDevicesList = db.pairedDevicesDAO().getAll();
                        if (pairedDevicesList.size() > 0) {
                            for (int i=0;i<pairedDevicesList.size();i++) {
                                Log.v("is_data_body",pairedDevicesList.get(i).getDeviceName()+"");
                                if (pairedDevicesList.get(i).getDeviceName().equals(device.getName())){
                                    Utils.getInstance(BluetoothAvailableListActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_MAC, device.getAddress());
                                    Utils.getInstance(BluetoothAvailableListActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_NAME, device.getName());
                                    Utils.getInstance(BluetoothAvailableListActivity.this).setBoolean(Utils.PREF_IS_TOY_CONNECTED, true);
                                    startActivity(new Intent(BluetoothAvailableListActivity.this, ConnectedToyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    finish();
                                }
                            }
                        }

//                    }

                }else{
                    Toast.makeText(BluetoothAvailableListActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();

                }

            }
        }
    };

}
