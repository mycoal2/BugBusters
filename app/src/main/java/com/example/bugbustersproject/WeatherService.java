package com.example.bugbustersproject;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class WeatherService {

    private static final String MY_API_KEY = "040f1762ace04cecb329fc5412a85dd4";
    private static final String BASE_URL = "https://api.weatherbit.io/v2.0/current";
    private static final String CITY = "Montreal";

    public static String getWeather() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "?city=" + CITY + "&key=" + MY_API_KEY)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public static WeatherResponse parseWeatherData(String json)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, WeatherResponse.class);
    }
}
