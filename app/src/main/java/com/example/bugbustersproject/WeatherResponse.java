package com.example.bugbustersproject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    @SerializedName("data")
    private List<WeatherData> data;

    public List<WeatherData> getData() {
        return data;
    }

    public static class WeatherData {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("weather")
        private WeatherInfo weatherInfo;

        public double getTemperature() {
            return temperature;
        }

        public WeatherInfo getWeatherInfo() {
            return weatherInfo;
        }
    }

    public static class WeatherInfo {
        @SerializedName("icon")
        private String icon;

        @SerializedName("code")
        private int code;

        @SerializedName("description")
        private String description;

        public String getIcon() {
            return icon;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
