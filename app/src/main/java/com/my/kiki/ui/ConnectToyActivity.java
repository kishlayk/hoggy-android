package com.my.kiki.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.my.kiki.R;
import com.my.kiki.databinding.ActivityConnectToyBinding;
import com.my.kiki.utils.Utils;

public class ConnectToyActivity extends AppCompatActivity implements View.OnClickListener {
 ActivityConnectToyBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect_toy);

        binding.btnConnectToy.setOnClickListener(this);
        binding.btnSkip.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnSkip:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;
            case R.id.btnConnectToy:
                startActivity(new Intent(this, PairingInstructionsActivity.class));
                finish();
                break;
        }
    }
}
