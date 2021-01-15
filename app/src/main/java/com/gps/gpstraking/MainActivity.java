package com.gps.gpstraking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    // refrences to the UI elements
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_addresses;

    Switch sw_locationupdates, sw_gps;

    // variable to track if we are tracking location or not.
    boolean updateOn = false;

    // location request is a config file for all settings related to FusedLocationProvidersClient

    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    // Google's API for location services. The majorety of the app functions using thiis class.
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // give each UI variable value
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_addresses = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);


        // set all properties locationRequest

        locationRequest = new LocationRequest();

        // how often does the default locationcheck occur?

        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        // how often does the check location occure when set to the most frequent update?

        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        // event that is trigred whenever the update interval is met
        locationCallBack = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save location
                updateUIValue(locationResult.getLastLocation());

            }
        };





        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    // most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Tower + WIFI");
                }
            }
        });


        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    // turn on location tracking
                    startLocationUpdates();
                } else {
                    //turn off tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();


    }// end onCreate method

    private void stopLocationUpdates() {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText("Not traching location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not traching location");
        tv_addresses.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }

    private void startLocationUpdates() {


        tv_updates.setText("Location is being tracked");

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else{
                Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

    private void updateGPS(){
        // get permissions from the user to track GPS
        // get the current location from the fused client
        // update the UI -i.e set all properties in their associated text view item

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // we  gor parmission. Put the values of location. XXX into the UI components.
                    if(location!=null){
                        updateUIValue(location);
                    }

                }
            });
        }
        else{
            // permission not granted yet.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValue(Location location) {
        // update all of the text view objects with a new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf((location.getAltitude())));

        }
        else{
            tv_altitude.setText("Not available");
        }
        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf((location.getSpeed())));

        }
        else{
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
            tv_addresses.setText(addresses.get(0).getAddressLine(0));
        }catch (Exception e){
            tv_addresses.setText("Unable to get street address");

        }

    }

}