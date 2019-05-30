package com.my.kiki.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.my.kiki.R;
import com.my.kiki.adapter.TicketsAdapter;
import com.my.kiki.databinding.ActivityTicketsBinding;
import com.my.kiki.model.PairedDevices;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;
import com.my.kiki.utils.Validation;

public class TicketsActivity extends AppCompatActivity implements TicketsAdapter.OnTicketSelectedListener,View.OnClickListener {

    ActivityTicketsBinding binding;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private TicketsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tickets);
        binding.progress.setVisibility(View.VISIBLE);
        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("tickets")
                .whereEqualTo("userEmail",Utils.getInstance(TicketsActivity.this).getString(Utils.PREF_USER_EMAIL));

        // RecyclerView
        mAdapter = new TicketsAdapter(mQuery, this,this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    binding.progress.setVisibility(View.GONE);
                    binding.rvTickets.setVisibility(View.GONE);
                    binding.tvEmptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.progress.setVisibility(View.GONE);
                    binding.rvTickets.setVisibility(View.VISIBLE);
                    binding.tvEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Toast.makeText(TicketsActivity.this,getString(R.string.err_something_wrong), Toast.LENGTH_SHORT).show();
            }
        };

        binding.rvTickets.setLayoutManager(new LinearLayoutManager(TicketsActivity.this));
        binding.rvTickets.setAdapter(mAdapter);
   /*     new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },2000);*/


        binding.btnAddTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TicketsActivity.this, AddTicketActivity.class));
            }
        });
        binding.lyToolBar.ivBack.setOnClickListener(this);
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

    @Override
    public void onTicketSelected(DocumentSnapshot restaurant) {

    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }


}
