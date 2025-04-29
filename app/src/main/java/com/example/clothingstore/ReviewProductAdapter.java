package com.example.clothingstore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReviewProductAdapter extends RecyclerView.Adapter<ReviewProductAdapter.ViewHolder> {
    private List<OrderItem> itemList;
    private Context context;

    public ReviewProductAdapter(List<OrderItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    private void showReviewPopup(Context context, String productId, String tenSP) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_review, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        TextView tvProductName = view.findViewById(R.id.tvProductName);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        tvProductName.setText(tenSP); // Hiển thị tên sản phẩm

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Nền bo góc nếu có

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String commentText = etComment.getText().toString().trim();

            if (rating == 0 || commentText.isEmpty()) {
                Toast.makeText(context, "Please rate and comment", Toast.LENGTH_SHORT).show();
                return;
            }

            Comment comment = new Comment("user123", "Anonymous", rating, commentText, System.currentTimeMillis());

            FirebaseDatabase.getInstance().getReference("Comments")
                    .child(productId)
                    .push()
                    .setValue(comment)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Thank you for your review!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = itemList.get(position);

        holder.tvTenSP.setText(item.getTenSP());
        holder.tvOrderId.setText("Mã sản phẩm: " + item.getProductId());

        // Load ảnh sản phẩm nếu có (giả sử có URL hình trong item.getHinh())
        Glide.with(context)
                .load(item.getHinh())
                .into(holder.imgHinh);

        // Xử lý nút Review cho từng sản phẩm
        holder.btnReview.setOnClickListener(v -> {
            showReviewPopup(context, item.getProductId(), item.getTenSP());
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenSP, tvOrderId, tvDate;
        ImageView imgHinh;
        Button btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            imgHinh = itemView.findViewById(R.id.imgHinh);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}



