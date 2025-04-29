package com.example.clothingstore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.SanPhamViewHolder> {

    private List<SanPham> sanPhamList;
    private final Context context;

    public SanPhamAdapter(List<SanPham> sanPhamList, Context context) {
        this.sanPhamList = sanPhamList;
        this.context = context;
    }

    @NonNull
    @Override
    public SanPhamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sanpham, parent, false);
        return new SanPhamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SanPhamViewHolder holder, int position) {
        SanPham sanPham = sanPhamList.get(position);

        holder.tenSPTextView.setText(sanPham.getTenSP());
        holder.giaTextView.setText(formatPrice(sanPham.getGia()));

        if (sanPham.getHinh() != null && !sanPham.getHinh().isEmpty()) {
            Glide.with(context)
                    .load(sanPham.getHinh())
                    .into(holder.hinhImageView);
            holder.hinhImageView.setVisibility(View.VISIBLE);
        } else {
            holder.hinhImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", sanPham.getProductId());
            intent.putExtra("tenSP", sanPham.getTenSP());
            intent.putExtra("giaSP", sanPham.getGia());
            intent.putExtra("hinhSP", sanPham.getHinh());
            intent.putExtra("MoTa", sanPham.getMoTa());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sanPhamList != null ? sanPhamList.size() : 0;
    }

    public void updateList(List<SanPham> newList) {
        this.sanPhamList = newList;
        notifyDataSetChanged();
    }

    static class SanPhamViewHolder extends RecyclerView.ViewHolder {
        ImageView hinhImageView;
        TextView tenSPTextView, giaTextView;

        SanPhamViewHolder(@NonNull View itemView) {
            super(itemView);
            hinhImageView = itemView.findViewById(R.id.hinhImageView);
            tenSPTextView = itemView.findViewById(R.id.tenSPTextView);
            giaTextView = itemView.findViewById(R.id.giaTextView);
        }
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }
}
