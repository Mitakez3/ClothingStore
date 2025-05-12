package com.example.clothingstore.Domain;

import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private int totalAmount;
    private List<OrderItem> orderItems;
    private String status;
    private long timestamp;
    private String address;
    private String phone;
    private String paymentMethod;

    public Order() {
    }

    public Order(String orderId, String customerId, int totalAmount, List<OrderItem> orderItems, String status, String paymentMethod, String address, String phone) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.phone = phone;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
