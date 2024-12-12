package com.concurrent.finalproject.models;

import androidx.annotation.NonNull;

public class Product {
    private String product_id;
    private String name;
    private int stock;
    private double price;

    public Product(String product_id, String name, int stock, double price) {
        this.product_id = product_id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public Product(String product_id, int stock) {
        this.product_id = product_id;
        this.stock = stock;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + "ID:" + product_id + ", name: " + name + ", stock: " + stock + ", price: " + price + "}";
    }
}
