package com.example.clothingstore;

public class SanPham {
    private String TenSP;
    private double Gia;
    private String Hinh;
    private String MoTa;
    private String TheLoai;
    private int soLuong;

    private String productId;

    public SanPham() {
    }


    public SanPham(String TenSP, double Gia, String Hinh, String MoTa, String TheLoai) {
        this.TenSP = TenSP;
        this.Gia = Gia;
        this.Hinh = Hinh;
        this.MoTa = MoTa;
        this.TheLoai = TheLoai;
        this.soLuong = 1;
    }

    public String getTenSP() {
        return TenSP;
    }
    public void setTenSP(String tenSP) {
        this.TenSP = tenSP;
    }

    public double getGia() {
        return Gia;
    }

    public String getHinh() {
        return Hinh;
    }

    public String getTheLoai() {
        return TheLoai;
    }

    public String getMoTa() {
        return MoTa;
    }

//    public void setGia(double Gia) {
//        this.Gia = Gia;
//    }
//
//    public void setMoTa(String MoTa) {
//        this.MoTa = MoTa;
//    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public int getSoLuong() {
        return soLuong;
    }
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
}
