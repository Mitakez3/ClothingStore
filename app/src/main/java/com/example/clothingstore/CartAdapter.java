package com.example.clothingstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

        // Load ảnh bằng Glide
        if (currentItem.getHinh() != null && !currentItem.getHinh().isEmpty()) {
            Glide.with(context).load(currentItem.getHinh()).into(holder.productImage);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        // Xử lý nút tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            currentItem.setSoLuong(currentItem.getSoLuong() + 1);
            notifyItemChanged(position);
            saveCart();
            cartUpdatedListener.onCartUpdated();
        });

        // Xử lý nút giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            if (currentItem.getSoLuong() > 1) {
                currentItem.setSoLuong(currentItem.getSoLuong() - 1);
                notifyItemChanged(position);
                saveCart();
                cartUpdatedListener.onCartUpdated();
            }
        });

        // Xóa sản phẩm khỏi giỏ hàng
        holder.btnDelete.setOnClickListener(v -> {
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
            saveCart();
            cartUpdatedListener.onCartUpdated();
            Toast.makeText(context, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        });
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

        public CartViewHolder(View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantity = itemView.findViewById(R.id.txtQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }

    private void saveCart() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CartPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        editor.putString("cartList", json);
        editor.apply();
    }

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }
}