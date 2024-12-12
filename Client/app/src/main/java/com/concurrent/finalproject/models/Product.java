package com.concurrent.finalproject.models;

import androidx.annotation.NonNull;

public class Product {
    private String ID;
    private String name;
    private int stock;
    private double price;

    public Product(String ID, String name, int stock, double price) {
        this.ID = ID;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public Product(String ID, int stock) {
        this.ID = ID;
        this.stock = stock;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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
        return "{" + "ID:" + ID + ", name: " + name + ", stock: " + stock + ", price: " + price + "}";
    }
}
