package com.jiat.rider.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jiat.rider.R;
import com.jiat.rider.model.ModelOrder;
import com.jiat.rider.model.ModelOrderDetailsRider;

import java.util.ArrayList;

public class AdapterRiderOderDetails extends RecyclerView.Adapter<AdapterRiderOderDetails.ViewHolder>{


    private FirebaseStorage  storage;
    private Context context;
    private ArrayList<ModelOrderDetailsRider> modelOrderDetailsRiders;

    public AdapterRiderOderDetails(Context context, ArrayList<ModelOrderDetailsRider> modelOrderDetailsRiders) {
        this.context = context;
        this.modelOrderDetailsRiders = modelOrderDetailsRiders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_cart_layout, parent, false);
        return new ViewHolder(view);
    }

    private Double FinalPriceItem;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        storage = FirebaseStorage.getInstance();
        //get Data
        ModelOrderDetailsRider orderDetailsRider = modelOrderDetailsRiders.get(position);
        String getPId = orderDetailsRider.getpId();
        String name = orderDetailsRider.getName();
        String price = orderDetailsRider.getPrice();
        String quantity = orderDetailsRider.getQuantity();
        String img = orderDetailsRider.getImage();

        //set Data
        holder.productTitle.setText(name);
        holder.productPrice.setText("LKR "+price);
        holder.productQty.setText("Qty: "+quantity);


        FinalPriceItem = Double.parseDouble(price) *Double.parseDouble(quantity);
        holder.productFinalPrice.setText("LKR "+FinalPriceItem);

        storage.getReference("Product_Images/"+orderDetailsRider.getImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.productImg.getContext()).load(uri)
                                .into(holder.productImg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(holder.productImg.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public int getItemCount() {
        return modelOrderDetailsRiders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView productImg;
        private TextView productTitle, productQty, productPrice, productFinalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImg = itemView.findViewById(R.id.imgItem);
            productTitle = itemView.findViewById(R.id.titleItem);
            productQty = itemView.findViewById(R.id.Cartqty);
            productPrice = itemView.findViewById(R.id.CartPrice);
            productFinalPrice = itemView.findViewById(R.id.finalPrice);
        }
    }
}
