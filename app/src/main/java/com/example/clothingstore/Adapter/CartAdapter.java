package com.example.clothingstore.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<SanPham> cartList;
    private Context context;
    private OnCartUpdatedListener cartUpdatedListener;

    public CartAdapter(Context context, List<SanPham> cartList, OnCartUpdatedListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.cartUpdatedListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SanPham currentItem = cartList.get(position);
        holder.productTitle.setText(currentItem.getTenSP());
        holder.productPrice.setText(formatPrice(currentItem.getGia()));
        holder.quantity.setText(String.valueOf(currentItem.getSoLuong()));

        holder.checkboxSelect.setChecked(currentItem.isSelected());
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentItem.setSelected(isChecked);
            cartUpdatedListener.onCartUpdated();
        });

        // Load image with Glide
        if (currentItem.getHinh() != null && !currentItem.getHinh().isEmpty()) {
            Glide.with(context).load(currentItem.getHinh()).into(holder.productImage);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        // Handle increase, decrease, and delete buttons
        holder.btnIncrease.setOnClickListener(v -> {
            currentItem.setSoLuong(currentItem.getSoLuong() + 1);
            notifyItemChanged(position);
            cartUpdatedListener.onCartUpdated();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (currentItem.getSoLuong() > 1) {
                currentItem.setSoLuong(currentItem.getSoLuong() - 1);
                notifyItemChanged(position);
                cartUpdatedListener.onCartUpdated();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
            updateCartInFirebase();
            cartUpdatedListener.onCartUpdated();
            Toast.makeText(context, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        });
    }


    private void updateCartInFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());

        // Lọc các sản phẩm đã chọn và xóa khỏi Firebase
        for (SanPham item : cartList) {
            if (item.isSelected()) {
                cartRef.child(item.getProductId()).removeValue();
            } else {
                // Cập nhật lại sản phẩm chưa chọn (có thể giữ lại số lượng)
                cartRef.child(item.getProductId()).child("quantity").setValue(item.getSoLuong());
            }
        }
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle, productPrice, quantity;
        ImageView btnIncrease, btnDecrease, btnDelete, productImage;
        CheckBox checkboxSelect;

        public CartViewHolder(View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantity = itemView.findViewById(R.id.txtQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            productImage = itemView.findViewById(R.id.productImage);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
    }

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }
}