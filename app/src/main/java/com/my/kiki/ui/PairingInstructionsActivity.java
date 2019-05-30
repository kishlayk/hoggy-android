package com.my.kiki.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.my.kiki.R;
import com.my.kiki.databinding.ActivityPairingInstructionsBinding;
import com.my.kiki.utils.Utils;

public class PairingInstructionsActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityPairingInstructionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pairing_instructions);
        initToolbar();
        binding.btnConnect.setOnClickListener(this);
        binding.btnSkip.setOnClickListener(this);
        binding.lyToolBar.ivBack.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btnConnect:
                startActivity(new Intent(this, BluetoothListActivity.class));
                finish();
                break;
            case R.id.btnSkip:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    public void initToolbar() {
        setSupportActionBar(binding.lyToolBar.toolbar);
        getSupportActionBar().setTitle("");

        TextView tvTitle = binding.lyToolBar.toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(getString(R.string.lbl_pairing_instructions));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }


}
