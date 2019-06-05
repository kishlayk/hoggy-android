package com.my.kiki.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.my.kiki.R;
import com.my.kiki.adapter.BluetoothListAdapter;
import com.my.kiki.databinding.ActivityBluetoothListBinding;
import com.my.kiki.db.MyDatabase;
import com.my.kiki.main.MainApplication;
import com.my.kiki.model.PairedDevices;
import com.my.kiki.model.PairedDevicesModel;
import com.my.kiki.receiver.ACLReceiver;
import com.my.kiki.receiver.ConnectViaAppReceiver;
import com.my.kiki.service.Connector;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.my.kiki.utils.Utils.insertToyifNew;
import static com.my.kiki.utils.Utils.isInternetAvailable;
import static com.my.kiki.utils.Utils.toyCorrectlyConnected;

public class BluetoothListActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListAdapter.BluetoothSelected {

    ActivityBluetoothListBinding binding;
    public static final int REQUEST_CODE_AVAILABLE_DEVICES = 1;

    //    private String [] bluetoothNameArr;
//    private int [] bluetoothimagesArr = {R.drawable.ic_bluetooth_connected_black_48dp,R.drawable.ic_bluetooth_connected_black_48dp,R.drawable.ic_bluetooth_connected_black_48dp};
    public final static String temp[][] = new String[50][2];
    private BluetoothAdapter mBluetoothAdapter;
    ArrayList<PairedDevicesModel> arrPairedDevices;

    MyDatabase db;

    ConnectViaAppReceiver cvReceiver;

    int selectedPos;
    private static final int READ_PHONE_STATE_CODE = 1234;

    ACLReceiver aclReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_list);
        initToolbar();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) + ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
        DialogPermission();
        }

        db = MyDatabase.getDataBase(this);

        binding.lyToolBar.ivBack.setOnClickListener(this);
        binding.fab.setOnClickListener(this);

        arrPairedDevices = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.bluetoothRv.setLayoutManager(linearLayoutManager);

//        bluetoothNameArr =  getResources().getStringArray(R.array.arrBluetooth);
//        imagesArr =  getResources().getIntArray(R.array.arrImage);

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

 /*       if (!bta.isEnabled()) {
            Intent btIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(btIntent);
            Log.i("BluetoothListActivity", "Bluetooth was not enabled, starting...");
        }*/

//        loadDevices();
        loadFromDb();

        aclReceiver = new ACLReceiver(this, new ACLReceiver.Listener(){

            @Override
            public void onConnected() {
                startActivity(new Intent(MainApplication.getGlobalContext(), ConnectedToyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }

            @Override
            public void onDisconnected() {

            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        Log.v("is_requestCode",requestCode+" "+permissions[1]);
        switch (requestCode) {
            case READ_PHONE_STATE_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED&&grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    if(Utils.isToyAlreadyConnected(this)){
                        startActivity(new Intent(this, ConnectedToyActivity.class));
                    }
                } else {
                    // Permission Denied
                   DialogPermission();

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void DialogPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Give the Dialog a Title
        builder.setTitle("Permission");
        builder.setMessage("As the Toy is a Bluetooth Toy, we require Bluetooth & Location Permissons");
        builder.setCancelable(false);
        //Set the Dynamically created layout as the Dialogs view


        //Add Dialog button that will just close the Dialog
        builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {  ActivityCompat.requestPermissions(BluetoothListActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_COARSE_LOCATION},
                    READ_PHONE_STATE_CODE);
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        //Show the custom AlertDialog
        AlertDialog alert = builder.create();
        alert.show();
        final Button positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    private void loadFromDb() {

        arrPairedDevices.clear();

        List<PairedDevices> pairedDevicesList = db.pairedDevicesDAO().getAll();
        if (pairedDevicesList.size() > 0) {
            for (int i=0;i<pairedDevicesList.size();i++) {
                PairedDevicesModel pairedDeviceModel = new PairedDevicesModel();
                pairedDeviceModel.setDeviceName(pairedDevicesList.get(i).getDeviceName());
                pairedDeviceModel.setDeviceAddress(pairedDevicesList.get(i).getDeviceMac());
                arrPairedDevices.add(pairedDeviceModel);
            }

            BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(arrPairedDevices, this);
            bluetoothListAdapter.setBluetoothSelected(this);

            binding.bluetoothRv.setAdapter(bluetoothListAdapter);

            if (arrPairedDevices.size() > 0) {
                binding.bluetoothRv.setVisibility(View.VISIBLE);
                binding.emptyList.setVisibility(View.GONE);
            } else {

                binding.bluetoothRv.setVisibility(View.GONE);
                binding.emptyList.setVisibility(View.VISIBLE);
            }

        }
    }

    private void loadDevices() {

        arrPairedDevices.clear();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        ArrayList<String> devices = new ArrayList<>();

        for (BluetoothDevice bt : pairedDevices) {

            PairedDevicesModel pairedDeviceModel = new PairedDevicesModel();
            pairedDeviceModel.setDeviceName(bt.getName());
            pairedDeviceModel.setDeviceAddress(bt.getAddress());
            arrPairedDevices.add(pairedDeviceModel);

//            devices.add(bt.getName() + "\n" + bt.getAddress());
        }

        /*ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(arrayAdapter);*/

        BluetoothListAdapter bluetoothListAdapter = new BluetoothListAdapter(arrPairedDevices, this);
        bluetoothListAdapter.setBluetoothSelected(this);

        binding.bluetoothRv.setAdapter(bluetoothListAdapter);

        if (arrPairedDevices.size() > 0) {
            binding.bluetoothRv.setVisibility(View.VISIBLE);
            binding.emptyList.setVisibility(View.GONE);
        } else {

            binding.bluetoothRv.setVisibility(View.GONE);
            binding.emptyList.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.fab:
                /*startActivityForResult(new Intent(this, BluetoothAvailableListActivity.class), REQUEST_CODE_AVAILABLE_DEVICES);*/
                startActivity(new Intent(this, BluetoothAvailableListActivity.class));
                break;
        }
    }

    public void initToolbar() {
        setSupportActionBar(binding.lyToolBar.toolbar);
        getSupportActionBar().setTitle("");

        TextView tvTitle = binding.lyToolBar.toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.lbl_paired_list));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }


    @Override
    public void onBluetoothSelected(int pos) {

        if(isInternetAvailable()){

            Log.d("bluetoothList","selected()");

            selectedPos = pos;
            Intent intent = new Intent(this, Connector.class);
            intent.putExtra("ID", 100);
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_MAC, arrPairedDevices.get(pos).getDeviceAddress());
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_NAME, arrPairedDevices.get(pos).getDeviceName());
            intent.putExtra(Utils.EXTRA_IS_FROM_CALL_RECEIVER,false);
            startService(intent);

        }else{
            Toast.makeText(this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
        }



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_AVAILABLE_DEVICES && resultCode == RESULT_OK) {

            String requiredValue = data.getStringExtra("key");

//            loadDevices();
            loadFromDb();
            Log.e("BluetoothListActivity", " onActivityResult requiredValue " + requiredValue);

        }

    }

    private BroadcastReceiver disconnectViaAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("BluetoothListActivity", " disconnectViaAppReceiver onReceive ");

          //  Toast.makeText(BluetoothListActivity.this,getString(R.string.str_device_unpaired),Toast.LENGTH_SHORT).show();

            db.pairedDevicesDAO().deletePairedDevice(arrPairedDevices.get(selectedPos).getDeviceName(),arrPairedDevices.get(selectedPos).getDeviceAddress());
            arrPairedDevices.remove(selectedPos);
            loadFromDb();
            startActivity(new Intent(BluetoothListActivity.this,BluetoothAvailableListActivity.class));


        }


    };


    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(disconnectViaAppReceiver, new IntentFilter(Utils.BROADCAST_INTENT_DEVICE_UNPAIRED));
/*        cvReceiver = new ConnectViaAppReceiver(this, new ConnectViaAppReceiver.Listener() {
            @Override
            public void callback1() {}

            @Override
            public void callback2() {}

            @Override
            public void callback3() {
                finish();
                startActivity(new Intent(BluetoothListActivity.this, ConnectedToyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(disconnectViaAppReceiver);
        /*try {
            cvReceiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            aclReceiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.activityResumed();
        loadFromDb();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.activityPaused();
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

}
