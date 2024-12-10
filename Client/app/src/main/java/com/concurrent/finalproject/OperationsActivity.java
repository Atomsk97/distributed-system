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

import com.concurrent.finalproject.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OperationsActivity extends AppCompatActivity {

    private final List<Product> listOfProductsToBuy = new ArrayList<>();

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

        List<Product> productList = MainActivity.products;

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
            return;
            /* TODO: IMPLEMENT ADD PRODUCT LOGIC
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

            if(!isIdCorrect(productId)){
                showToast("ID not found in list");
                return;
            }
            if(productAmount <= 0){
                showToast("Can not add an empty amount");
                return;
            }

            //listOfProductsToBuy.add(new Product(productId, "", 0.0, productAmount));
            totalAmount += getPrice(productId)*productAmount;
            showToast("Product added in the cart");
             */
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

            JSONObject user = new JSONObject();
            JSONArray productsToBuy = new JSONArray();
            JSONObject message = new JSONObject();
            try{
                user.put("request", "BUY");
                user.put("idClient", userId);
                user.put("amount", totalAmount);

                for(Product p : listOfProductsToBuy){
                    try {
                        JSONObject product = new JSONObject();
                        product.put("id", p.getID());
                        product.put("amount", p.getStock());
                        productsToBuy.put(product);
                    }catch (JSONException e){
                        System.out.println("Error in JSON creation for multiple items:\n" + e.getMessage());
                        return;
                    }
                }
                try {
                    message.put("request", "BUY");
                    message.put("products", productsToBuy);
                }catch (JSONException e){
                    System.out.println("Error in JSON message:\n" + e.getMessage());
                }
            }catch (JSONException e){
                System.out.println("Error in JSON creations for the user:\n" + e.getMessage());
            }

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

    /*
    private void updateRecyclerView(){
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("request", "INFO");
            new Thread(() -> {
                try {
                    String data = MainActivity.call(messageJson.toString(), MainActivity.requestProductQueueName);
                    if(data.equals("")){
                        System.out.println("NO DATA WAS RETRIEVED");
                        return;
                    }

                    Gson gson = new Gson();
                    ProductResponse response = gson.fromJson(data, ProductResponse.class);
                    products = response.getResult();
                    List<Product> productList = Arrays.asList(products);
                    runOnUiThread(() -> recyclerView.setAdapter(new ProductAdapter(productList)));
                }catch (IOException | InterruptedException | ExecutionException e) {
                    System.out.println("ERROR ON CALL:\n" + e.getMessage());
                }
            }).start();
        } catch (JSONException e) {
            System.out.println("ERROR ON JSON creation:\n" + e.getMessage());
        }
    }

     */

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