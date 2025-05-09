package com.example.clothingstore.Domain;

public class Voucher {
    private String title;
    private double discountPercent;
    private String expireDays;

    public Voucher() {}

    public Voucher(String title, double discountPercent, String expireDays) {
        this.title = title;
        this.discountPercent = discountPercent;
        this.expireDays = expireDays;
    }

    public String getTitle() { return title; }
    public double getDiscountPercent() { return discountPercent; }
    public String getExpireDays() { return expireDays; }
}
