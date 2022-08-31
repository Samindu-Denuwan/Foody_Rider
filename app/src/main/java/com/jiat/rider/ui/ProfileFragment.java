package com.jiat.rider.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jiat.rider.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ProfileFragment extends Fragment implements LocationListener {

    private ImageView profileImg;
    private TextView username, userMobile , address, mobileNumberView, emailView;
    private ImageButton btnEdit, btnOrders, btnFav, btnCart;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private static final int LOCATION_REQUEST_CODE = 100;

    private String[] locationPermissions;
    //image Choose Uri
    private Uri image_uri;

    private LocationManager locationManager;

    private double latitude = 0.0, longitude =0.0;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);



        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        profileImg = view.findViewById(R.id.profileImgView);
        username = view.findViewById(R.id.profileName);
        userMobile = view.findViewById(R.id.phoneNumber);
        btnEdit = view.findViewById(R.id.editDetails);
        btnOrders = view.findViewById(R.id.ordersBtn);
        btnFav = view.findViewById(R.id.FavBtn);
        btnCart = view.findViewById(R.id.cartBtn);
        address = view.findViewById(R.id.addressTv);
        mobileNumberView = view.findViewById(R.id.mobileTv);
        emailView = view.findViewById(R.id.emailTv);


        loadMyInfo();

        //order button shortcut
        btnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment =  new OrdersFragment();
                FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();
            }
        });

        /*//favourite button shortcut
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Favourites...", Toast.LENGTH_SHORT).show();
            }
        });


        //cart button shortcut
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Shopping Cart...", Toast.LENGTH_SHORT).show();
            }
        });*/

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getActivity().getApplicationContext(), ProfileEditUserActivity.class);
                // startActivity(intent);
                bottomSheet();

            }
        });


        return view;
    }


    private ImageView imgProfileBtn;
    private ImageButton imgBtnGps, imgBtnBack;
    private Button btnUpdateProfileUser;
    private EditText editTextAddressUser, editTextFullNameUser, editTextPhoneNumUser;

    private static final String TAG = "owner";





    private String userFullName, phoneNumber, addressUser;
    private void bottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_profile_bottomsheet, null);
        bottomSheetDialog.setContentView(view);

        imgProfileBtn = view.findViewById(R.id.imageViewUser);
        imgBtnBack = view.findViewById(R.id.imgBtnBack);
        imgBtnGps = view.findViewById(R.id.imgBtnGetGps);
        btnUpdateProfileUser =view.findViewById(R.id.buttonEditProfileUser);
        editTextAddressUser = view.findViewById(R.id.editTextAddress);
        editTextFullNameUser= view.findViewById(R.id.editTextFullName);
        editTextPhoneNumUser= view.findViewById(R.id.editTextPhoneNum);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        //checkUser();

        //load user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (getActivity() == null) {
                            return;
                        }
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            String name = ""+ds.child("name").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String address = ""+ds.child("address").getValue();
                            String email = ""+ds.child("email").getValue();
                            latitude = Double.parseDouble(""+ds.child("latitude").getValue());
                            longitude = Double.parseDouble(""+ds.child("longitude").getValue());
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String online = ""+ds.child("online").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String uid = ""+ds.child("uid").getValue();

                            editTextAddressUser.setText(address);
                            editTextFullNameUser.setText(name);
                            editTextPhoneNumUser.setText(phone);


                            Glide.with(getActivity())
                                    .load(profileImage)
                                    .circleCrop()
                                    .into(imgProfileBtn);



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        imgBtnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detect Location
                if(checkLocationPermission()){
                    //already allow
                    detectLocation();
                }else{
                    //not allow, request
                    requestLocationPermission();

                }

            }
        });

        imgProfileBtn.setOnClickListener(new View.OnClickListener() {
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


        btnUpdateProfileUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //inputData();

                //input data
                userFullName = editTextFullNameUser.getText().toString().trim();
                phoneNumber = editTextPhoneNumUser.getText().toString().trim();
                addressUser = editTextAddressUser.getText().toString().trim();

                //Data Validate

                if(TextUtils.isEmpty(userFullName)){
                    Toast.makeText(getActivity(), "Enter Name..", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(getActivity(), "Enter Phone Number..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(addressUser)){
                    Toast.makeText(getActivity(), "Please click GPS Button to detect Location..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(latitude == 0.0 ){
                    Toast.makeText(getActivity(), "Please click GPS Button to detect Location..", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressDialog.setMessage("Updating Profile...");
                progressDialog.show();

                if(image_uri == null){
                    //update without image

                    //setup data to save

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", "" + userFullName);
                    hashMap.put("phone", "" + phoneNumber);
                    hashMap.put("address", "" + addressUser);
                    hashMap.put("latitude", "" + latitude);
                    hashMap.put("longitude", "" + longitude);

                    /*hashMap.put("profileImage", "");*/

                    //update to db
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    bottomSheetDialog.dismiss();
                                    //updated
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Profile Updated...", Toast.LENGTH_SHORT).show();
                                    /*finish();*/
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed to update
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }else{
                    //update with image

                    //image first upload
                    String filePathAndName = "profile_images/" +""+ firebaseAuth.getUid();

                    //getStorage reference
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                    storageReference.putFile(image_uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!uriTask.isSuccessful());
                                    Uri downloadImageUri = uriTask.getResult();

                                    if(uriTask.isSuccessful()){
                                        //image url receive, now update

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("name", "" + userFullName);
                                        hashMap.put("phone", "" + phoneNumber);
                                        hashMap.put("address", "" + addressUser);
                                        hashMap.put("latitude", "" + latitude);
                                        hashMap.put("longitude", "" + longitude);
                                        hashMap.put("profileImage", "" + downloadImageUri);

                                        //update to db
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //updated
                                                        bottomSheetDialog.dismiss();
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Profile Updated...", Toast.LENGTH_SHORT).show();
                                                        /*finish();*/
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //failed to update
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }


            }
        });








        bottomSheetDialog.show();
    }




    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(), locationPermissions, LOCATION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(getActivity(), "Please Wait", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 ,0, this);
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {//location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();


    }

    private void findAddress() {
        if (getActivity() == null) {
            return;
        }

        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getActivity() , Locale.getDefault());


        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);

            //set Address
            editTextAddressUser.setText(address);

        }catch (Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getActivity(), "Please Turn On Location", Toast.LENGTH_SHORT).show();
    }


    @SuppressWarnings("deprecation")
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
                        Toast.makeText(getActivity(), "Location Permission is necessary..", Toast.LENGTH_SHORT).show();

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
                        Glide.with(getActivity()).load(image_uri).circleCrop().into(imgProfileBtn);
                        Log.i(TAG, image_uri.getPath());
                    }
                }
            }
    );


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (getActivity() == null) {
                            return;
                        }
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String user_address = ""+ds.child("address").getValue();
                            String img = ""+ds.child("profileImage").getValue();
                            String phone = ""+ds.child("phone").getValue();

                            username.setText(name);
                            mobileNumberView.setText(phone);
                            userMobile.setText(phone);
                            emailView.setText(email);
                            address.setText(user_address);
                            Glide.with(getActivity()).load(img).circleCrop()
                                    .into(profileImg);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

}