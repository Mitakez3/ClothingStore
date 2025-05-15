package com.example.clothingstore.Adapter;

import android.app.AlertDialog;
import android.content.Context;
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
import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.Domain.OrderItem;
import com.example.clothingstore.R;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReviewProductAdapter extends RecyclerView.Adapter<ReviewProductAdapter.ViewHolder> {
    private List<OrderItem> itemList;
    private Context context;
    private String customerId;
    private OnReviewCompleteListener reviewCompleteListener;

    public interface OnReviewCompleteListener {
        void onReviewComplete();
    }
    public void setOnReviewCompleteListener(OnReviewCompleteListener listener) {
        this.reviewCompleteListener = listener;
    }
    public ReviewProductAdapter(List<OrderItem> itemList, Context context, String customerId) {
        this.itemList = itemList;
        this.context = context;
        this.customerId = customerId;
    }


    private void showReviewPopup(Context context, String productId, String tenSP) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_review, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        TextView tvProductName = view.findViewById(R.id.tvProductName);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        tvProductName.setText(tenSP);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String commentText = etComment.getText().toString().trim();

            if (rating == 0 || commentText.isEmpty()) {
                Toast.makeText(context, "Vui lòng đánh giá và nhập nhận xét", Toast.LENGTH_SHORT).show();
                return;
            }

            Comment comment = new Comment(customerId, rating, commentText, System.currentTimeMillis());

            FirebaseDatabase.getInstance().getReference("Comments")
                    .child(productId)
                    .push()
                    .setValue(comment)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        if (reviewCompleteListener != null) {
                            reviewCompleteListener.onReviewComplete();
                        }
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

        holder.tvQuantity.setText("Số lượng: " + item.getSoLuong());
        holder.tvPrice.setText(formatPrice(item.getGia()));

        // Load ảnh sản phẩm nếu có (giả sử có URL hình trong item.getHinh())
        Glide.with(context)
                .load(item.getHinh())
                .into(holder.imgHinh);

        // Xử lý nút Review cho từng sản phẩm
        holder.btnReview.setOnClickListener(v -> {
            showReviewPopup(context, item.getProductId(), item.getTenSP());
        });

        if (item.isReviewed()) {
            holder.btnReview.setText("Đã đánh giá");
            holder.btnReview.setEnabled(false);
        } else {
            holder.btnReview.setText("Đánh giá");
            holder.btnReview.setEnabled(true);
        }

    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }




    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenSP, tvOrderId, tvDate, tvPrice, tvQuantity;
        ImageView imgHinh;
        Button btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            imgHinh = itemView.findViewById(R.id.imgHinh);
            btnReview = itemView.findViewById(R.id.btnReview);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}



