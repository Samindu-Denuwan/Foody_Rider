package com.jiat.rider.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jiat.rider.OrdersRiderDetailsActivity;
import com.jiat.rider.R;
import com.jiat.rider.filter.FilterOrder;
import com.jiat.rider.model.ModelOrder;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.ViewHolder>implements Filterable {

    private Context  context;
    public ArrayList<ModelOrder> orderArrayList, filterList;
    private FilterOrder filter;

    public AdapterOrder(Context context, ArrayList<ModelOrder> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        this.filterList = orderArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //get data
        ModelOrder modelOrder = orderArrayList.get(position);
        String orderID = modelOrder.getOrderId();
        String orderBY = modelOrder.getOrderBy();
        String orderCOST = modelOrder.getOrderCost();
        String orderSTATUS = modelOrder.getOrderStatus();
        String orderTIME = modelOrder.getOrderTime();
        String orderTO = modelOrder.getOrderTo();
        String rider = modelOrder.getRider();

        //set Data

        //change order status text color


            holder.orderAmount.setText("Amount : LKR " + orderCOST);
            holder.orderId.setText("Order Id: " + orderID);
            holder.orderStatus.setText(orderSTATUS);

        if(orderSTATUS.equals("Ready to Deliver")){
            holder.orderStatus.setTextColor(context.getResources().getColor(R.color.Blue));
        }else if(orderSTATUS.equals("Rider Cancelled")){
            holder.orderStatus.setTextColor(context.getResources().getColor(R.color.red));
        }else{
            holder.orderStatus.setTextColor(context.getResources().getColor(R.color.orange));
        }


            //convert timestamp
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(orderTIME));
            String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

            holder.orderDate.setText(formatedDate);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OrdersRiderDetailsActivity.class);
                    intent.putExtra("orderTo", orderTO);
                    intent.putExtra("orderId", orderID);
                    intent.putExtra("orderBy", orderBY);
                    context.startActivity(intent);
                }
            });
            loadCustomerInfo(modelOrder, holder);

        }




    private void loadCustomerInfo(ModelOrder modelOrder, final ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrder.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String custName = ""+snapshot.child("name").getValue();
                        holder.orderName.setText(custName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return orderArrayList.size();

    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterOrder(this,filterList);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView orderId, orderDate, orderName, orderAmount, orderStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderId = itemView.findViewById(R.id.orderTv);
            orderDate = itemView.findViewById(R.id.dateTv);
            orderName = itemView.findViewById(R.id.customerTv);
            orderAmount = itemView.findViewById(R.id.amountTv);
            orderStatus = itemView.findViewById(R.id.statusTv);

        }
    }
}
