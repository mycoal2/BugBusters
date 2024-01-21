package com.example.bugbustersproject;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

    public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FetchWeatherTask();
        setupUI();
    }
        private void FetchWeatherTask() {
            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String weather = WeatherService.getWeather();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WeatherPopupWindow weatherPopupWindow = new WeatherPopupWindow(MainActivity.this, findViewById(android.R.id.content));
                            WeatherResponse weatherResponse = WeatherService.parseWeatherData(weather);

                            // Update UI based on the parsed data
                            if (weatherResponse != null && weatherResponse.getData() != null && !weatherResponse.getData().isEmpty()) {
                                WeatherResponse.WeatherData weatherData = weatherResponse.getData().get(0);
                                double temperature = weatherData.getTemperature();
                                WeatherResponse.WeatherInfo weatherInfo = weatherData.getWeatherInfo();

                                // Set temperature and description to TextViews

                                // Access the weather icon directly
                                int iconCode = weatherInfo.getCode();
                                weatherPopupWindow.showPopup(iconCode, temperature);
                            }
                        }
                    });
                }
            });
        }

    protected void setupUI() {
        Button buttonTestPage;
        buttonTestPage = findViewById(R.id.buttonTestPage);
        Button buttonMapRoutes;
        buttonMapRoutes = findViewById(R.id.buttonMapRoutes);


        buttonTestPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Page
                Intent intent = new Intent(getApplicationContext(), bixiMap.class);
                startActivity(intent);
            }
        });
        buttonMapRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }



}