package com.example.clothingstore.Adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<SanPham> categoryList;
    private OnCategoryClickListener listener;
    public interface OnCategoryClickListener {
        void onCategoryClick(String theLoai);
    }

    public CategoryAdapter(List<SanPham> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        SanPham category = categoryList.get(position);
        Log.d(TAG, "Category name: " + category.getTheLoai()); // Log để kiểm tra tên thể loại

        if ("Sản phẩm hot".equals(category.getTheLoai())) {
            holder.textCategoryName.setText("Sản phẩm hot");
            // Có thể thay đổi màu nền hoặc các thuộc tính khác cho ô "Sản phẩm hot"
            holder.imageCategory.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));  // Màu nền cho ô lọc "Sản phẩm hot"
        } else {
            holder.textCategoryName.setText(category.getTheLoai());
            Glide.with(holder.itemView.getContext())
                    .load(category.getHinh())
                    .into(holder.imageCategory);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category.getTheLoai());
            }
        });
    }





    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCategory;
        TextView textCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCategory = itemView.findViewById(R.id.imgCategory);
            textCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}
