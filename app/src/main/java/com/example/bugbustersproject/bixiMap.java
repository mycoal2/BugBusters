package com.example.bugbustersproject;
import static com.android.volley.VolleyLog.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
public class bixiMap extends AppCompatActivity implements OnMapReadyCallback {

    private TextView timeTextView;
    private GoogleMap myGoogleMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private SupportMapFragment mapFragment;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");

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
        LoadBixiStations();

        View parentLayout = findViewById(android.R.id.content);
        myGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String myString = marker.getPosition().latitude + ", " + marker.getPosition().longitude;
                Snackbar snackbar = Snackbar.make(parentLayout, marker.getTitle(), Snackbar.LENGTH_LONG);
                snackbar.show();
                return false;
            }
        });
    }

    private final View.OnClickListener searchButtonOnClickListener = v -> searchButtonClicked();

    protected void setupUI() {
        // Define Button
        Button buttonMainActivity;
        buttonMainActivity = findViewById(R.id.buttonMainActivity);

        buttonMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFirestoreData();
//                addRTData();
//                readRTData();
            }
        });

        Button searchButton = findViewById(R.id.searchButton);

        timeTextView = findViewById(R.id.timeTextView);

        timeTextView.setOnClickListener(searchButtonOnClickListener);
        Calendar currentTime = Calendar.getInstance();
        String timeString = currentTime.get(Calendar.HOUR_OF_DAY) + ":" + currentTime.get(Calendar.MINUTE);
        timeTextView.setText(timeString);

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
                            Log.d("TEST", "TESTST");
                            address = addressList.get(0);

                            // Check if the address is not null before using it
                            if (address != null) {
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                myGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Concordia"));
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

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                    myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }


            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    public void searchButtonClicked() {
        //TODO search data again based on time

        View parentLayout = findViewById(android.R.id.content);

        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMin = currentTime.get(Calendar.MINUTE);

        //TODO figure add a zero if single digit
        String currentTimeString = currentHour + ":" + currentMin;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timeTextView.setText(hourOfDay + ":" + minute);
            }
        }, currentHour, currentMin, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void LoadBixiStations() {
        BixiStationGenerator bixiStationGenerator = new BixiStationGenerator();
        List<BixiSation> bixiSationList = bixiStationGenerator.getBixiStations();

        for(BixiSation bixiSation : bixiSationList) {
            LatLng latLng = new LatLng(bixiSation.getLatitude(), bixiSation.getLongitude());
            myGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .position(latLng)
                    .title(bixiSation.getIntersectionName()));
        }
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
                        WeatherPopupWindow weatherPopupWindow = new WeatherPopupWindow(bixiMap.this, findViewById(android.R.id.content));
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

    private void readFirestoreData() {
        db.collection("STATION_REF")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    private void addRTData() {
        // Write a message to the database
        myRef.setValue("Hello, World!");
        Log.d(TAG, "helo");
    }
    private void readRTData() {
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}