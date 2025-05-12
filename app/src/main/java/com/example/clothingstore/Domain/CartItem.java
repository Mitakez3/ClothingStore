package com.example.clothingstore.Domain;

public class CartItem {
    private SanPham sanPham;
    private int quantity;
    private String size;
    private boolean isSelected = false;;

    public CartItem() {
    }

    public CartItem(SanPham sanPham, int quantity, String size) {
        this.sanPham = sanPham;
        this.quantity = quantity;
        this.size = size;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public CartItem(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

