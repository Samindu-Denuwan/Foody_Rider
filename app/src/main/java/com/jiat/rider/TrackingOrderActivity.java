package com.jiat.rider;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.jiat.rider.databinding.ActivityTrackingOrderBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 1312;
    private static final String TAG = "map";
    private GoogleMap mMap;
    private ActivityTrackingOrderBinding binding;
    private Marker marker_me, marker_Customer;
    private FirebaseAuth firebaseAuth;
    private String desti_Latitude, desti_Longitude, customerName;
    private String orderBy;
    private Polyline polyline;
    private LatLng riderPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrackingOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        orderBy = getIntent().getStringExtra("orderBy");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_style);
        mMap.setMapStyle(style);


        if (checkPermissions()) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_REQUEST_CODE
            );
        }
        loadCustomerLocation();



        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);

                    }

                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        Log.i(TAG, locationResult.getLastLocation().toString());

                        Location lastLocation = locationResult.getLastLocation();

                        riderPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());


                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("riderLat", "" + lastLocation.getLatitude());
                        hashMap.put("riderLong", "" + lastLocation.getLongitude());

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Temp_Location").setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //db update
                                        if(marker_me == null){
                                            MarkerOptions markerOptions = new MarkerOptions();
                                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.m1));
                                            markerOptions.title("you");
                                            markerOptions.position(riderPosition);
                                            markerOptions.anchor((float) 0.5, (float) 0.5);
                                            markerOptions.rotation(lastLocation.getBearing());
                                            float result[] = new float[10];
                                            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), Cus_lat,Cus_longi, result );
                                            markerOptions.snippet("Distance: "+result[0]+"m");
                                            marker_me = mMap.addMarker(markerOptions);
                                            moveCamera(riderPosition);
                                        }else{
                                            marker_me.setPosition(riderPosition);
                                            marker_me.setRotation(lastLocation.getBearing());



                                        }
                                        getDirection(riderPosition, marker_Customer.getPosition());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                    }
                                });







                    }
                }, Looper.getMainLooper());





    }



    public void loadCustomerLocation() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        desti_Latitude = ""+snapshot.child("latitude").getValue();
                        desti_Longitude = ""+snapshot.child("longitude").getValue();
                        customerName = ""+snapshot.child("name").getValue();

                        markCustomer(desti_Latitude, desti_Longitude,customerName );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }



private  double Cus_lat, Cus_longi;
    public void markCustomer(String desti_latitude, String desti_longitude, String customerName) {
         Cus_lat = Double.parseDouble(desti_latitude);
         Cus_longi = Double.parseDouble(desti_longitude);
        LatLng latLng1 = new LatLng(Cus_lat, Cus_longi);

        if(marker_Customer == null){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_marker));
            markerOptions.title("Customer: "+customerName);
            markerOptions.position(latLng1);
            marker_Customer = mMap.addMarker(markerOptions);

        }else{
            marker_Customer.setPosition(latLng1);
        }
       //


    }

    public void getDirection(LatLng start, LatLng end) {
        okhttp3.OkHttpClient client = new OkHttpClient();

        String URL = "https://maps.googleapis.com/maps/api/directions/json?origin="
                +start.latitude
                +","
                +start.longitude
                +"&destination="
                +end.latitude
                +","
                +end.longitude
                +"&alternatives=true&key="
                +getString(R.string.direction_api_key);

        Log.i(TAG, URL);


        okhttp3.Request request = new Request.Builder()
                .url(URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray routes = jsonObject.getJSONArray("routes");

                    Log.i(TAG, routes.length()+"");

                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");

                    Log.i(TAG, overviewPolyline.toString());

                    List<LatLng> points = PolyUtil.decode((overviewPolyline.getString("points")));



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(polyline == null){
                                PolylineOptions polylineOptions = new PolylineOptions();
                                polylineOptions.width(10);
                                polylineOptions.color(getColor(R.color.black));

                                polylineOptions.addAll(points);
                                polyline = mMap.addPolyline(polylineOptions);
                            }else{
                                polyline.setPoints(points);
                            }

                        }
                    });




                }catch (JSONException e){
                    e.printStackTrace();
                }


            }
        });

    }

    public void moveCamera(LatLng latLng){
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(15f)
                .build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(cameraUpdate);
    }


    public boolean checkPermissions() {
        boolean permissions = false;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            permissions = true;
        }
        return permissions;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionsGranted = false;
        if (requestCode == LOCATION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsGranted = true;
                } else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                        &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsGranted = true;

                }

            }
            if (permissionsGranted) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

}