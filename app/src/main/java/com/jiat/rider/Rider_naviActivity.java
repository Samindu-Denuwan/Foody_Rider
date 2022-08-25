package com.jiat.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jiat.rider.ui.HomeFragment;
import com.jiat.rider.ui.OrdersFragment;
import com.jiat.rider.ui.ProfileFragment;
import com.jiat.rider.ui.SettingsFragment;

import java.util.HashMap;

public class Rider_naviActivity extends AppCompatActivity {

    private final int ID_HOME = 1;
    private final int ID_ORDERS = 2;
    private final int ID_ACCOUNT = 3;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;

    Fragment fragment = null;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private TextView usernameHeader;
    private TextView textViewEmail;
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_navi);



        //pre load home
        loadFragment(new HomeFragment());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();




        //side navi
        drawerLayout = findViewById(R.id.side_drawer);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        getSupportActionBar().setTitle("Home");
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id)
                {
                    case R.id.menu_home:
                        fragment = new HomeFragment();
                        loadFragment_side(fragment);
                        getSupportActionBar().setTitle("Home");
                        break;



                    case R.id.menu_orders:
                        fragment = new OrdersFragment();
                        loadFragment_side(fragment);
                        getSupportActionBar().setTitle("Orders");
                        break;

                    case R.id.menu_profile:
                        fragment = new ProfileFragment();
                        loadFragment_side(fragment);
                        getSupportActionBar().setTitle("Profile");
                        break;

                    case R.id.menu_settings:
                        fragment = new SettingsFragment();
                        loadFragment_side(fragment);
                        getSupportActionBar().setTitle("Settings");
                        break;

                    case R.id.menu_logout:
                        makeMeOffline();
                        break;

                    default:
                        return true;
                }


                return true;
            }
        });


        View headerView = navigationView.getHeaderView(0);
        imageViewProfile = headerView.findViewById(R.id.header_profile);
        usernameHeader = headerView.findViewById(R.id.header_username);
        textViewEmail = headerView.findViewById(R.id.header_email);

        //bottom Navi
        //TextView textView = findViewById(R.id.pageTitle);
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ORDERS, R.drawable.ic_orders));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_ACCOUNT, R.drawable.ic_account_circle_24));

        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                //navigate user

                switch (item.getId()){
                    case ID_HOME:
                        fragment = new HomeFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Home");
                        break;



                    case ID_ORDERS:
                        fragment = new OrdersFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Orders");

                        break;

                    case ID_ACCOUNT:
                        fragment = new ProfileFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Profile");

                        break;

                    default:

                }

            }
        });

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                String name;

                switch (item.getId()){
                    case ID_HOME:
                        name = "Home";
                        break;


                    case ID_ORDERS:
                        name = "Orders";
                        break;

                    case ID_ACCOUNT:
                        name = "Account";
                        break;

                    default:
                        name = "";
                }
                //textView.setText(getString(R.string.page_title, name));
            }
        });

        bottomNavigation.show(ID_HOME, true);
        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
                //Toast.makeText(UserNaviActivity.this, "Already Selected", Toast.LENGTH_SHORT).show();

                switch (item.getId()){
                    case ID_HOME:
                        fragment = new HomeFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Home");
                        break;



                    case ID_ORDERS:
                        fragment = new OrdersFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Orders");

                        break;

                    case ID_ACCOUNT:
                        fragment = new ProfileFragment();
                        loadFragment(fragment);
                        getSupportActionBar().setTitle("Profile");

                        break;

                    default:

                }
            }
        });
    }

    private void loadFragment_side(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();


    }

    private void makeMeOffline() {
        progressDialog.setMessage("Login Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to DB

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //login out success
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to  update
                        progressDialog.dismiss();
                        Toast.makeText(Rider_naviActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(Rider_naviActivity.this, LoginActivity.class));
            finish();

        }else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String img = ""+ds.child("profileImage").getValue();

                            usernameHeader.setText(name);
                            textViewEmail.setText(email);
                            Glide.with(Rider_naviActivity.this).load(img).circleCrop()
                                    .into(imageViewProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}