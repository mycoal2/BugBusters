package com.example.bugbustersproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent goMain = new Intent(SplashScreen.this, bixiMap.class);
                SplashScreen.this.startActivity(goMain);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
                SplashScreen.this.finish();
            }
        }, 5000);

    }
}