package com.example.clothingstore;

public class OrderItem {
    private String productId;
    private String tenSP;
    private String hinh;
    private int gia;
    // thêm các trường khác nếu có: mô tả, thể loại, v.v.
    private int soLuong;
    public OrderItem() {}

    public OrderItem(String productId, int soLuong, String tenSP, String hinh, int gia) {
        this.productId = productId;
        this.soLuong = soLuong;
        this.tenSP = tenSP;
        this.hinh = hinh;
        this.gia = gia;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }



    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }


    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public String getHinh() { return hinh; }
    public void setHinh(String hinh) { this.hinh = hinh; }

    public int getGia() { return gia; }
    public void setGia(int gia) { this.gia = gia; }
}
