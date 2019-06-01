package com.my.kiki.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.my.kiki.R;
import com.my.kiki.databinding.ActivitySplashBinding;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;

public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;

    protected boolean active = true;
    protected int splashTime = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        Thread splashThread = new Thread() {
            public void run() {
                try {
                    int waited = 0;
                    while (active && waited < splashTime) {
                        sleep(100);
                        if (active)
                            waited += 100;
                    }
                } catch (Exception e) {
                    Log.e("Splash Screen", e.getLocalizedMessage());
                } finally {
                    finish();
                    Intent i = null;
                    LogUtils.i("SplashActivity" + " onCreate KEY_IS_LOGGED_IN " + Utils.getInstance(SplashActivity.this).getBoolean(Utils.KEY_IS_LOGGED_IN));
                    if (!Utils.getInstance(SplashActivity.this).getBoolean(Utils.KEY_IS_LOGGED_IN))
                        i = new Intent(SplashActivity.this, SignupActivity.class);
                    else if (Utils.getInstance(SplashActivity.this).getBoolean(Utils.KEY_IS_LOGGED_IN))
//                        i = new Intent(SplashActivity.this,ConnectToyActivity.class);
                        i = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(i);

                }
            }
        };
        splashThread.start();
    }
}
