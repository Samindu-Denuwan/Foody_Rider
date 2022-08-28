package com.jiat.rider.filter;

import android.annotation.SuppressLint;
import android.widget.Filter;
import com.jiat.rider.adapter.AdapterOrder;
import com.jiat.rider.model.ModelOrder;

import java.util.ArrayList;

public class FilterOrder extends Filter {

    private AdapterOrder adapterOrder;
    private ArrayList<ModelOrder> filterList;

    public FilterOrder(AdapterOrder adapterOrder, ArrayList<ModelOrder> filterList) {
        this.adapterOrder = adapterOrder;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search
        if(constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelOrder> filterModel = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //check by title
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filtered data to list
                filterModel.add(filterList.get(i));
                }

            }
            results.count = filterModel.size();
            results.values = filterModel;

        }else{
            //search is empty
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterOrder.orderArrayList = (ArrayList<ModelOrder>) results.values;
        adapterOrder.notifyDataSetChanged();

    }
}
