package com.concurrent.finalproject.interfaces;

import com.concurrent.finalproject.models.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestBodyDAO {

    @POST("/ventas")
    Call<RequestBody> sendData(@Body RequestBody requestBody);
}
