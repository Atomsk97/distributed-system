package com.concurrent.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.concurrent.finalproject.interfaces.ProductDAO;
import com.concurrent.finalproject.interfaces.RequestBodyDAO;
import com.concurrent.finalproject.models.Product;
import com.concurrent.finalproject.models.RequestBody;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OperationsActivity extends AppCompatActivity {

    private final List<Product> listOfProductsToBuy = new ArrayList<>();
    List<Product> productList;

    private EditText editTextProductId, editTextAmount;

    private String userId;

    //private double totalAmount = 0;

    private Dialog waitDialog = null;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations);

        editTextProductId = findViewById(R.id.editText_product_id);
        editTextAmount = findViewById(R.id.editText_amount);
        ImageView imageViewRefresh = findViewById(R.id.imageViewRefresh);
        Button buttonAddProduct = findViewById(R.id.button_add_product);
        Button buttonSubmit = findViewById(R.id.button_submit);

        userId = String.valueOf(Math.round(Math.random()*1000));

        recyclerView = findViewById(R.id.recyclerView_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = MainActivity.products;

        ProductAdapter productAdapter = new ProductAdapter(productList, editTextProductId, editTextAmount);
        recyclerView.setAdapter(productAdapter);

        waitDialog = MainActivity.makeWaitDialog(this);

        imageViewRefresh.setOnClickListener(view -> {
            updateRecyclerView();
        });

        buttonAddProduct.setOnClickListener(view -> {
            int productId;
            try {
                productId = Integer.parseInt(editTextProductId.getText().toString());
            } catch (Exception e) {
                productId = 0;
            }
            int productAmount;
            try {
                productAmount = Integer.parseInt(editTextAmount.getText().toString());
            } catch (Exception e) {
                productAmount = 0;
            }

            if (productAmount <= 0) {
                showToast("Can not add an empty amount");
                return;
            }

            //totalAmount += getPrice(productId, productAmount);
            listOfProductsToBuy.add(new Product(String.valueOf(productId), productAmount));
            editTextProductId.setText("");
            editTextAmount.setText("");
            editTextAmount.setEnabled(false);
            showToast("Product added in the cart");
        });

        buttonSubmit.setOnClickListener(view -> {
            if (listOfProductsToBuy.size() < 1) {
                showToast("Empty list");
                return;
            }

            showWaitDialog();
            List<RequestBody.Detail> details = new ArrayList<>();
            listOfProductsToBuy.forEach(item -> {
                details.add(new RequestBody.Detail(item.getID(), item.getStock(), item.getPrice()));
            });

            RequestBody requestBody = new RequestBody(userId, details);

            Retrofit retrofitToSales = new Retrofit.Builder()
                    .baseUrl("http: /")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestBodyDAO dao = retrofitToSales.create(RequestBodyDAO.class);

            Call<RequestBody> call = dao.sendData(requestBody);

            call.enqueue(new Callback<RequestBody>() {
                @Override
                public void onResponse(Call<RequestBody> call, Response<RequestBody> response) {
                    if (response.isSuccessful()) {
                        System.out.println("YES");
                    } else {
                        System.out.println("OH NOUS");
                    }
                    updateRecyclerView();
                    listOfProductsToBuy.clear();
                    //totalAmount = 0;
                }

                @Override
                public void onFailure(Call<RequestBody> call, Throwable t) {
                    System.out.println("onFailure: " + t.getMessage());
                    dismissWaitDialog();
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //private double getPrice(int productId, int productAmount) {
        //return productList.get(productId).getPrice() * productAmount;
    //}

    private void updateRecyclerView() {
        ProductDAO productDAO = MainActivity.retrofit.create(ProductDAO.class);
        Call<List<Product>> call = productDAO.getProducts();
        showWaitDialog();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful()) {
                    productList = response.body();
                    if (productList != null) {
                        runOnUiThread(() -> recyclerView.setAdapter(new ProductAdapter(productList, editTextProductId, editTextAmount)));
                        System.out.println("LIST UPDATED!");
                    } else {
                        System.out.println("LIST IS EMPTY!");
                    }
                    dismissWaitDialog();
                } else {
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


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showWaitDialog() {
        if (waitDialog != null) {
            waitDialog.show();
        } else {
            waitDialog = MainActivity.makeWaitDialog(this);
            waitDialog.show();
        }
    }

    private void dismissWaitDialog() {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
    }
}