package com.concurrent.finalproject;

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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    public static String host = "192.168.18.107";
    public static int port = 5672;

    EditText editTextIp, editTextPort;
    Button buttonConnect;

    Dialog waitDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIp = findViewById(R.id.editText_ip);
        editTextPort = findViewById(R.id.editText_port);
        buttonConnect = findViewById(R.id.button_connect);

        buttonConnect.setOnClickListener(view -> {
            host = editTextIp.getText().toString().trim();
            port = Integer.parseInt(editTextPort.getText().toString().trim());

            if(waitDialog != null){
                waitDialog.show();
            }else{
                waitDialog = makeWaitDialog(this);
                waitDialog.show();
            }

            JSONObject messageJson = new JSONObject();
            try {
                messageJson.put("request", "INFO");
            } catch (JSONException e) {
                System.out.println("ERROR ON JSON creation:\n" + e.getMessage());
            }

            new Thread(() -> {
                try {
                    init();
                    try {
                        String data = call(messageJson.toString(), requestProductQueueName);
                        if(data.equals("")){
                            System.out.println("NO DATA WAS RETRIEVED");
                            return;
                        }
                        //System.out.println(" [.] Got: '" + data + "'");
                        Gson gson = new Gson();
                        ProductResponse productResponse = gson.fromJson(data, ProductResponse.class);
                        dismissWaitDialog();
                        if("SUCCESS".equals(productResponse.getMessage())){
                            Intent intent = new Intent(MainActivity.this, OperationsActivity.class);
                            intent.putExtra("products", productResponse.getResult());
                            startActivity(intent);
                        }
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        System.out.println("ERROR ON CALL:\n" + e.getMessage());
                    }
                } catch (IOException | TimeoutException e) {
                    dismissWaitDialog();
                    System.out.println("ERROR ON INIT:\n" + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to connect to " + host, Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    private void init() throws IOException, TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static String call(String message, String requestQueueName) throws IOException, InterruptedException, ExecutionException {
        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes(StandardCharsets.UTF_8));
        System.out.println("Sending " + message + " through " + requestQueueName + " queue. CorrId: " + corrId);

        final CompletableFuture<String> response = new CompletableFuture<>();

        String cTag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
           if(delivery.getProperties().getCorrelationId().equals(corrId)){
               response.complete(new String(delivery.getBody(), StandardCharsets.UTF_8));
           }
        }, consumerTag -> {});


        String result;
        try {
            result = response.get(10, TimeUnit.SECONDS);
        }catch (TimeoutException e){
            channel.basicCancel(cTag);
            System.out.println("Timeout reached, no response received. Closing connection.");
            result="";
        }finally {
            channel.basicCancel(cTag);
        }
        return result;
    }

    public void closeConnection(){
        new Thread(() -> {
            try{
                if(channel!= null && channel.isOpen()){
                    channel.close();
                }
                if(connection != null &&  connection.isOpen()){
                    connection.close();
                }
            }catch (IOException | TimeoutException e){
                System.out.println("Error in CLOSE:\n" + e.getMessage());
            }
        }).start();
    }

    public static Dialog makeWaitDialog(Context context){
        Dialog waitDialog = new Dialog(context);
        waitDialog.setCancelable(false);
        waitDialog.setContentView(R.layout.custom_waiting_dialog);

        if(waitDialog.getWindow() != null){
            waitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            waitDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return waitDialog;
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