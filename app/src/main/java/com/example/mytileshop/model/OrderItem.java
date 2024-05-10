package com.example.mytileshop.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class OrderItem {
    private String orderId;
    private Timestamp date;
    private String status;
    private double totalPrice;
    private List<Map<String, Object>> products;

    public OrderItem() {}

    public OrderItem(String orderId, Timestamp date, String status, double totalPrice, List<Map<String, Object>> products) {
        this.orderId = orderId;
        this.date = date;
        this.status = status;
        this.totalPrice = totalPrice;
        this.products = products;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public List<Map<String, Object>> getProducts() {
        return products;
    }
}
