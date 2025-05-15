package com.example.clothingstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.CategoryAdapter;
import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.Domain.GridSpacingItemDecoration;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;
import com.example.clothingstore.Adapter.SanPhamAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ItemProductActivity extends AppCompatActivity {
    private static final String TAG = "ItemProductActivity";
    private RecyclerView recyclerView, recyclerViewCategories;
    private SanPhamAdapter sanPhamAdapter;
    private EditText searchBar;
    private List<SanPham> sanPhamList = new ArrayList<>();

    private ImageView btnCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_product);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        recyclerViewCategories = findViewById(R.id.recyclerViewCategory);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        loadCategories();

        searchBar = findViewById(R.id.searchBar);
        btnCart = findViewById(R.id.btnCart);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ItemProductActivity.this, CartActivity.class);
            startActivity(intent);
        });

        loadSanPham();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSanPham(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCategories() {
        DatabaseReference sanPhamRef = FirebaseDatabase.getInstance().getReference("SanPham");
        sanPhamRef.limitToFirst(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SanPham> categories = new ArrayList<>();

                // Thêm một ô "Sản phẩm hot" vào đầu danh sách thể loại
                SanPham hotCategory = new SanPham("Sản phẩm hot", 0.0, "", "", "Hot");  // Đây chỉ là một ô lọc, không phải sản phẩm thực tế
                Log.d(TAG, "Before adding hot category: " + hotCategory); // Log trước khi thêm
                categories.add(hotCategory);
                Log.d(TAG, "Before adding product: " + categories); // Log trước khi thêm từng sản phẩm

                // Thêm các sản phẩm thể loại khác vào danh sách
                for (DataSnapshot spSnapshot : snapshot.getChildren()) {
                    SanPham sanPham = spSnapshot.getValue(SanPham.class);
                    if (sanPham != null) {
                        categories.add(sanPham);
                    }
                }

                // Log sau khi hoàn tất việc thêm vào categories
                Log.d(TAG, "Categories list after adding: " + categories);

                // Cập nhật Adapter
                CategoryAdapter adapter = new CategoryAdapter(categories, theLoai -> {
                    if ("Sản phẩm hot".equalsIgnoreCase(theLoai)) {
                        // Khi chọn ô "Sản phẩm hot", gọi hàm filterHotProducts()
                        filterHotProducts(sanPhamList);  // Giả sử sanPhamList là danh sách sản phẩm đầy đủ
                    } else {
                        filterSanPhamByTheLoai(theLoai);
                    }
                });
                recyclerViewCategories.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load category.", error.toException());
            }
        });
    }



    private void filterSanPhamByTheLoai(String theLoai) {
        List<SanPham> filteredList = new ArrayList<>();
        for (SanPham sp : sanPhamList) {
            if (sp.getTheLoai().equalsIgnoreCase(theLoai)) {
                filteredList.add(sp);
            }
        }
        sanPhamAdapter.updateList(filteredList);
    }

    private void loadSanPham() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference spRef = database.getReference("SanPham");

        spRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SanPham> tempList = new ArrayList<>();
                for (DataSnapshot spSnapshot : snapshot.getChildren()) {
                    String tenSP = spSnapshot.child("TenSP").getValue(String.class);
                    Double gia = spSnapshot.child("Gia").getValue(Double.class);
                    String hinh = spSnapshot.child("Hinh").getValue(String.class);
                    String moTa = spSnapshot.child("MoTa").getValue(String.class);
                    String theLoai = spSnapshot.child("TheLoai").getValue(String.class);
                    String productId = spSnapshot.getKey();

                    if (tenSP != null && gia != null && hinh != null && theLoai != null) {
                        SanPham sp = new SanPham(tenSP, gia, hinh, moTa != null ? moTa : "", theLoai);
                        sp.setProductId(productId);
                        tempList.add(sp);
                    }
                }
                sanPhamList = tempList;  // Cập nhật danh sách sản phẩm
                onSanPhamLoaded(sanPhamList); // Hiển thị sản phẩm đã tải
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read SanPham.", error.toException());
            }
        });
    }

    private void filterHotProducts(List<SanPham> fullList) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        List<SanPham> filteredList = new ArrayList<>();
        int[] loadedCount = {0};

        for (SanPham sp : fullList) {
            String productId = sp.getProductId();

            DatabaseReference commentRef = database.getReference("Comments").child(productId);
            commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double total = 0;
                    int count = 0;

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Comment cmt = snap.getValue(Comment.class);
                        if (cmt != null) {
                            total += cmt.getRating();
                            count++;
                        }
                    }

                    double avg = count > 0 ? total / count : 0;
                    if (avg >= 4.0) {
                        // Nếu sản phẩm có điểm trung bình >= 4, tiếp tục lấy soldCount
                        DatabaseReference soldRef = database.getReference("soldCount").child(productId);
                        soldRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapSold) {
                                int sold = snapSold.exists() ? snapSold.getValue(Integer.class) : 0;
                                sp.setSoldCount(sold);
                                filteredList.add(sp);

                                // Kiểm tra khi đã xử lý hết
                                loadedCount[0]++;
                                if (loadedCount[0] == fullList.size()) {
                                    showTopHotProducts(filteredList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loadedCount[0]++;
                            }
                        });
                    } else {
                        loadedCount[0]++;
                        if (loadedCount[0] == fullList.size()) {
                            showTopHotProducts(filteredList);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadedCount[0]++;
                }
            });
        }
    }
    private void showTopHotProducts(List<SanPham> hotList) {
        // Sắp xếp theo lượt bán giảm dần
        hotList.sort((sp1, sp2) -> Integer.compare(sp2.getSoldCount(), sp1.getSoldCount()));

        // Chỉ lấy 2 sản phẩm hot nhất
        List<SanPham> top2 = hotList.size() > 2 ? hotList.subList(0, 2) : hotList;

        runOnUiThread(() -> {
            sanPhamAdapter = new SanPhamAdapter(top2, this);
            recyclerView.setAdapter(sanPhamAdapter);
        });
    }


    private void onSanPhamLoaded(List<SanPham> sanPhams) {
        sanPhamAdapter = new SanPhamAdapter(sanPhams, this);
        recyclerView.setAdapter(sanPhamAdapter);
    }

    private String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT);
    }

    private void filterSanPham(String query) {
        String normalizedQuery = removeDiacritics(query);

        List<SanPham> filteredList = new ArrayList<>();
        for (SanPham sp : sanPhamList) {
            String normalizedTenSP = removeDiacritics(sp.getTenSP());
            if (normalizedTenSP.contains(normalizedQuery)) {
                filteredList.add(sp);
            }
        }
        sanPhamAdapter.updateList(filteredList);
    }
}

