package com.my.kiki.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.my.kiki.adapter.DeviceRecyclerViewAdapter;
import com.my.kiki.bluetooth.BluetoothController;
import com.my.kiki.main.MainApplication;
import com.my.kiki.ui.BluetoothListActivity;
import com.my.kiki.ui.HomeActivity;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class Utils {


    SharedPreferences pref;
    Editor edit;
    Context context;
    static Utils utils;

    public static final String blanch_caps = "blanch_caps.otf";

    public static final String PREF_USER_ID = "com.kiki.pref_user_id";
    public static final String PREF_USER_API_KEY = "com.kiki.pref_user_api_key";
    public static final String PREF_USER_EMAIL = "com.kiki.pref_user_email";
    public static final String PREF_USER_NAME = "com.kiki.pref_user_name";
    public static final String PREF_USER_BIRTHDAY = "com.kiki.pref_user_birthday";
    public static final String PREF_USER_CLASS = "com.kiki.pref_user_class";
    public static final String CLASS_KEY = "class_key";
    public static final String CLASS_KEY_POSITION = "class_key_position";
    public static final String PREF_USER_PASSWORD = "com.kiki.pref_user_password";
    public static final String PREF_IS_TOY_CONNECTED = "com.kiki.pref_is_toy_connected";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public static final boolean SHOULD_PRINT_LOG = true;


    public static final String PREF_NAME = "kiki_app";

    public static final String PREF_IS_SPEAKING = "com.kiki.pref_is_speaking";
    public static final String PREF_IS_ERROR = "com.kiki.pref_is_error";

    public static final String ET_FONT = "Helvetica.otf";
    public static final String TV_FONT = "Raleway-Regular.ttf";
    public static final String BTN_FONT = "Raleway-Medium.ttf";
    public static final String FONT_DIR = "fonts/";
    public static final String EXTRA_MATCHID = "match_id";

    public static final String EXTRA_SELECTED_DEVICE_MAC = "extra_selected_device_mac";
    public static final String EXTRA_SELECTED_DEVICE_NAME = "extra_selected_device_name";
    public static final String BROAD_CAST_RECEIVER_DEVICE_CONNECTED = "broad_cast_receiver_device_connected";
    public static final String BROAD_CAST_RECEIVER_DEVICE_UNPAIRED = "broad_cast_receiver_device_unpaired";
    public static final String EXTRA_DEVICE_IS_CONNECTED = "extra_device_is_connected";
    public static final String PREF_CONNECTED_DEVICE_MAC = "com.kiki.pref_connected_device_mac";
    public static final String PREF_CONNECTED_DEVICE_NAME = "com.kiki.pref_connected_device_name";
    public static final String EXTRA_IS_FROM_CALL_RECEIVER = "extra_is_from_call_receiver";
    public static final String EXTRA_IS_TEMP_DISCONNECT = "extra_is_temp_disconnect";

    public static final String TOY_DEVICE_OBJECT = "toy_device_object";
    public static boolean temp;

    public Utils(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        edit = pref.edit();
    }

    public synchronized static Utils getInstance(Context context) {
        if (utils == null) {
            utils = new Utils(context);

        }
        return utils;
    }

    public void setString(String key, String value) {
        edit.putString(key, value);
        edit.commit();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }

    public void setInt(String key, int value) {
        edit.putInt(key, value);
        edit.commit();
    }

    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public void setBoolean(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.commit();
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public static boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) MainApplication.getGlobalContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isToyConnected(){



        BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        BA.enable();
        Set s = BA.getBondedDevices();
        Iterator it = s.iterator();
        while (it.hasNext()){
            BluetoothDevice d = (BluetoothDevice) it.next();
            System.out.println("device is "+d.getName());
        }

        BA.getRemoteDevice("00:58:56:07:72:D8");
//        ConnectivityManager cm = (ConnectivityManager) MainApplication.getGlobalContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo bluetooth = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
//        if(controller.isBluetoothEnabled()) return true;
        return false;
    }

    public static boolean isToyReady(){
        return false;
    }

    public static int isInternetOk(){
        return 0;
    }

    public static boolean toyCorrectlyConnected(BluetoothDevice device, Editor editor){

        if(device.getName().isEmpty() || !device.getName().equals("Pet Singer")){
            return false;
        }
        //store current device in prefrences
        String currentDevice = Utils.getInstance(MainApplication.getGlobalContext()).getString(Utils.TOY_DEVICE_OBJECT);
        try {
            if (currentDevice == null || currentDevice.isEmpty() || currentDevice.equals(null)) {
                editor.putString(Utils.TOY_DEVICE_OBJECT, new Gson().toJson(device));
                editor.commit();
            } else{
                BluetoothDevice storedDevice = new Gson().fromJson(currentDevice, BluetoothDevice.class);
                if (!storedDevice.getAddress().equals(device.getAddress())){
                    return false;
                }
            }
        }catch (NullPointerException e){
            Log.e( "BluetoothListActivity", e.getMessage());

        }
        return true;

    }

    public static boolean isToyConnected(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if (bta.isEnabled() && audioManager.isBluetoothA2dpOn()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioDeviceInfo[] audiodevices = audioManager.getDevices(1);
                for (AudioDeviceInfo ad:audiodevices) {
                  if (ad.getProductName().equals("Pet Singer")){
                      try {
                          setDevicePreferencesonConnection(context, ad.getProductName().toString(), (String) ad.getClass().getDeclaredMethod("getAddress").invoke(ad));
                      } catch (IllegalAccessException e) {
                          e.printStackTrace();
                      } catch (InvocationTargetException e) {
                          e.printStackTrace();
                      } catch (NoSuchMethodException e) {
                          e.printStackTrace();
                      }
                      return true;
                  }
                }
            } else {
                    return false;
//                bta.disable();
//                audioManager.stopBluetoothSco();
//                audioManager.isBluetoothA2dpOn();
//                audioManager.setBluetoothA2dpOn(true);
            }
        }
        return false;
    }

    public static void setDevicePreferencesonConnection(Context context, String deviceName, String deviceAddress){
        Utils.getInstance(context).setString(Utils.PREF_CONNECTED_DEVICE_MAC, deviceAddress);
        Utils.getInstance(context).setString(Utils.PREF_CONNECTED_DEVICE_NAME, deviceName);
        Utils.getInstance(context).setBoolean(Utils.PREF_IS_TOY_CONNECTED, true);
    }

    public float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

//            try {
//                Thread.sleep(360);
//            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int getMobileDataSpeed(){
        TelephonyManager telephonyManager = (TelephonyManager) MainApplication.getGlobalContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainApplication.getGlobalContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return -10000;
        }
//        if(!temp){
//            temp=true;
//            throw new RuntimeException();
//        }

        List<CellInfo> cellInfos = telephonyManager != null? telephonyManager.getAllCellInfo(): null;
        if (cellInfos != null) {
            CellInfoWcdma cellinfowcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
            CellSignalStrengthWcdma cellSignalStrengthCdma = cellinfowcdma.getCellSignalStrength();
            return cellSignalStrengthCdma.getDbm();
        }


        return -10000;
    }

    public long getHeapSize(){
        final Runtime runtime = Runtime.getRuntime();
        if (runtime != null) {
            final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
            final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
            return availHeapSizeInMB;
        }
        return -10000;
    }
}
