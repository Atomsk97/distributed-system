package com.concurrent.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.concurrent.finalproject.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ItemViewHolder> {

    private final List<Product> productList;

    public ProductAdapter(List<Product> products){
        this.productList = products;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewId.setText(String.valueOf(product.getID()));
        holder.textViewName.setText(product.getName());
        holder.textViewPrice.setText(String.valueOf(product.getPrice()));
        holder.textViewStock.setText(String.valueOf(product.getStock()));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textViewId;
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewStock;
        public ItemViewHolder(@NonNull View productView){
            super(productView);
            textViewId = productView.findViewById(R.id.textView_product_id);
            textViewName = productView.findViewById(R.id.textView_product_name);
            textViewPrice = productView.findViewById(R.id.textView_product_price);
            textViewStock = productView.findViewById(R.id.textView_product_stock);
        }
    }
}
