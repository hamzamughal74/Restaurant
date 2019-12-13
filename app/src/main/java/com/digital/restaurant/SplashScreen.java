package com.digital.restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        int secondsDelayed = 2;
        final Intent intent;
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(LoginScreen.userPersistenceKey, null) == null) {
            intent = new Intent(this, LoginScreen.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
                finish();
            }
        }, secondsDelayed * 1000);
    }
}