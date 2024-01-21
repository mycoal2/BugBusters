package com.example.bugbustersproject;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class mapRoutes extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng origin;
    private LatLng destination;
    private Polyline currentPolyline;
    private String apiKey;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");

    public static String getGoogleMapsApiKey() {
        Properties properties = new Properties();
        Log.d("tag2", "not return null3");
        try (FileInputStream fileInputStream = new FileInputStream("local.properties")) {
            Log.d("tag2", "not return null2");
            properties.load(fileInputStream);
            Log.d("tag2", "not return null");
            return properties.getProperty("MY_API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("tag2", "return null lol");
            // Handle exception (e.g., log, display an error message)
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_routes);

        setupUI();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set your origin and destination coordinates
        origin = new LatLng(45.5242361, -73.5815522); // Example: San Francisco, CA
        destination = new LatLng(45.5341344, -73.5735244); // Example: Los Angeles, CA

    }

    protected void setupUI() {
        // Define Button
        Button buttonMainActivity;
        Button Tab2;
        buttonMainActivity = findViewById(R.id.buttonMainActivity);
        Tab2 = findViewById(R.id.Tab2);

        buttonMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Page
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        Tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addData();
//                readData();

//                addRTData();
                readRTData();

                // Request directions when your activity starts
//                requestDirections();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(45.5242361, -73.5815522);
        googleMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private void addData() {
        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Alan");
        user.put("middle", "Mathison");
        user.put("last", "Turing");
        user.put("born", 1912);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
    private void readData() {
        db.collection("users")
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
    private void requestDirections() {
        new DirectionsTask().execute();
    }

    private class DirectionsTask extends AsyncTask<Void, Void, DirectionsResult> {

        @Override
        protected DirectionsResult doInBackground(Void... voids) {
            try {
//                apiKey = getGoogleMapsApiKey();
                Log.d("taglol", "api = " + apiKey);
                GeoApiContext context = new GeoApiContext.Builder()
                        .apiKey("AIzaSyBH5W5rUwIp_pLhoR9eHzu2lM6zJ-XcK-M")
                        .build();

                return DirectionsApi.newRequest(context)
                        .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                        .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(TravelMode.BICYCLING)
                        .await();
            } catch (Exception e) {
                Log.e("DirectionsTask", "Error getting directions", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(DirectionsResult directionsResult) {
            super.onPostExecute(directionsResult);

            if (directionsResult != null && mMap != null) {
                // Parse the directions result and draw the polyline on the map
                drawDirections(directionsResult);
            }
        }
    }

    private void drawDirections(DirectionsResult directionsResult) {
        // Clear previous polyline if any
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        // Extract the encoded polyline from the directions result
        String encodedPolyline = directionsResult.routes[0].overviewPolyline.getEncodedPath();

        // Decode the polyline and add it to the map
        List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
        currentPolyline = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

        // Optional: Zoom to fit the bounds of the polyline
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(directionsResult.routes[0].bounds, 100));
    }

}