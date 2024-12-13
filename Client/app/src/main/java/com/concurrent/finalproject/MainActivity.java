package com.concurrent.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.concurrent.finalproject.interfaces.ProductDAO;
import com.concurrent.finalproject.models.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    EditText editTextIp, editTextPort, editTextSaleIp, editTextSalePort;
    Button buttonConnect;

    Dialog waitDialog = null;

    public static List<Product> products = new ArrayList<>();

    public static Retrofit retrofit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIp = findViewById(R.id.editText_ip);
        editTextPort = findViewById(R.id.editText_port);
        editTextSaleIp = findViewById(R.id.editText_sale_ip);
        editTextSalePort = findViewById(R.id.editText_sale_port);
        buttonConnect = findViewById(R.id.button_connect);

        buttonConnect.setOnClickListener(view -> {
            String host = editTextIp.getText().toString().trim();
            String port = editTextPort.getText().toString().trim();
            String baseUrl = "http://" + host + ":" + port + "/";

            setRetrofit(baseUrl);

            if (waitDialog != null) {
                waitDialog.show();
            } else {
                waitDialog = makeWaitDialog(this);
                waitDialog.show();
            }

            ProductDAO productDAO = retrofit.create(ProductDAO.class);

            Call<List<Product>> call = productDAO.getProducts();
            call.enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                    if (response.isSuccessful()) {
                        products = response.body();
                        if (products != null) {
                            int tam = products.size();
                            for (int i = 0; i < tam; i++) {
                                System.out.println(products.get(i));
                            }
                        }
                        dismissWaitDialog();
                        Intent intent = new Intent(MainActivity.this, OperationsActivity.class);
                        intent.putExtra("saleIP", editTextSaleIp.getText().toString());
                        intent.putExtra("salePORT", editTextSalePort.getText().toString());
                        startActivity(intent);
                    } else {
                        System.out.println("FAILED");
                        dismissWaitDialog();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                    System.out.println("onFailure: " + t.getMessage());
                    dismissWaitDialog();
                }
            });
        });
    }

    public static Dialog makeWaitDialog(Context context) {
        Dialog waitDialog = new Dialog(context);
        waitDialog.setCancelable(false);
        waitDialog.setContentView(R.layout.custom_waiting_dialog);

        if (waitDialog.getWindow() != null) {
            waitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            waitDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return waitDialog;
    }

    public static void setRetrofit(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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