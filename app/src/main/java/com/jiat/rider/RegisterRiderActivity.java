package com.jiat.rider;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterRiderActivity extends AppCompatActivity implements LocationListener {

    private EditText fullName, phoneNum, addressUser, email, password, reTypePassword;
    private Button btnRegisterUser;
    private ImageButton imgBtnBack, imgBtnGps;
    private ImageView imageViewUser;
    private TextView sellerRegi;

    private static final String TAG = "owner";
    private static final int LOCATION_REQUEST_CODE = 100;


    private String[] locationPermissions;


    //image Choose Uri
    private Uri image_uri;

    private LocationManager locationManager;

    private double latitude = 0.0, longitude =0.0;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String shopUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_rider);

        shopUID = "d6RHVgGQoNZMkciEMVl16lSSsIw2";

        imageViewUser = findViewById(R.id.imageViewUser);

        fullName = findViewById(R.id.editTextFullName);
        phoneNum = findViewById(R.id.editTextPhoneNum);
        addressUser = findViewById(R.id.editTextAddress);
        password = findViewById(R.id.editTextPassword);
        reTypePassword = findViewById(R.id.editTextRetypePassword);
        email = findViewById(R.id.editTextEmail);

        imgBtnBack = findViewById(R.id.imgBtnBack);
        imgBtnGps = findViewById(R.id.imgBtnGetGps);


        btnRegisterUser = findViewById(R.id.buttonRegisterUser);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        Glide.with(this)
                .load(R.drawable.profile)
                .circleCrop()
                .into(imageViewUser);

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imgBtnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detecting current Location
                if(checkLocationPermission()){
                    //already allow
                    detectLocation();
                }else{
                    //not allow, request
                    requestLocationPermission();

                }

            }
        });

        imageViewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image choose
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                //startActivityForResult(Intent.createChooser(intent, "Select Image"),IMAGE_REQUEST );
                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));

            }
        });

        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

    }

    private String userFullName, phoneNumber, address, userEmail, userPassword, confirmPassword;
    private void inputData() {
        //input data
        userFullName = fullName.getText().toString().trim();
        phoneNumber = phoneNum.getText().toString().trim();
        address = addressUser.getText().toString().trim();
        userEmail = email.getText().toString().trim();
        userPassword = password.getText().toString().trim();
        confirmPassword = reTypePassword.getText().toString().trim();

        //Data Validate

        if(TextUtils.isEmpty(userFullName)){
            Toast.makeText(this, "Enter Name..", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Enter Phone Number..", Toast.LENGTH_SHORT).show();
            return;
        }
        if(latitude == 0.0 ){
            Toast.makeText(this, "Please click GPS Button to detect Location..", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            Toast.makeText(this, "Invalid Email Pattern..", Toast.LENGTH_SHORT).show();
            return;
        }
        if(userPassword.length()<6){
            Toast.makeText(this, "Password Must be at lease 6 characters...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!userPassword.equals(confirmPassword)){
            Toast.makeText(this, "Password doesn't match..", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();


    }

    private void createAccount() {
        progressDialog.setMessage("Create Account..");
        progressDialog.show();

        //create Acc
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //acc created
                        saverFirebaseData();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterRiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Sending Account Info to Owner...");

        String timestamp = ""+System.currentTimeMillis();

        if(image_uri == null){
            //save info without image

            //setup data to save

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + userEmail);
            hashMap.put("name", "" + userFullName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "Rider");
            hashMap.put("online", "false" );
            hashMap.put("profileImage", "");
            hashMap.put("registerStatus", "false");


            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db update
                            progressDialog.dismiss();


                            AlertDialog.Builder  builder = new AlertDialog.Builder(RegisterRiderActivity.this);
                            builder.setTitle("Getting Approve")
                                    .setMessage("Registered..Owner will Approve Later...")
                                    .setCancelable(true)
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    })
                                    .show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterRiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                        }
                    });


        }else{
            //save info with img

            //img name and path
            String filePathAndName = "profile_images/" + ""+firebaseAuth.getUid();
            //upload img
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){

                                //setup data to save

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", "" + userEmail);
                                hashMap.put("name", "" + userFullName);
                                hashMap.put("phone", "" + phoneNumber);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("accountType", "Rider");
                                hashMap.put("online", "false" );
                                hashMap.put("registerStatus", "false");
                                hashMap.put("profileImage", ""+ downloadImageUri);

                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db update
                                                progressDialog.dismiss();
                                                AlertDialog.Builder  builder = new AlertDialog.Builder(RegisterRiderActivity.this);
                                                builder.setTitle("Getting Approve")
                                                        .setMessage("Registered..Owner will Approve Later...")
                                                        .setCancelable(true)
                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                finish();
                                                            }
                                                        })
                                                        .show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(RegisterRiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterRiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please Wait", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 ,0, this);
    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);

            //set Address
            addressUser.setText(address);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

        //gps location disable
        Toast.makeText(this, "Please Turn On Location", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission Allow
                        detectLocation();
                    }else{
                        //permission Denied
                        Toast.makeText(this, "Location Permission is necessary..", Toast.LENGTH_SHORT).show();

                    }
                }
            }break;

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        image_uri = result.getData().getData();
                        Glide.with(RegisterRiderActivity.this).load(image_uri).circleCrop().into(imageViewUser);
                        Log.i(TAG, image_uri.getPath());
                    }
                }
            }
    );
}