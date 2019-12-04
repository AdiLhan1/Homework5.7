package com.example.servicerr;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.servicerr.data.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnLocationClickListener, OnCameraTrackingChangedListener, MapboxMap.OnMapClickListener {
    private EditText etDesc;
    private ImageView imageView, image;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private boolean isInTrackingMode;
    private final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        initView();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        callPermissions();
        clickListener();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    public void addInService(Location location) {
        String latitudeStr = String.valueOf(location.getLatitude());
        String longitudeStr = String.valueOf(location.getLongitude());
        Intent broadcast1 = new Intent("getting_data");
        broadcast1.putExtra("value", latitudeStr);
        broadcast1.putExtra("value", longitudeStr);
        sendBroadcast(broadcast1);

    }

    public void requestLocationUpdates(Style loadedMapStyle) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
            if (location != null) {
                Log.e(TAG, "onSuccess: " + location);
                String latitudeStr = String.valueOf(location.getLatitude());
                String longitudeStr = String.valueOf(location.getLongitude());
                latitude.setText(latitudeStr);
                longitude.setText(longitudeStr);
                addInService(location);

            }
        });
        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                .elevation(5)
                .accuracyAlpha(.6f)
                .accuracyColor(Color.RED)
                .build();

        // Get an instance of the component
        locationComponent = mapboxMap.getLocationComponent();

        LocationComponentActivationOptions locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build();

        // Activate with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);

        // Add the location icon click listener
        locationComponent.addOnLocationClickListener(this);

        // Add the camera tracking listener. Fires if the map camera is manually moved.
        locationComponent.addOnCameraTrackingChangedListener(this);

        findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(view -> {
            if (!isInTrackingMode) {
                isInTrackingMode = true;
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.zoomWhileTracking(16f);
                Toast.makeText(MainActivity.this, "right",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "not right",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void callPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            requestLocationUpdates(style);
            // Toast instructing user to tap on the map
            Toast.makeText(
                    MainActivity.this,
                    "MApready",
                    Toast.LENGTH_LONG
            ).show();
            mapboxMap.addOnMapClickListener(MainActivity.this);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            cameraUpdate(new LatLng(location.getLatitude(), location.getLongitude()));

                        }
                    });
        });
    }

    private void cameraUpdate(LatLng latLng) {
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng) // Sets the new camera position
                .zoom(17) // Sets the zoom
                .bearing(180) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }


    private void initView() {
        mapView = findViewById(R.id.mapView);
        etDesc = findViewById(R.id.etDescription);
        imageView = findViewById(R.id.image_second);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);


    }

    private void clickListener() {
        imageView.setOnClickListener(v -> {
            stopForeground();
            imageView.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
        });
        image = findViewById(R.id.image);
        image.setOnClickListener(v -> {
            startForeground();
            image.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMessage(Event event) {
        Toast.makeText(this, event.getTitle(), Toast.LENGTH_LONG).show();

    }

    private void startForeground() {
        Intent intent = new Intent(this, TrackingService.class);
        startService(intent);
    }

    private void stopForeground() {
        Intent intent = new Intent(this, TrackingService.class);
        stopService(intent);
    }

    private String getTextFromEt() {
        if (etDesc.getText() == null) return "No typed description";
        return etDesc.getText().toString();
    }


    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;

    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
        // Empty on purpose

    }

    @Override
    public void onLocationComponentClick() {
    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        // Toast instructing user to tap on the map
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }
}
