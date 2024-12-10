package com.concurrent.finalproject.models;

import androidx.annotation.NonNull;

public class Product {
    private String ID;
    private String name;
    private int stock;
    private double price;

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
        return "{\n" + "ID:" + ID + "\nname: " + name + "\nstock: " + stock + "\nprice: " + price +
                "\n}";
    }
}
