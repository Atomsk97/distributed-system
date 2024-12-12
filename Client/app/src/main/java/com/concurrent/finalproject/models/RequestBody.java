package com.concurrent.finalproject.models;

import java.util.List;

public class RequestBody {
    private String clientId;
    private List<Detail> details;

    public RequestBody(String clientId, List<Detail> details) {
        this.clientId = clientId;
        this.details = details;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public static class Detail{
        private String product_id;
        private int amount;
        private double price;

        public Detail(String product_id, int amount, double price) {
            this.product_id = product_id;
            this.amount = amount;
            this.price = price;
        }

        public String getProduct_id() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
