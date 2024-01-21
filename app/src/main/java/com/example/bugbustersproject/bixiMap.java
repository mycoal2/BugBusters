package com.example.bugbustersproject;
import static com.android.volley.VolleyLog.TAG;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
public class bixiMap extends AppCompatActivity implements OnMapReadyCallback {

    private TextView timeTextView;
    private GoogleMap myGoogleMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private SupportMapFragment mapFragment;
    private String currentNormValue;

    private Boolean isRequestIn = false;
    private FloatingActionButton fab;
    private FloatingActionButton fab2;

    private Double latitude;
    private Double longitude;
    boolean toggle = true;
    List<DocumentSnapshot> savedDocument = null;

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
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        Switch switchMode = findViewById(R.id.toggleRequest);
        fab = findViewById(R.id.fabButton);
        fab2 = findViewById(R.id.fabButton2);
        switchMode.setText("Pickup Bike Availability");
        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isRequestIn = true;
                switchMode.setText("Docking Bike Availability");
            } else {
                isRequestIn = false;
                switchMode.setText("Pickup Bike Availability");
            }
        });

            fab.setOnClickListener(v -> {
                if (latitude != null && longitude != null) {
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
                }

            });
            fab2.setOnClickListener(v -> {
                if(toggle) {
                    readFirestoreData();
                } else {
                    myGoogleMap.clear();
                    if(savedDocument == null) {

                    } else {
                        LatLng latLng = new LatLng(latitude, longitude);
                        myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                        for (DocumentSnapshot document : savedDocument) {
                            LatLng latLong = new LatLng(document.getDouble("lat"), document.getDouble("lon"));

                            myGoogleMap.addMarker(new MarkerOptions().
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                    .position(latLong).title(document.getString("station_name")));
                            Log.d("TEST4444", document.getId() + " => " + document.getData());
                        }
                    }
                }
                toggle = !toggle;



            });




        FetchWeatherTask();
        setupUI();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        LatLng montreal = new LatLng(45.5017, -73.5673);
        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(montreal, 9));
//        readFirestoreData();

        View parentLayout = findViewById(android.R.id.content);
        myGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(marker.getTitle() == null) {
                    return false;
                }
                requestDataPrediction(marker.getPosition());
                fetchDataPrediction();

                Snackbar snackbar = Snackbar.make(parentLayout, currentNormValue, Snackbar.LENGTH_LONG);
                snackbar.show();

                return false;
            }
        });
    }

    private int adjustDayOfWeekToPython(int dayOfWeek) {
        if (dayOfWeek == 1) {
            return 6;
        }
        return dayOfWeek - 2;
    }

    private void requestDataPrediction(LatLng position) {
        Calendar calendar = Calendar.getInstance();
        calendar.get(Calendar.DAY_OF_WEEK);

        //TODO move to not create reference on every click
        DatabaseReference databaseReference = database.getReference("/request/information");

        int dayOfWeek = adjustDayOfWeekToPython(calendar.get(Calendar.DAY_OF_WEEK));
        int hour = 12; //dummy value incase it crashes in try catch
        
        try {
            String hourText = timeTextView.getText().toString();
            hour = Integer.parseInt(hourText.split(":")[0]);
        } catch (Exception e) {
            Log.d("in catch", "aaaaaaaaaaaaaaaa");
        }
        databaseReference.child("day_of_week").setValue(dayOfWeek).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
        databaseReference.child("hour").setValue(hour).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
        databaseReference.child("lat").setValue(position.latitude).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
        databaseReference.child("lon").setValue(position.longitude).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
        databaseReference.child("month").setValue(6).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
        //TODO add switch mode
        databaseReference.child("is_incoming_bike").setValue(true).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));


        DatabaseReference ref = database.getReference("/request");
        ref.child("request_in").setValue(true).addOnSuccessListener(unused -> Log.d("test", "successfully updated!"));
    }

    private void fetchDataPrediction() {
        DatabaseReference databaseReference = database.getReference("/request/information_out");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    for(DataSnapshot ss: snapshot.getChildren()) {
                        if (ss.getKey().equals("cat")) {
                            Log.d("Hello", ss.getValue().toString());
                            String textToDisplay = isRequestIn ? "Docking Bike Availability: " : "Pickup Bike Availability: ";
                            currentNormValue =  textToDisplay +  ss.getValue().toString();
                        }
                    }
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar snackbar = Snackbar.make(parentLayout, currentNormValue, Snackbar.LENGTH_LONG);
                    snackbar.show();
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private final View.OnClickListener searchButtonOnClickListener = v -> searchButtonClicked();

    protected void setupUI() {


        timeTextView = findViewById(R.id.timeTextView);
        timeTextView.setOnClickListener(searchButtonOnClickListener);

        Calendar currentTime = Calendar.getInstance();
        String timeString = currentTime.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", currentTime.get(Calendar.MINUTE));
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
                        addressList = geocoder.getFromLocationName(searchedLocation, 1);

                        if (addressList != null && !addressList.isEmpty()) {
                            toggle = !toggle;
                            Log.d("TEST", "TESTST");
                            address = addressList.get(0);

                            Log.d("TEST123", "hello " + address);
                            // Check if the address is not null before using it
                            if (address != null) {
                                latitude = address.getLatitude();
                                longitude = address.getLongitude();
                                myGoogleMap.clear();
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                                myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                                Log.d("TEST234", "LAT - " + address.getLatitude() + " long - " + address.getLongitude());
                                double latRange = calculateLatBounds(350);
                                double longRange = calculateLongBounds(350);
                                Log.d("lat - long", "lat - " + latRange + " long - " + longRange);
                                double latUpperBound = address.getLatitude() + latRange;
                                double latLowerBound = address.getLatitude() - latRange;
                                double longUpperBound = address.getLongitude() + longRange;
                                double longLowerBound = address.getLongitude() - longRange;
                                Log.d("bounds", "latU - " + latUpperBound + "latL -" + latLowerBound + "longU- " + longUpperBound + "longL - " + longLowerBound);

                                db.collection("STATION_REF")
                                        .whereGreaterThanOrEqualTo("lat", latLowerBound).whereLessThan("lat", latUpperBound).get()
//                                        .whereGreaterThanOrEqualTo("lon", longLowerBound).whereLessThan("lon", longUpperBound).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // Handle the results for latitude query
                                                    List<DocumentSnapshot> latResults = task.getResult().getDocuments();

                                                    List<DocumentSnapshot> filteredResults = new ArrayList<>();

                                                    for (DocumentSnapshot doc : latResults) {
                                                        double lon = doc.getDouble("lon");
                                                        if (lon > longLowerBound && lon < longUpperBound) {
                                                            // Document satisfies both latitude and longitude conditions
                                                            filteredResults.add(doc);
                                                        }
                                                    }
                                                    if(filteredResults.isEmpty()) {
                                                        Snackbar.make(findViewById(android.R.id.content), "No Stations Nearby", Snackbar.LENGTH_LONG).show();
                                                    } else {
                                                        savedDocument = filteredResults;
                                                        for (DocumentSnapshot document : filteredResults) {
                                                            LatLng latLong = new LatLng(document.getDouble("lat"), document.getDouble("lon"));

                                                            myGoogleMap.addMarker(new MarkerOptions().
                                                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                                                    .position(latLong).title(document.getString("station_name")));
                                                            Log.d("TEST4444", document.getId() + " => " + document.getData());
                                                        }
                                                    }

                                                } else {
                                                    Log.w("55555", "Error getting documents.", task.getException());
                                                }
                                            }
                                        });

                                Log.d("test123", "db firestore");
                                myGoogleMap.addMarker(new MarkerOptions().position(latLng));
                                myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), "Invalid address", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
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
                timeTextView.setText(hourOfDay + ":" + String.format("%02d", minute));
            }
        }, currentHour, currentMin, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
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

    private double calculateLatBounds(int distance) {

        // new_latitude  = latitude  + (dy / r_earth) * (180 / pi);
        // r_earth = 6378km = 6378 000m
        double r_earth = 6378000;
        double latBound = (distance/r_earth) * (180/PI);
        return latBound;
    }

    private double calculateLongBounds(int distance) {
        // new_longitude = longitude + (dx / r_earth) * (180 / pi) / cos(latitude * pi/180);
        // r_earth = 6378km = 6378 000m
        double r_earth = 6378000;
        double longBound = (distance/r_earth) * (180/PI) / cos(PI/180);
        return longBound;
    }

    private void readFirestoreData() {
        db.collection("STATION_REF")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String stationName = document.getString("station_name");
                                Double lat = document.getDouble("lat");
                                Double lng = document.getDouble("lon");


                                // Check if lat and lng are not null before using them
                                if (lat != null && lng != null) {
                                    LatLng latLng = new LatLng(lat, lng);
                                    myGoogleMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                            .position(latLng)
                                            .title(stationName)
                                    );
                                } else {
                                    Log.w(TAG, "Latitude or Longitude is null for document " + document.getId());
                                }

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