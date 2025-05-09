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
                for (DataSnapshot spSnapshot : snapshot.getChildren()) {
                    SanPham sanPham = spSnapshot.getValue(SanPham.class);
                    if (sanPham != null) {
                        categories.add(sanPham);
                    }
                }
                CategoryAdapter adapter = new CategoryAdapter(categories, theLoai -> {
                    filterSanPhamByTheLoai(theLoai);
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
        DatabaseReference myRef = database.getReference("SanPham");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sanPhamList.clear();
                Log.d(TAG, "Tổng số sản phẩm: " + dataSnapshot.getChildrenCount());
                for (DataSnapshot spSnapshot : dataSnapshot.getChildren()) {
                    String tenSP = spSnapshot.child("TenSP").getValue(String.class);
                    Double gia = spSnapshot.child("Gia").getValue(Double.class);
                    String hinh = spSnapshot.child("Hinh").getValue(String.class);
                    String moTa = spSnapshot.child("MoTa").getValue(String.class);
                    String theLoai = spSnapshot.child("TheLoai").getValue(String.class);

                    if (tenSP != null && gia != null && hinh != null && theLoai != null) {
                        SanPham sp = new SanPham(tenSP, gia, hinh, moTa != null ? moTa : "", theLoai);
                        sp.setProductId(spSnapshot.getKey()); // Gán key (ví dụ: sp1) làm productId
                        sanPhamList.add(sp);
                    }

                }
                onSanPhamLoaded(sanPhamList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
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

