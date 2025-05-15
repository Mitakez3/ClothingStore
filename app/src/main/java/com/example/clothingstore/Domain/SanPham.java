package com.example.clothingstore.Domain;

public class SanPham {
    private String TenSP;
    private double Gia;
    private String Hinh;
    private String MoTa;
    private String TheLoai;
    private int SoLuong;
    private String size;
    private boolean isSelected;
    private String productId;
    private int soldCount;
    private boolean isHotCategory = false;

    public SanPham() {
    }

    public SanPham(String TenSP, double Gia, String Hinh, String MoTa, String TheLoai) {
        this.TenSP = TenSP;
        this.Gia = Gia;
        this.Hinh = Hinh;
        this.MoTa = MoTa;
        this.TheLoai = TheLoai;
        this.SoLuong = 1;
    }
    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
    public String getTenSP() {
        return TenSP;
    }
    public void setTenSP(String tenSP) {
        this.TenSP = TenSP;
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
        return SoLuong;
    }
    public void setSoLuong(int SoLuong) {
        this.SoLuong = SoLuong;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public int getSoldCount() {
        return soldCount;
    }
    public boolean isHotCategory() {
        return isHotCategory;
    }

    public void setHotCategory(boolean hotCategory) {
        isHotCategory = hotCategory;
    }

}
