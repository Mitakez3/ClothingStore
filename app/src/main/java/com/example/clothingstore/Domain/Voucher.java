package com.example.clothingstore.Domain;

import java.util.Map;

public class Voucher {
    private String title;
    private int discountPercent;
    private String expireDays;
    private String voucherId;
    private Map<String, String> status;

    public Voucher() {}

    public Voucher(String title, int discountPercent, String expireDays) {
        this.title = title;
        this.discountPercent = discountPercent;
        this.expireDays = expireDays;
    }
    public String getTitle() { return title; }
    public double getDiscountPercent() { return discountPercent; }
    public String getExpireDays() { return expireDays; }
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public Map<String, String> getStatus() {
        return status;
    }
    public void setStatus(Map<String, String> status) {
        this.status = status;
    }
}
