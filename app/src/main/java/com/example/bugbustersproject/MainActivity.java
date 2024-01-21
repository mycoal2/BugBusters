    package com.example.bugbustersproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

    public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }
    protected void setupUI() {
        // Define Button
        Button buttonTestPage;
        buttonTestPage = findViewById(R.id.buttonTestPage);
        Button buttonMapRoutes;
        buttonMapRoutes = findViewById(R.id.buttonMapRoutes);


        buttonTestPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Page
                Intent intent = new Intent(getApplicationContext(), buttonTestPage.class);
                startActivity(intent);
            }
        });
        buttonMapRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Page
                Intent intent = new Intent(getApplicationContext(), mapRoutes.class);
                startActivity(intent);
            }
        });
    }



}