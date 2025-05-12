package com.example.clothingstore.Domain;

public class CartItem {
    private SanPham sanPham;
    private int quantity;

    public CartItem() {
    }

    public CartItem(SanPham sanPham, int quantity) {
        this.sanPham = sanPham;
        this.quantity = quantity;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

