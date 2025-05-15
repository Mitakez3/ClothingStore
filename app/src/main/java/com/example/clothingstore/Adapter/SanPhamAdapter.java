package com.example.clothingstore.Adapter;

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
import com.example.clothingstore.Activity.ProductDetailActivity;
import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.SanPhamViewHolder> {

    private List<SanPham> sanPhamList;
    private final Context context;
    private OnAddToCartListener listener;
    private OnItemClickListener onItemClickListener;
    private boolean isInventoryMode;

    public interface OnItemClickListener {
        void onItemClick(String productId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public SanPhamAdapter(Context context, List<SanPham> sanPhamList, boolean isInventoryMode) {
        this.context = context;
        this.sanPhamList = sanPhamList;
        this.isInventoryMode = isInventoryMode;
    }

    @NonNull
    @Override
    public SanPhamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sanpham, parent, false);
        return new SanPhamViewHolder(itemView);
    }

    public interface OnAddToCartListener {
        void onAddToCart(String productId);
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

        // Lấy trung bình đánh giá từ Firebase
        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("Comments").child(sanPham.getProductId());

        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                if (avg == 0) {
                    holder.ratingTextView.setText("Chưa có đánh giá");
                } else {
                    holder.ratingTextView.setText("⭐ " + String.format("%.1f", avg));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.ratingTextView.setText("⭐ -");
            }
        });

        // Lấy số lượng đã bán từ Firebase
        DatabaseReference soldRef = FirebaseDatabase.getInstance()
                .getReference("soldCount").child(sanPham.getProductId());

        soldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sold = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                holder.soldTextView.setText("Đã bán " + sold);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.soldTextView.setText("Đã bán ?");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isInventoryMode) {
                // Ở chế độ quản lý tồn kho
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(sanPham.getProductId());
                }
            } else {
                // Ở chế độ xem sản phẩm
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productId", sanPham.getProductId());
                intent.putExtra("tenSP", sanPham.getTenSP());
                intent.putExtra("giaSP", sanPham.getGia());
                intent.putExtra("hinhSP", sanPham.getHinh());
                intent.putExtra("MoTa", sanPham.getMoTa());
                context.startActivity(intent);
            }
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
        TextView tenSPTextView, giaTextView, ratingTextView, soldTextView;

        SanPhamViewHolder(@NonNull View itemView) {
            super(itemView);
            hinhImageView = itemView.findViewById(R.id.hinhImageView);
            tenSPTextView = itemView.findViewById(R.id.tenSPTextView);
            giaTextView = itemView.findViewById(R.id.giaTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            soldTextView = itemView.findViewById(R.id.soldTextView);

        }
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }
}
