package com.concurrent.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.concurrent.finalproject.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ItemViewHolder> {

    private final List<Product> productList;

    EditText editTextProductId, editTextAmount;

    public ProductAdapter(List<Product> products, EditText editTextProductId, EditText editTextAmount){
        this.productList = products;
        this.editTextProductId = editTextProductId;
        this.editTextAmount = editTextAmount;
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
        holder.textViewName.setText(product.getName());
        holder.textViewPrice.setText(String.valueOf(product.getPrice()));
        holder.textViewStock.setText(String.valueOf(product.getStock()));
        holder.imageViewShoppingCart.setOnClickListener(view -> {
            editTextProductId.setText(product.getID());
            editTextAmount.setEnabled(true);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewStock;
        ImageView imageViewShoppingCart;
        public ItemViewHolder(@NonNull View productView){
            super(productView);
            textViewName = productView.findViewById(R.id.textView_product_name);
            textViewPrice = productView.findViewById(R.id.textView_product_price);
            textViewStock = productView.findViewById(R.id.textView_product_stock);
            imageViewShoppingCart = productView.findViewById(R.id.imageViewShoppingCart);
        }
    }
}
