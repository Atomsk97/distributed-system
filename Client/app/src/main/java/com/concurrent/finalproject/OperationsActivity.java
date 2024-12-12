package com.concurrent.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.concurrent.finalproject.interfaces.ProductDAO;
import com.concurrent.finalproject.models.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperationsActivity extends AppCompatActivity {

    private final List<Product> listOfProductsToBuy = new ArrayList<>();
    List<Product> productList;

    private Button buttonConfirmUser;

    private EditText editTextUserId, editTextProductId, editTextAmount;

    private String userId;

    private boolean userIsSet;

    private double totalAmount = 0;

    private Dialog waitDialog = null;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations);

        editTextUserId = findViewById(R.id.editText_user_id);
        editTextProductId = findViewById(R.id.editText_product_id);
        editTextAmount = findViewById(R.id.editText_amount);
        Button buttonAddProduct = findViewById(R.id.button_add_product);
        Button buttonSubmit = findViewById(R.id.button_submit);
        buttonConfirmUser = findViewById(R.id.button_confirm_user);

        recyclerView = findViewById(R.id.recyclerView_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = MainActivity.products;

        ProductAdapter productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        waitDialog = MainActivity.makeWaitDialog(this);

        buttonConfirmUser.setOnClickListener(view -> {
            if(editTextUserId.getText().toString().equals("")){
                showToast("UserID cannot be empty");
                return;
            }
            if (!userIsSet){
                cleanEditText(false);
                showToast("UserID set to " + userId);
            }else{
                cleanEditText(true);
                showToast("UserID unsetted.\nChart cleared.");
            }
        });

        buttonAddProduct.setOnClickListener(view -> {
            int productId;
            try {
                productId = Integer.parseInt(editTextProductId.getText().toString());
            }catch (Exception e){
                productId = 0;
            }
            int productAmount;
            try {
                productAmount = Integer.parseInt(editTextAmount.getText().toString());
            }catch (Exception e){
                productAmount = 0;
            }

            /*
            if(!isIdCorrect(productId)){
                showToast("ID not found in list");
                return;
            }
             */

            if(productAmount <= 0){
                showToast("Can not add an empty amount");
                return;
            }

            //totalAmount += getPrice(productId)*productAmount;
            totalAmount += 1;
            listOfProductsToBuy.add(new Product(String.valueOf(productId), productAmount));
            editTextProductId.setText("");
            editTextAmount.setText("");
            showToast("Product added in the cart");
        });

        buttonSubmit.setOnClickListener(view -> {
            if(!userIsSet){
                showToast("Confirm the UserId with the button on the right side");
                return;
            }

            if(listOfProductsToBuy.size() < 1){
                showToast("Empty list");
                return;
            }

            if(waitDialog != null){
                waitDialog.show();
            }else{
                waitDialog = MainActivity.makeWaitDialog(this);
                waitDialog.show();
            }

           //TODO: Submit action with Retrofit
            updateRecyclerView();
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void cleanEditText(boolean hasToClean){
        if(hasToClean){
            userId = "";
            userIsSet = false;
            buttonConfirmUser.setTextColor(Color.rgb(63,216, 63));//"#3FD83F"
            editTextUserId.setText("");
            editTextProductId.setText("");
            editTextAmount.setText("");
            buttonConfirmUser.setText("✓");
            editTextUserId.setEnabled(true);
            listOfProductsToBuy.clear();
            totalAmount = 0;
        }else{
            userId = editTextUserId.getText().toString();
            userIsSet = true;
            buttonConfirmUser.setTextColor(Color.rgb(223,23, 23));//"#DF1717"
            buttonConfirmUser.setText("X");
            editTextUserId.setEnabled(false);
        }
    }

    private void updateRecyclerView(){
        ProductDAO productDAO = MainActivity.retrofit.create(ProductDAO.class);
        Call<List<Product>> call = productDAO.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if(response.isSuccessful()){
                    productList = response.body();
                    if(productList != null){
                        runOnUiThread(() -> recyclerView.setAdapter(new ProductAdapter(productList)));
                        System.out.println("LIST UPDATED!");
                    }else{
                        System.out.println("LIST IS EMPTY!");
                    }
                    dismissWaitDialog();
                }else{
                    System.out.println("FAILED IN RESPONSE");
                    dismissWaitDialog();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                System.out.println("onFailure in Update RecyclerView:\n" + t.getMessage());
                dismissWaitDialog();
            }
        });
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void dismissWaitDialog(){
        if(waitDialog != null){
            waitDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(waitDialog != null){
            waitDialog.dismiss();
            waitDialog = null;
        }
    }
}