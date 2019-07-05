package com.onewayit.veki.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.onewayit.veki.R;

public class SplashScreen extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        initializeVariables();
        callDelay();


    }

    private void initializeVariables() {
        context = SplashScreen.this;
    }

    private void goToNextActivity() {
        Intent intent = new Intent(context, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void callDelay() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNextActivity();
            }
        }, 2000);
    }


}
