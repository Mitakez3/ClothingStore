package com.example.clothingstore.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Activity.CartActivity;
import com.example.clothingstore.Adapter.CategoryAdapter;
import com.example.clothingstore.Adapter.SanPhamAdapter;
import com.example.clothingstore.Domain.GridSpacingItemDecoration;
import com.example.clothingstore.Activity.Profile;
import com.example.clothingstore.Domain.SanPham;
import com.example.clothingstore.R;
import com.google.android.material.imageview.ShapeableImageView;
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

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";

    private RecyclerView recyclerView, recyclerViewCategories;
    private SanPhamAdapter sanPhamAdapter;
    private CategoryAdapter categoryAdapter;
    private EditText searchBar;
    private List<SanPham> sanPhamList = new ArrayList<>();
    private List<SanPham> categoryList = new ArrayList<>();
    private ImageView btnCart;
    private ShapeableImageView userAvatar;

    public ShopFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_item_product, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategory);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        searchBar = view.findViewById(R.id.searchBar);
        btnCart = view.findViewById(R.id.btnCart);
        userAvatar = view.findViewById(R.id.useravatar);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CartActivity.class);
            startActivity(intent);
        });

        userAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Profile.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

        return view;
    }

    private void loadSanPham() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("SanPham");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sanPhamList.clear();
                categoryList.clear();

                int count = 0;
                for (DataSnapshot spSnapshot : dataSnapshot.getChildren()) {
                    String tenSP = spSnapshot.child("TenSP").getValue(String.class);
                    Double gia = spSnapshot.child("Gia").getValue(Double.class);
                    String hinh = spSnapshot.child("Hinh").getValue(String.class);
                    String moTa = spSnapshot.child("MoTa").getValue(String.class);
                    String theLoai = spSnapshot.child("TheLoai").getValue(String.class);

                    if (tenSP != null && gia != null && hinh != null && theLoai != null) {
                        SanPham sp = new SanPham(tenSP, gia, hinh, moTa != null ? moTa : "", theLoai);
                        sanPhamList.add(sp);

                        if (count < 6) {
                            categoryList.add(sp);
                            count++;
                        }
                    }
                }
                onSanPhamLoaded(sanPhamList);
                onCategoriesLoaded(categoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void onSanPhamLoaded(List<SanPham> sanPhams) {
        sanPhamAdapter = new SanPhamAdapter(sanPhams, getContext());
        recyclerView.setAdapter(sanPhamAdapter);
    }

    private void onCategoriesLoaded(List<SanPham> categories) {
        categoryAdapter = new CategoryAdapter(categories, theLoai -> {
            filterSanPhamByTheLoai(theLoai);
        });
        recyclerViewCategories.setAdapter(categoryAdapter);
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
        if (sanPhamAdapter != null) {
            sanPhamAdapter.updateList(filteredList);
        }
    }
}
