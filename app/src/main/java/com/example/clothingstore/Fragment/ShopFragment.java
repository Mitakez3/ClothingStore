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
import com.example.clothingstore.Domain.Comment;
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

    private RecyclerView recyclerView, recyclerViewCategories,recyclerViewHot;
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

        // 1. Main grid
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        // 2. HOT horizontal list
        recyclerViewHot = view.findViewById(R.id.recyclerViewHot); // KHÔNG dùng getActivity().findViewById!
        recyclerViewHot.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewHot.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        // 3. Categories horizontal list
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategory);
        recyclerViewCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        loadSanPham();
        loadCategories();

        searchBar = view.findViewById(R.id.searchBar);
        btnCart = view.findViewById(R.id.btnCart);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CartActivity.class);
            startActivity(intent);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSanPham(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
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
                        String productId = spSnapshot.getKey(); // lấy sp1, sp2, ...
                        sp.setProductId(productId); // gán vào đối tượng
                        sanPhamList.add(sp);

                        if (count < 6) {
                            categoryList.add(sp);
                            count++;
                        }
                    }
                }
                onSanPhamLoaded(sanPhamList);
                filterHotProducts(sanPhamList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void onSanPhamLoaded(List<SanPham> sanPhams) {
        sanPhamAdapter = new SanPhamAdapter(getContext(), sanPhams, false);
        recyclerView.setAdapter(sanPhamAdapter);
    }

    private void loadCategories() {
        DatabaseReference sanPhamRef = FirebaseDatabase.getInstance().getReference("SanPham");
        sanPhamRef.limitToFirst(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SanPham> categories = new ArrayList<>();
                for (DataSnapshot spSnapshot : snapshot.getChildren()) {
                    SanPham sanPham = spSnapshot.getValue(SanPham.class);
                    if (sanPham != null) categories.add(sanPham);
                }
                categoryAdapter = new CategoryAdapter(categories, theLoai -> {
                    filterSanPhamByTheLoai(theLoai);
                });
                recyclerViewCategories.setAdapter(categoryAdapter);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load category.", error.toException());
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
        // Sắp xếp giảm dần, lấy 2 đầu
        hotList.sort((a, b) -> Integer.compare(b.getSoldCount(), a.getSoldCount()));
        List<SanPham> top2 = hotList.size() > 2 ? hotList.subList(0, 2) : hotList;

        // Đổ riêng lên recyclerViewHot
        requireActivity().runOnUiThread(() -> {
            SanPhamAdapter hotAdapter = new SanPhamAdapter(requireContext(), top2, false);
            recyclerViewHot.setAdapter(hotAdapter);
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
