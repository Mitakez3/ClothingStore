package com.example.clothingstore.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.SanPhamAdapter;
import com.example.clothingstore.Domain.GridSpacingItemDecoration;
import com.example.clothingstore.Domain.SanPham;
import com.example.clothingstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SanPhamAdapter sanPhamAdapter;
    private List<SanPham> sanPhamList;

    private DatabaseReference sanPhamRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerView = findViewById(R.id.rvSanPham);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        sanPhamList = new ArrayList<>();
        sanPhamAdapter = new SanPhamAdapter(this, sanPhamList, true);

        recyclerView.setAdapter(sanPhamAdapter);

        sanPhamRef = FirebaseDatabase.getInstance().getReference("SanPham");

        loadSanPhamData();

        sanPhamAdapter.setOnItemClickListener(productId -> loadSanPhamDetailAndShowDialog(productId));

        findViewById(R.id.btnAddProduct).setOnClickListener(v -> showAddProductDialog());
    }

    private void loadSanPhamData() {
        sanPhamRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sanPhamList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    SanPham sp = snap.getValue(SanPham.class);
                    if (sp != null) {
                        sp.setProductId(snap.getKey());
                        sanPhamList.add(sp);
                    }
                }
                sanPhamAdapter.updateList(sanPhamList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InventoryActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSanPhamDetailAndShowDialog(String productId) {
        sanPhamRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SanPham sp = snapshot.getValue(SanPham.class);
                if (sp == null) {
                    Toast.makeText(InventoryActivity.this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.setProductId(snapshot.getKey());
                showEditProductDialog(sp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InventoryActivity.this, "Lỗi tải chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm sản phẩm mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);

        EditText edtMaSP = dialogView.findViewById(R.id.edtMaSP);
        EditText edtTenSP = dialogView.findViewById(R.id.edtTenSP);
        EditText edtGia = dialogView.findViewById(R.id.edtGia);
        EditText edtHinh = dialogView.findViewById(R.id.edtHinh);
        EditText edtMoTa = dialogView.findViewById(R.id.edtMoTa);
        Spinner spinnerTheLoai = dialogView.findViewById(R.id.spinnerTheLoai);
        EditText edtSoLuong = dialogView.findViewById(R.id.edtSoLuong);

        // Thiết lập spinner thể loại
        String[] danhSachTheLoai = {"Áo Khoác", "Áo Thun", "Sơ Mi", "Quần Dài", "Quần Short", "Phụ Kiện"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, danhSachTheLoai);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheLoai.setAdapter(adapterSpinner);

        // Disable edit mã sản phẩm
        edtMaSP.setEnabled(false);
        edtMaSP.setFocusable(false);

        builder.setView(dialogView);

        // Trước khi show dialog, lấy mã sản phẩm mới dựa trên mã lớn nhất hiện có
        sanPhamRef.get().addOnSuccessListener(snapshot -> {
            int maxNumber = 0;
            for (DataSnapshot child : snapshot.getChildren()) {
                String key = child.getKey(); // vd: sp1, sp2, sp3
                if (key != null && key.startsWith("sp")) {
                    try {
                        int num = Integer.parseInt(key.substring(2));
                        if (num > maxNumber) maxNumber = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            int newNumber = maxNumber + 1;
            String newProductId = "sp" + newNumber;
            edtMaSP.setText(newProductId);
        });

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String productId = edtMaSP.getText().toString().trim();
            String tenSP = edtTenSP.getText().toString().trim();
            String giaStr = edtGia.getText().toString().trim();
            String hinh = edtHinh.getText().toString().trim();
            String moTa = edtMoTa.getText().toString().trim();
            String theLoai = spinnerTheLoai.getSelectedItem().toString();
            String soLuongStr = edtSoLuong.getText().toString().trim();

            if (productId.isEmpty() || tenSP.isEmpty() || giaStr.isEmpty() || soLuongStr.isEmpty()) {
                Toast.makeText(InventoryActivity.this, "Mã sản phẩm, Tên, Giá và Số lượng không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            double gia;
            int soLuong;
            try {
                gia = Double.parseDouble(giaStr);
                soLuong = Integer.parseInt(soLuongStr);
            } catch (NumberFormatException e) {
                Toast.makeText(InventoryActivity.this, "Giá hoặc Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> newProductData = new HashMap<>();
            newProductData.put("TenSP", tenSP);
            newProductData.put("Gia", gia);
            newProductData.put("Hinh", hinh);
            newProductData.put("MoTa", moTa);
            newProductData.put("SoLuong", soLuong);
            newProductData.put("TheLoai", theLoai);

            // Lưu sản phẩm mới với key là productId tự tạo (spN)
            sanPhamRef.child(productId).setValue(newProductData)
                    .addOnSuccessListener(unused -> Toast.makeText(InventoryActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(InventoryActivity.this, "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void showEditProductDialog(SanPham sp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa sản phẩm");

        // Inflate layout dialog_edit_product.xml với EditTexts và Spinner theo yêu cầu
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);
        EditText edtMaSP = dialogView.findViewById(R.id.edtMaSP);
        EditText edtTenSP = dialogView.findViewById(R.id.edtTenSP);
        EditText edtGia = dialogView.findViewById(R.id.edtGia);
        EditText edtHinh = dialogView.findViewById(R.id.edtHinh);
        EditText edtMoTa = dialogView.findViewById(R.id.edtMoTa);
        Spinner spinnerTheLoai = dialogView.findViewById(R.id.spinnerTheLoai);
        EditText edtSoLuong = dialogView.findViewById(R.id.edtSoLuong);

        // Gán dữ liệu hiện tại vào các trường
        edtMaSP.setText(sp.getProductId());
        edtTenSP.setText(sp.getTenSP());
        edtGia.setText(String.valueOf(sp.getGia()));
        edtHinh.setText(sp.getHinh());
        edtMoTa.setText(sp.getMoTa());
        edtSoLuong.setText(String.valueOf(sp.getSoLuong()));

        // Setup spinner thể loại với 6 loại
        String[] danhSachTheLoai = {"Áo Khoác", "Áo Thun", "Sơ Mi", "Quần Dài", "Quần Short", "Phụ Kiện"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, danhSachTheLoai);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheLoai.setAdapter(adapterSpinner);

        // Chọn giá trị spinner hiện tại
        int selectedIndex = 0;
        for (int i = 0; i < danhSachTheLoai.length; i++) {
            if (danhSachTheLoai[i].equals(sp.getTheLoai())) {
                selectedIndex = i;
                break;
            }
        }
        spinnerTheLoai.setSelection(selectedIndex);

        builder.setView(dialogView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            // Lấy dữ liệu từ input
            String tenSP = edtTenSP.getText().toString().trim();
            String giaStr = edtGia.getText().toString().trim();
            String hinh = edtHinh.getText().toString().trim();
            String moTa = edtMoTa.getText().toString().trim();
            String theLoai = spinnerTheLoai.getSelectedItem().toString();
            String soLuongStr = edtSoLuong.getText().toString().trim();

            if (tenSP.isEmpty() || giaStr.isEmpty() || soLuongStr.isEmpty()) {
                Toast.makeText(InventoryActivity.this, "Tên, Giá và Số lượng không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            double gia;
            int SoLuong;
            try {
                gia = Double.parseDouble(giaStr);
                SoLuong = Integer.parseInt(soLuongStr);
            } catch (NumberFormatException e) {
                Toast.makeText(InventoryActivity.this, "Giá hoặc Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("TenSP", tenSP);
            updatedData.put("Gia", gia);
            updatedData.put("Hinh", hinh);
            updatedData.put("MoTa", moTa);
            updatedData.put("SoLuong", SoLuong);
            updatedData.put("TheLoai", theLoai);

            sanPhamRef.child(sp.getProductId()).updateChildren(updatedData)
                    .addOnSuccessListener(unused -> Toast.makeText(InventoryActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(InventoryActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
