package com.my.kiki.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.my.kiki.R;
import com.my.kiki.databinding.ActivityTroubleshootBinding;
import com.my.kiki.service.Connector;
import com.my.kiki.utils.Utils;

public class TroubleshootActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityTroubleshootBinding binding;
String ques1="",ques2="",ques3="",ques4="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_troubleshoot);
        initToolbar();
        binding.btnDone.setOnClickListener(this);
        binding.lyToolBar.ivBack.setOnClickListener(this);
     //   binding.tvDisconnect.setOnClickListener(this);

        binding.btnYes1.setOnClickListener(this);
        binding.btnYes2.setOnClickListener(this);
        binding.btnYes3.setOnClickListener(this);
        binding.btnYes4.setOnClickListener(this);

        binding.btnNo1.setOnClickListener(this);
        binding.btnNo2.setOnClickListener(this);
        binding.btnNo3.setOnClickListener(this);
        binding.btnNo4.setOnClickListener(this);

        if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
         //   binding.tvToyName.setText(Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME));
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btnSignUp:
                break;

            case R.id.ivBack:
                finish();
                break;

            case R.id.btnYes1:
                setSelectedBtn(binding.btnYes1);
                setDefaultBtn(binding.btnNo1);
                ques1="1";
                break;

            case R.id.btnYes2:
                setSelectedBtn(binding.btnYes2);
                setDefaultBtn(binding.btnNo2);
                ques2="1";
                break;

            case R.id.btnYes3:
                setSelectedBtn(binding.btnYes3);
                setDefaultBtn(binding.btnNo3);
                ques3="1";
                break;

            case R.id.btnYes4:
                setSelectedBtn(binding.btnYes4);
                setDefaultBtn(binding.btnNo4);
                ques4="1";
                break;

            case R.id.btnNo1:
                setSelectedBtn(binding.btnNo1);
                setDefaultBtn(binding.btnYes1);
                ques1="0";
                break;

            case R.id.btnNo2:
                setSelectedBtn(binding.btnNo2);
                setDefaultBtn(binding.btnYes2);
                ques2="0";
                break;

            case R.id.btnNo3:
                setSelectedBtn(binding.btnNo3);
                setDefaultBtn(binding.btnYes3);
                ques3="0";
                break;

            case R.id.btnNo4:
                setSelectedBtn(binding.btnNo4);
                setDefaultBtn(binding.btnYes4);
                ques4="0";
                break;

                case R.id.tvDisconnect:
                showDisconnectAlert();
                break;

                case R.id.btnDone:
                checkdone();
                break;

        }
    }

    private void setSelectedBtn(Button button) {
        button.setBackgroundResource(R.drawable.rounded_btn_bg_selected);
        button.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void setDefaultBtn(Button button) {
        button.setBackgroundResource(R.drawable.rounded_btn_bg);
        button.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    public void initToolbar() {
        setSupportActionBar(binding.lyToolBar.toolbar);
        getSupportActionBar().setTitle("");

        TextView tvTitle = binding.lyToolBar.toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.str_trouble_shoot));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }

    private void showDisconnectAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        }


    }

    private void checkdone() {
        if (ques1.equals("")&&ques2.equals("")&&ques3.equals("")&&ques4.equals("")) {

            Toast.makeText(this, "Please select all options" , Toast.LENGTH_SHORT).show();
        } else   if (ques1.equals("")|ques2.equals("")|ques3.equals("")|ques4.equals("")) {

            Toast.makeText(this, "Please select all options" , Toast.LENGTH_SHORT).show();
        }else
        if (ques1.equals("1")&&ques2.equals("1")&&ques3.equals("1")&&ques4.equals("1")){

            Toast.makeText(this, "You can connect with the Toy" , Toast.LENGTH_SHORT).show();

        }else if (ques1.equals("0") | ques2.equals("0") | ques3.equals("0") | ques4.equals("0")){

            Toast.makeText(this, "You cannot connect with the Toy", Toast.LENGTH_SHORT).show();

        }else    {
            Toast.makeText(this, "Please select all options" , Toast.LENGTH_SHORT).show();
            }


    }



    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


}
