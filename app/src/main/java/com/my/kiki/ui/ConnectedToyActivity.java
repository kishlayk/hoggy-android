package com.my.kiki.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.my.kiki.R;
import com.my.kiki.databinding.ActivityConnectedToyBinding;
import com.my.kiki.utils.Utils;

public class ConnectedToyActivity extends AppCompatActivity implements View.OnClickListener {
 ActivityConnectedToyBinding binding;
    boolean isClicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connected_toy);

        binding.btnNext.setOnClickListener(this);
        binding.ivBluetoothImage.setOnClickListener(this);

        if (Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME) != null && !Utils.getInstance(this).getString(Utils.PREF_CONNECTED_DEVICE_NAME).equals("")) {
            binding.tvBluetoothName.setText("Hoggy "+getString(R.string.str_connected));
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isClicked)
                finish();
                startActivity(new Intent(ConnectedToyActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
              //  overridePendingTransition(0, 0);
            }
        }, 2500);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnNext:
            case R.id.ivBluetoothImage:
                isClicked = true;
                finish();
                startActivity(new Intent(ConnectedToyActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
             //   overridePendingTransition(0, 0);
                break;
        }
    }
}
