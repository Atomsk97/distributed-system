package com.concurrent.finalproject.interfaces;

import com.concurrent.finalproject.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductDAO {
    @GET("almacen")
    Call<List<Product>> getProducts();

}
