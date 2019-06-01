package com.my.kiki.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.my.kiki.R;
import com.my.kiki.databinding.ActivitySignupBinding;
import com.my.kiki.model.UserModel;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;
import com.my.kiki.utils.Validation;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    ActivitySignupBinding binding;
    final Calendar myCalendar = Calendar.getInstance();
    private FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        mFirestore = FirebaseFirestore.getInstance();
        initToolbar();
        binding.btnSignUp.setOnClickListener(this);
        binding.edtBirthday.setOnClickListener(this);
        binding.btnGoogleAssistant.setOnClickListener(this);
        binding.edtBirthday.setFocusable(false);


        //Setting the ArrayAdapter data on the Spinner
binding.edtClass.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        startActivityForResult(new Intent(SignupActivity.this, ClassDialogActivity.class), 102);
    }
});
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.edtBirthday:

                new DatePickerDialog(SignupActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;

            case R.id.btnSignUp:

                if (Validation.hasText(binding.edtName, getString(R.string.err_fields))
                        && Validation.hasText(binding.edtBirthday, getString(R.string.err_fields))
                        && Validation.hasText(binding.edtClass, getString(R.string.err_fields))
                        && Validation.isValidMail(binding.edtEmail, getString(R.string.error_invalid_email))) {

                    int classcheck  = Integer.parseInt(binding.edtClass.getText().toString());
                    if (classcheck<=12&&classcheck!=0){
                        if (isInternetAvailable()) {
                            registerUser();
                        } else {
                            Toast.makeText(this, getString(R.string.msg_no_internet), Toast.LENGTH_SHORT).show();
                        }

                }else {
                        Toast.makeText(this, "Enter Valid Class", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.btnGoogleAssistant:
                startActivity(new Intent(this,HomeActivity.class));
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
        tvTitle.setText(getString(R.string.lbl_registration));

        binding.lyToolBar.ivBack.setOnClickListener(this);

    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        binding.edtBirthday.setText(sdf.format(myCalendar.getTime()));
    }

    private void registerUser() {
        binding.progress.setVisibility(View.VISIBLE);
        // call db insert code here
        LogUtils.i("SignupActivity" + " registerUser ");
        WriteBatch batch = mFirestore.batch();
        DocumentReference databasesRef = mFirestore.collection("users").document(binding.edtEmail.getText().toString());
        UserModel userModel = new UserModel();
        userModel.setUserName(binding.edtName.getText().toString());
        userModel.setUserBirthday(binding.edtBirthday.getText().toString());
        userModel.setUserClass(binding.edtClass.getText().toString());
        userModel.setUserEmail(binding.edtEmail.getText().toString());
        batch.set(databasesRef, userModel);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete( Task<Void> task) {
                if (task.isSuccessful()) {
                    LogUtils.i("DbUploadActivity" + " onUploadResultIntent  " + "Write batch succeeded.");
                    Utils.getInstance(SignupActivity.this).setBoolean(Utils.KEY_IS_LOGGED_IN, true);
                    Utils.getInstance(SignupActivity.this).setString(Utils.PREF_USER_EMAIL, binding.edtEmail.getText().toString());
                    Utils.getInstance(SignupActivity.this).setString(Utils.PREF_USER_NAME, binding.edtName.getText().toString());
                    Utils.getInstance(SignupActivity.this).setString(Utils.PREF_USER_BIRTHDAY, binding.edtBirthday.getText().toString());
                    Utils.getInstance(SignupActivity.this).setString(Utils.PREF_USER_CLASS, binding.edtClass.getText().toString());



                    startActivity(new Intent(SignupActivity.this, ConnectToyActivity.class));
                    binding.progress.setVisibility(View.GONE);
                    finish();
                } else {
                    LogUtils.i("DbUploadActivity" + " onUploadResultIntent " + "write batch failed." + task.getException());
                    binding.progress.setVisibility(View.GONE);
                }
            }
        });
    }
    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
   if (requestCode == 102) {
           // loadData();
       binding.edtClass.setText((Utils.getInstance(this).getInt(Utils.CLASS_KEY))+"");
        }
    }
}
