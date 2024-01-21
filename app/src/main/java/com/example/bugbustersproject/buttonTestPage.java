package com.example.bugbustersproject;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class buttonTestPage extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myGoogleMap;
    private SearchView mapSearchView;
    private AutocompleteSupportFragment autocompleteFragment;
    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_test_page);
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), "AIzaSyBH5W5rUwIp_pLhoR9eHzu2lM6zJ-XcK-M");
        }
        PlacesClient placesClient = Places.createClient(this);
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        FetchWeatherTask();
        setupUI();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Customize the map if needed
        myGoogleMap = googleMap;
    }

    private final View.OnClickListener searchButtonOnClickListener = v -> searchButtonClicked();

    protected void setupUI() {
        // Define Button
        Button buttonMainActivity;
        buttonMainActivity = findViewById(R.id.buttonMainActivity);


        buttonMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Page
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(searchButtonOnClickListener);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                String searchedLocation = place.getName();
                List<Address> addressList = null;
                Address address = null;

                // Check if the location name is not null or empty
                if (searchedLocation != null && !searchedLocation.isEmpty()) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());

                    try {
                        // Perform geocoding
                        addressList = geocoder.getFromLocationName(searchedLocation, 1);

                        // Check if the addressList is not empty
                        if (addressList != null && !addressList.isEmpty()) {
                            address = addressList.get(0);

                            // Check if the address is not null before using it
                            if (address != null) {
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                                myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            } else {
                                // Handle the case where address is null
                                Snackbar.make(findViewById(android.R.id.content), "Invalid address", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle the case where addressList is empty
                            Snackbar.make(findViewById(android.R.id.content), "No address found", Snackbar.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            @Override
            public void onError(@NonNull Status status) {

            }
        });
        //mapSearchView = findViewById(R.id.mapSearchView);

//        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                String searchedLocation = mapSearchView.getQuery().toString();
//                List<Address> addressList = null;
//                Address address = null;
//
//                //TODO is if necessary?
//                if (searchedLocation != null) {
//                    Geocoder geocoder = new Geocoder(getApplicationContext());
//
//                    try {
//                        addressList = geocoder.getFromLocationName(searchedLocation, 1);
//                        address = addressList.get(0);
//                        if(address == null) {
//                            return false;
//                        }
//
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                    myGoogleMap.addMarker(new MarkerOptions().position(latLng));
//                    myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
    }

    public void searchButtonClicked() {
        //TODO search data again based on time

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "clicked", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", view -> {

                })
                .show();
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
                        WeatherPopupWindow weatherPopupWindow = new WeatherPopupWindow(buttonTestPage.this, findViewById(android.R.id.content));
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

}