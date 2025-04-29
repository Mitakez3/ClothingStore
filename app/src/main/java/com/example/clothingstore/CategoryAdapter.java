package com.example.clothingstore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        holder.textCategoryName.setText(category.getTheLoai());
        Glide.with(holder.itemView.getContext())
                .load(category.getHinh())
                .into(holder.imageCategory);

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
