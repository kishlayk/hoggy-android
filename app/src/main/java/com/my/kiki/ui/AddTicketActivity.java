package com.my.kiki.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.my.kiki.R;
import com.my.kiki.databinding.ActivityAddTicketBinding;
import com.my.kiki.model.TicketsModel;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;
import com.my.kiki.utils.Validation;

public class AddTicketActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAddTicketBinding binding;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_ticket);
        mFirestore = FirebaseFirestore.getInstance();
        initToolbar();
        binding.btnSignUp.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btnSignUp:

                if (Validation.hasText(binding.edtTitle, getString(R.string.err_fields))
                        && Validation.hasText(binding.edtDescription, getString(R.string.err_fields))) {

                    if (isInternetAvailable()){
                        addTicket();
                    }else {
                        Toast.makeText(this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
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
        tvTitle.setText(getString(R.string.lbl_add_ticket));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }



    private void addTicket() {
        binding.progress.setVisibility(View.VISIBLE);
        // call db insert code here
        LogUtils.i("SignupActivity" + " registerUser "+Utils.getInstance(AddTicketActivity.this).getString(Utils.PREF_USER_EMAIL));
        WriteBatch batch = mFirestore.batch();
        DocumentReference databasesRef = mFirestore.collection("tickets").document();
        TicketsModel ticketModel = new TicketsModel();
        ticketModel.setTicketTitle(binding.edtTitle.getText().toString());
        ticketModel.setTicketDesc(binding.edtDescription.getText().toString());
        ticketModel.setUserEmail(Utils.getInstance(AddTicketActivity.this).getString(Utils.PREF_USER_EMAIL));
        ticketModel.setTicketStatus("O");
        batch.set(databasesRef, ticketModel);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    binding.progress.setVisibility(View.GONE);
                    LogUtils.i("AddTicketActivity" + " addTicket  " + "Write batch succeeded.");
                    Toast.makeText(AddTicketActivity.this,"Successfully Submitted", Toast.LENGTH_SHORT).show();
                } else {
                    binding.progress.setVisibility(View.GONE);
                    LogUtils.i("AddTicketActivity" + " addTicket " + "write batch failed." + task.getException());
                  //  Toast.makeText(AddTicketActivity.this,"error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
