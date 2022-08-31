package com.jiat.rider.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jiat.rider.R;
import com.jiat.rider.adapter.AdapterOrder;
import com.jiat.rider.model.ModelOrder;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private SwitchCompat activeSwitch;
    private FirebaseAuth firebaseAuth;
    private TextView AllOrders;
    private RecyclerView orderRecycler;
    public ArrayList<ModelOrder> orderArrayList;
    private AdapterOrder adapterOrder;
    private String shopUID;
    private CardView cardViewOrders;




    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activeSwitch = view.findViewById(R.id.switchStatus);
        firebaseAuth = FirebaseAuth.getInstance();
        shopUID = "d6RHVgGQoNZMkciEMVl16lSSsIw2";

        AllOrders = view.findViewById(R.id.allOrderTv);
        orderRecycler = view.findViewById(R.id.order_recycler);
        cardViewOrders = view.findViewById(R.id.cardViewOrders);



        loadStatus();
       loadOrderDetails();
        loadNewOrders();



       /* activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    setActive();

                }else{
                    setInactive();

                }
            }
        });*/

        activeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeSwitch.isChecked()){
                    setActive();
                }else{
                    setInactive();
                }

            }
        });

        cardViewOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment =  new OrdersFragment();
                FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.commit();
            }
        });


        return view;
    }

    private void loadNewOrders() {
        orderArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();


                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("rider").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for(DataSnapshot ds: snapshot.getChildren()) {
                                            ModelOrder modelOrder = ds.getValue(ModelOrder.class);

                                            //add to list
                                            orderArrayList.add(modelOrder);

                                        }

                                    }
                                    //setup Adapter
                                    adapterOrder = new AdapterOrder(getActivity(), orderArrayList);
                                    adapterOrder.getFilter().filter("Ready to Deliver");




                                    //set Recycler
                                    orderRecycler.setAdapter(adapterOrder);


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("rider").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                       String count = ""+ snapshot.getChildrenCount();
                                       AllOrders.setText(count);



                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void setInactive() {

        if (getActivity() == null) {
            return;
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "" + "false");

        //update to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated
                        Toast.makeText(getActivity(), "You are Change to Inactive Mode", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setActive() {

        if (getActivity() == null) {
            return;
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "" + "true");

        //update to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated
                        Toast.makeText(getActivity(), "You are Change to Active Mode", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void loadStatus() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String online = ""+ds.child("online").getValue();
                    if(online.equals("true")){
                        activeSwitch.setChecked(true);
                    }else{
                        activeSwitch.setChecked(false);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}