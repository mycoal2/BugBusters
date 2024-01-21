package com.example.bugbustersproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class buttonTestPage extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myGoogleMap;
    private SearchView mapSearchView;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_test_page);
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

        mapSearchView = findViewById(R.id.mapSearchView);

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchedLocation = mapSearchView.getQuery().toString();
                Address address = null;

                //TODO is if necessary?
                if (searchedLocation != null) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());

                    try {
                        address = geocoder.getFromLocationName(searchedLocation, 1).get(0);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                    myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void searchButtonClicked() {
        //TODO search data again based on time

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "clicked", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", view -> {

                })
                .show();
    }
}