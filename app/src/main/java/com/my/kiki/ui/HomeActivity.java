package com.my.kiki.ui;

import android.Manifest;
import android.animation.Animator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.my.kiki.R;
import com.my.kiki.adapter.OptionsAdapter;
import com.my.kiki.databinding.ActivityHomeBinding;
import com.my.kiki.service.Connector;
import com.my.kiki.service.SpeechService;
import com.my.kiki.utils.Utils;
import com.my.kiki.voiceassistant.MessageDialogFragment;
import com.my.kiki.voiceassistant.VoiceRecorder;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static com.my.kiki.notification.SpeechServiceNotification.ACTION_NAME;
import static com.my.kiki.utils.Utils.PREF_IS_ERROR;
import static com.my.kiki.utils.Utils.PREF_IS_SPEAKING;
import static com.my.kiki.utils.Utils.PREF_IS_TOY_CONNECTED;
import static com.my.kiki.utils.Utils.isInternetAvailable;
import static com.my.kiki.utils.Utils.isToyConnected;

public class HomeActivity extends AppCompatActivity implements OptionsAdapter.OptionsSelected, View.OnClickListener, MessageDialogFragment.Listener {


    private static final int READ_PHONE_STATE_CODE = 1234;
    ActivityHomeBinding binding;
    private String[] optionsNameArr;
    boolean isProcessAudio = false;
    private SpeechService mSpeechService;
    private VoiceRecorder mVoiceRecorder;
    AudioManager.OnAudioFocusChangeListener afChangeListener;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private int[] imagesArr = {R.drawable.ic_bluetooth, R.drawable.ic_google_assistant, R.drawable.ic_ticket, R.drawable.ic_usage_manual/*, R.drawable.ic_launcher*/};
    private int[] layoutBgArr = {R.drawable.bg_bluetooth_layout, R.drawable.bg_tickets_list_layout, R.drawable.bg_usage_manual_layout/*, R.drawable.ic_launcher*/};
    BluetoothAdapter mBluetoothAdapter;
    private TextView alertTV;
    private RelativeLayout alertLayout;
    private TextView disconnectTV;
    private ProgressBar progressBar;
    private BluetoothDevice myDevice;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);

            if (mSpeechService != null) {
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    AudioManager audioManager = (AudioManager) HomeActivity.this.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.isBluetoothA2dpOn() && Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                         /*   audioManager.setSpeakerphoneOn(false);
                            audioManager.setBluetoothScoOn(true);
                            audioManager.startBluetoothSco();*/
                            startRecord();
                    }
                }
                //you can even read from fionAudioFocusChangele and set it to  google assistant
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_ERROR, false);
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }



        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }


        @Override
        public void onVoiceEnd() {
            showStatus(false);


            if (mSpeechService != null) {

                boolean isError= Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_ERROR);

                Log.i("xyz123","Is internet available : " + isInternetAvailable()+" finishRecognizing "+isError);
                if (isInternetAvailable() && !isError) {

                    mSpeechService.finishRecognizing();
                    // Stop listening to voice
                    Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, true);

                }


                // Stop Cloud Speech API
               /* mSpeechService.removeListener(mSpeechServiceListener);
                unbindService(mServiceConnection);
                mSpeechService = null;*/

            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            StrictMode.setThreadPolicy(new ThreadPolicy.Builder()
//                    .detectAll()
//                    .detectCustomSlowCalls()
//                    .detectNetwork()
//                    .detectResourceMismatches()
//                    .detectUnbufferedIo()
//                    .penaltyLog()
//                    .build());
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.lyToolBar.tvTitle.setText(getString(R.string.title_home));
        GridLayoutManager horizontalLayoutManager = new GridLayoutManager(this, 2);
        binding.optionsRv.setLayoutManager(horizontalLayoutManager);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) + ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            DialogPermission();
        }
        optionsNameArr = getResources().getStringArray(R.array.arrOptions);
//        imagesArr =  getResources().getIntArray(R.array.arrImage);
        OptionsAdapter optionsAdapter = new OptionsAdapter(optionsNameArr, imagesArr, layoutBgArr, this);
        optionsAdapter.setOptionsSelected(this);

        binding.optionsRv.setAdapter(optionsAdapter);

        alertTV = findViewById(R.id.home_alerts_text);
        alertLayout = findViewById(R.id.home_alerts);
        disconnectTV = findViewById(R.id.tvDisconnect);
        progressBar = findViewById(R.id.progressBar);

//        if (Utils.getInstance(this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
//            binding.rlToyOptions.setVisibility(View.VISIBLE);
//        } else {
//            binding.rlToyOptions.setVisibility(View.GONE);
//            DialogAssistant();
//        }


        if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
            binding.tvToyName.setText("Hoggy");
           // binding.tvToyName.setText(Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME));
        }

        binding.tvTroubleShoot.setOnClickListener(this);
        binding.tvDisconnect.setOnClickListener(this);
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v("datavs",Utils.getInstance(this).getString(Utils.PREF_USER_EMAIL)+
                " "+Utils.getInstance(this).getString(Utils.PREF_USER_NAME)+
                " "+Utils.getInstance(this).getString(Utils.PREF_USER_BIRTHDAY)+
                " "+Utils.getInstance(this).getString(Utils.PREF_USER_CLASS)
        );

        BluetoothAdapter.LeScanCallback scanCallback =
             new BluetoothAdapter.LeScanCallback() {
                 @Override
                 public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                         myDevice = device;
                 }
             };
        mBluetoothAdapter.startLeScan(scanCallback);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
//        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isBluetoothA2dpOn()) {
            Log.v("is_state","okok");
            Utils.getInstance(HomeActivity.this).setBoolean(Utils.PREF_IS_TOY_CONNECTED, false);
            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_MAC, "");
            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_NAME, "");
            binding.rlToyOptions.setVisibility(View.GONE);
            stopVoiceRecorder();
            //audio is currently being routed to bluetooth -> bluetooth is connected
        }

        isToyConnected();
//        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        this.getVolumeControlStream();

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BROAD_CAST_RECEIVER_DEVICE_CONNECTED);
        filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Utils.DISCONNECT_TOY_RECEIVER);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filter);

        Log.v("is_stop","listen_start");
//
//        if (Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
//            mSpeechServiceListener.onVoiceRecordStart();
//        }

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("is_stop","listen_stop");
        unregisterReceiver(mBroadcastReceiver);

        //  isProcessAudio=false;
        // Stop Cloud Speech API
        if(mSpeechServiceListener!=null) {
            if(mSpeechService!=null) {
                mSpeechService.removeListener(mSpeechServiceListener);
            }
        }
        unbindService(mServiceConnection);

        stopVoiceRecorder();

        mSpeechService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("xyz123xyz",Utils.getInstance(this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)+"");
        if (Utils.getInstance(this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
            binding.rlToyOptions.setVisibility(View.VISIBLE);
            disconnectTV.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            if(isInternetAvailable())
            showAlert();

//            if(!isInternetAvailable())
//                showAlert(getString(R.string.msg_no_internet));
//            else showAlert(getString(R.string.connected));
        } else {
            binding.rlToyOptions.setVisibility(View.GONE);
            showAlert(getString(R.string.toy_not_connected));
        }

        if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
            binding.tvToyName.setText("Hoggy12");
        }
    }

    @Override
    public void onOptionsSelected(int pos) {
        switch (pos) {
            /*case 4:
                startActivity(new Intent(this, TicketsActivity.class));
                break;*/
            case 0:
                if (Utils.getInstance(this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                    if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_MAC) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_MAC).equals("")
                            && Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
                        Toast.makeText(this,getString(R.string.err_device_already_connected,"Hoggy"),Toast.LENGTH_SHORT).show();
                    }
                } else {


                    if(isInternetAvailable()) {
                        if(Utils.isToyConnected(this)){
//                            checkBluetoothConnection();
                            startActivity(new Intent(this, ConnectedToyActivity.class));
                        }else{
                            startActivity(new Intent(this, BluetoothListActivity.class));
                        }
                        finish();
                    }else if(!isInternetAvailable()) {
                        showAlert(getString(R.string.msg_no_internet));
//                        Toast.makeText(HomeActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                    }else hideAlert();

                }
                break;

            case 2:
                startActivity(new Intent(this, TicketsActivity.class));
                break;
        }
    }

  /*  private void checkBluetoothConnection() {
       BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
           @Override
           public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
               super.onConnectionStateChange(gatt, status, newState);
               if (newState == STATE_CONNECTED) {
                   Log.d("checkblutooth","connected");
                   boolean rssiStatus = gatt.readRemoteRssi();
               }
           }
           @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if(status == BluetoothGatt.GATT_SUCCESS)
                    Log.d("bluetooth rssi",String.valueOf(rssi));
            }
        };
       try {
           myDevice.connectGatt(this, true, bluetoothGattCallback);
       }catch(Exception e){
           Log.e("homeActivity",e.getMessage());
       }
    }
    */

    public void showAlert(final String msg){
        alertLayout.setAlpha(0f);
        alertLayout.setVisibility(View.VISIBLE);
        alertLayout.animate().alpha(1f).setDuration(400).setListener(null) ;
                alertTV.setText(msg);
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvTroubleShoot:
                startActivity(new Intent(this, TroubleshootActivity.class));
                break;
            case R.id.tvDisconnect:
                // showDisconnectAlert();
                disConnectDevice();
                break;
        }
    }


    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //     mStatus.setTextColor(hearingVoice ? mColorHearing : mColorNotHearing);
            }
        });
    }

    private void showDisconnectAlert() {
        Builder builder = new Builder(this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.alert_msg_disconnect));
        builder.setPositiveButton(getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                disConnectDevice();
            }
        });
        builder.setNegativeButton(getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void disConnectDevice() {

        if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_MAC) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_MAC).equals("")
                && Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {

            Intent intent = new Intent(this, Connector.class);
            intent.putExtra("ID", 100);
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_MAC, Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_MAC));
            intent.putExtra(Utils.EXTRA_SELECTED_DEVICE_NAME, Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME));
            intent.putExtra(Utils.EXTRA_IS_FROM_CALL_RECEIVER,false);
            startService(intent);


            stopVoiceRecorder();
            binding.rlToyOptions.setVisibility(View.GONE);
            alertLayout.setVisibility(View.INVISIBLE);
            Utils.getInstance(HomeActivity.this).setBoolean(Utils.PREF_IS_TOY_CONNECTED, false);
            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_MAC, "");
            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_NAME, "");


        }


    }

    @Override
    protected void onNewIntent(Intent intent)  {
        super.onNewIntent(intent);
        String action = intent.getStringExtra("action");

        try {
            if (action.equalsIgnoreCase(ACTION_NAME))
                disConnectDevice();
        }catch(NullPointerException e){
            Log.d("notification click", "ignore");
        }
    }

    //toy connection & internet connection
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                String action = intent.getAction();

                if(action.equals(BluetoothDevice.ACTION_FOUND)){
                    int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                    Log.d("home","bluetooth strength:" + rssi);
                }

                if(action.equals(Utils.DISCONNECT_TOY_RECEIVER)){
                    Log.d("homeActivity","disconnect");
                    disConnectDevice();
                    return;
                }
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    if(isInternetAvailable()){
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, false);
                                isProcessAudio=false;
                            }
                        }, 500);

//                        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)){
//                            showAlert(getString(R.string.msg_no_internet));
//                        }

                       showAlert();

                    }else if(!isInternetAvailable()){
                        showAlert(getString(R.string.msg_no_internet));
//                        Toast.makeText(HomeActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                    }else hideAlert();

                }else {

                    Log.i("HomeActivity", " BT onReceive" +
                            intent.getExtras().getBoolean(Utils.EXTRA_DEVICE_IS_CONNECTED));

                    if (!intent.getExtras().getBoolean(Utils.EXTRA_DEVICE_IS_CONNECTED)) {
                        if (!intent.getExtras().getBoolean(Utils.EXTRA_IS_TEMP_DISCONNECT)) {
                            Utils.getInstance(HomeActivity.this).setBoolean(Utils.PREF_IS_TOY_CONNECTED, false);
                            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_MAC, "");
                            Utils.getInstance(HomeActivity.this).setString(Utils.PREF_CONNECTED_DEVICE_NAME, "");
                        }

                        binding.rlToyOptions.setVisibility(View.GONE);
                        stopVoiceRecorder();

                        showAlert(getString(R.string.toy_not_connected));

                    } else {

                        if(isInternetAvailable()){

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("is_data_body1","123");
                                    if (Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                                        binding.rlToyOptions.setVisibility(View.VISIBLE);
                                        startRecord();
                                    } else {
                                        binding.rlToyOptions.setVisibility(View.GONE);

                                    }

                                }
                            }, 500);
                            if(isSignalWeak())
                                showAlert("Weak Network Signal");

                        }else if(!isInternetAvailable()){
                            showAlert(getString(R.string.msg_no_internet));
//                            Toast.makeText(HomeActivity.this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                        }else hideAlert();


                    }
                }

            }

        }


    };

    public void showAlert(){
        if(isSignalWeak())
            showAlert("Weak Network Signal");
        else if(isToyConnectedPrefs())
            showAlert(getString(R.string.connected));
        else showAlert(getString(R.string.toy_not_connected));
    }

    private boolean isSignalWeak() {
        boolean isWeak = false;
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public int getWifiLevel() {
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(linkSpeed, 5);
        return level;
    }

    private void startVoiceRecorder() {
        Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, false);
        Log.e("xyz123","startVoiceRecorder");
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, false);
        isProcessAudio=false;
        Log.e("xyz123","stopVoiceRecorder");
        if (mVoiceRecorder != null) {
            mSpeechService.stopAudio();
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance("This app needs to record audio and recognize your speech.")
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    Log.e("xyz123","onSpeechRecognized");
                    if (isFinal && mVoiceRecorder != null) {
                        mVoiceRecorder.dismiss();
                    }
                 /*   if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    mAdapter.addResult(text);
                                    mRecyclerView.smoothScrollToPosition(0);
                                    Log.e("xyz123","onSpeechRecognized: "+text);
                                } else {
                                    mText.setText(text);
                                    Log.e("xyz123","onSpeechRecognized: "+text);
                                }
                            }
                        });
                    }*/

                }
                @Override
                public void onSpeechResponsed(final String text2, final boolean isFinal) {

                    Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, false);
                    isProcessAudio=false;

//                    if (isFinal && mVoiceRecorder != null) {
//                        mVoiceRecorder.dismiss();
//                    }

                    boolean isSpeaking= Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_SPEAKING);


                   if(isFinal) {

                        Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_ERROR, true);

//                        if (isInternetAvailable() && isSpeaking) {
//                                Utils.getInstance(HomeActivity.this).setBoolean(PREF_IS_SPEAKING, false);
//                            isProcessAudio=false;
//                        }
                    }
                    boolean isError= Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_ERROR);

                    Log.e("xyz123",isError+" onSpeechResponsed: "+isSpeaking+" isFinal "+isFinal);

                /*  if (mText != null && !TextUtils.isEmpty(text2)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    mAdapter.addResult(text2);
                                    mRecyclerView.smoothScrollToPosition(0);
                                    Log.e("xyz123","onSpeechResponsed: "+text2);
                                } else {
                                    mText.setText(text2);
                                    Log.e("xyz123","onSpeechResponsed2: "+text2);
                                }
                            }
                        });
                    }*/
                    //  startVoiceRecorder();
                }

                @Override
                public void onVoiceRecordStart() {

                    Log.d("home","onVoiceRecordStart()");

                    if (Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                        binding.rlToyOptions.setVisibility(View.VISIBLE);
                        startRecord();
                    } else {
                        binding.rlToyOptions.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onVoiceRecordStop() {

                    binding.rlToyOptions.setVisibility(View.GONE);
                    stopVoiceRecorder();
                   // Toast.makeText(HomeActivity.this,getString(R.string.str_device_unpaired),Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onRequestStart() {
//                    Log.e("xyz123","onRequestStart");
                }

                @Override
                public void onCredentioalSuccess() {
                    Log.e("xyz123","onCredentioalSuccess");
                }

                @Override
                public void restartSpeechService() {
                    unbindService(mServiceConnection);
                    bindService(new Intent(HomeActivity.this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
                }

                @Override
                public void onError(){
                    Log.w("home","onError()");
                    mSpeechService.stopRecognising();
                    int sampleRate = mVoiceRecorder==null?mVoiceRecorder.getSampleRate():16000;
                    mSpeechService.startRecognizing(sampleRate);
                }

                @Override
                public void onConnecting() {
                    disconnectTV.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    if (Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                        showAlert("Connecting...");
                    }    else
                        showAlert(getString(R.string.toy_not_connected));
                }

                @Override
                public void onConnected() {
                    Log.d("home","connected");
                    if (isInternetAvailable() && Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {
                        showAlert(getString(R.string.connected));
                    }
                    disconnectTV.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }

            };

    @Override
    public void onMessageDialogDismissed() {


    }
    protected void startRecord() {

        if (!isProcessAudio && Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED)) {

            Log.v("isvoice123", "1");

            startVoiceRecorder();
            isProcessAudio = true;
        }
    }

    boolean isToyConnectedPrefs(){
       return Utils.getInstance(HomeActivity.this).getBoolean(Utils.PREF_IS_TOY_CONNECTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        Log.v("is_requestCode",requestCode+" "+permissions[1]);
        switch (requestCode) {
            case READ_PHONE_STATE_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED&&grantResults[2] == PackageManager.PERMISSION_GRANTED) {

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
        builder.setMessage("As the Toy is a Bluetooth Toy, we require Bluetooth & Location Permissions");
        builder.setCancelable(false);
        //Set the Dynamically created layout as the Dialogs view


        //Add Dialog button that will just close the Dialog
        builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {  ActivityCompat.requestPermissions(HomeActivity.this,
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
    private void DialogAssistant() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Give the Dialog a Title
        builder.setTitle("Message");
        builder.setMessage("Toy is not connected, please try to connect");
        builder.setCancelable(false);
        //Set the Dynamically created layout as the Dialogs view


        //Add Dialog button that will just close the Dialog
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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

    enum networkState {
        CONNECTED, NOT_CONNECTED
    }

}
