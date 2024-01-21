package com.example.bugbustersproject;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class WeatherPopupWindow {

    private Context context;
    private PopupWindow popupWindow;
    private View mainActivityView;
    public String weatherDescription;

    public WeatherPopupWindow(Context context, View mainActivityView) {
        this.context = context;
        this.mainActivityView = mainActivityView;
        initPopupWindow();
    }

    private void initPopupWindow() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.activity_weather_popup, null);
        mainActivityView.setVisibility(View.INVISIBLE);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.showAtLocation(mainActivityView, Gravity.CENTER, 0, 0);

        Button okButton = popupView.findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            dismissPopup();
        });

    }

    public void showPopup(int iconCode, double temperature) {
        ImageView iconImageView = popupWindow.getContentView().findViewById(R.id.weatherImageView);
        TextView tempText = popupWindow.getContentView().findViewById(R.id.tempatureTextView);
        TextView weatherDescriptionText = popupWindow.getContentView().findViewById(R.id.weatherDescriptionTextView);
        int iconDrawableResource = getIconDrawableResource(iconCode);
        iconImageView.setImageResource(iconDrawableResource);
        tempText.setText(temperature + " °C");
        weatherDescriptionText.setText(weatherDescription);
    }
    private int getIconDrawableResource(int iconCode) {
        switch (iconCode) {
            case 200:
            case 201:
            case 202:
            case 230:
            case 231:
            case 232:
            case 233:
                weatherDescription = "Its thunderstorming! Excpect less traffic";
                return R.drawable.icons8_stormy_weather_48;
            case 300:
            case 301:
            case 302:
            case 500:
            case 501:
            case 502:
            case 511:
            case 520:
            case 521:
            case 522:
            case 900:
                weatherDescription = "Its raining! Expect less traffic";
                return R.drawable.icons8_rainfall_48;
            case 600:
            case 601:
            case 602:
            case 610:
            case 611:
            case 612:
            case 621:
            case 622:
            case 623:
                weatherDescription = "Its snowing! Expect less traffic";
                return R.drawable.icons8_snow_94;
            case 700:
            case 711:
            case 721:
            case 731:
            case 741:
            case 751:
            case 761:
            case 762:
            case 771:
            case 781:
            case 801:
            case 802:
            case 803:
            case 804:
                weatherDescription = "Its cloudy! Expect less traffic";
                return R.drawable.icons8_clouds_48;
            case 800:
                weatherDescription = "Its sunny! Expect more traffic";
                return R.drawable.icons8_summer_48;
            default:
                return R.drawable.icons8_summer_48;
        }

    }

    public void dismissPopup() {
        mainActivityView.setVisibility(View.VISIBLE);
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
