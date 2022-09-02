package com.jiat.rider.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class OrdersFragment extends Fragment {


    private TextView FilterTv;
    private ImageButton BtnFilter;
    private RecyclerView orderSellerRecycler;
    public ArrayList<ModelOrder>orderArrayList;
    private AdapterOrder adapterOrder;
    private FirebaseAuth  firebaseAuth;
    private String shopUID;


    public OrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Orders");
        shopUID = "d6RHVgGQoNZMkciEMVl16lSSsIw2";
        FilterTv = view.findViewById(R.id.filterOrderTv);
        BtnFilter = view.findViewById(R.id.filterBtn);
        orderSellerRecycler = view.findViewById(R.id.order_recycler);

        firebaseAuth = FirebaseAuth.getInstance();
        loadRiderOrders();


        BtnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"All","Ready to Deliver", "On the way", "Delivered", "Rider Cancelled"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Filter Orders: ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                if (which ==0){
                                    //All Click
                                    FilterTv.setText("Showing All Orders");
                                    adapterOrder.getFilter().filter("");
                                }else{
                                    String optionClicked = options[which];
                                    FilterTv.setText("Showing "+optionClicked+" Orders");
                                    adapterOrder.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();
            }
        });
        return view;

    }

    private void loadRiderOrders() {
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
                                    //set Recycler
                                    orderSellerRecycler.setAdapter(adapterOrder);

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
}