package com.example.clothingstore.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.Adapter.CommentAdapter;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView productTitle, productPrice, productDescription, tvQuantity;
    private ImageView productImage, btnIncrease, btnDecrease;
    private Button btnBuyNow, btnAddToCart;
    private EditText edtComment;
    private RatingBar ratingBar;
    private Button btnSendComment;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private int quantity = 1; // Default quantity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        // Ánh xạ UI
        productTitle = findViewById(R.id.productTitle);
        productPrice = findViewById(R.id.productPrice);
        productImage = findViewById(R.id.productImage);
        productDescription = findViewById(R.id.productDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        Button btnShowComments = findViewById(R.id.btnShowComments);
        TextView tvAverageRating = findViewById(R.id.tvAverageRating);

        // Nhận dữ liệu sản phẩm từ Intent
        String productId = getIntent().getStringExtra("productId"); // dùng productId thật
        String tenSP = getIntent().getStringExtra("tenSP");
        double giaSP = getIntent().getDoubleExtra("giaSP", 0.0);
        String hinhSP = getIntent().getStringExtra("hinhSP");
        String moTa = getIntent().getStringExtra("MoTa");
        String theLoai = getIntent().getStringExtra("TheLoai");

        // Cập nhật UI
        productTitle.setText(tenSP);
        productPrice.setText(formatPrice(giaSP));
        productDescription.setText(moTa);
        tvQuantity.setText(String.valueOf(quantity));

        if (hinhSP != null && !hinhSP.isEmpty()) {
            Glide.with(this).load(hinhSP).into(productImage);
        }

        // Quantity selector logic
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnShowComments.setOnClickListener(v -> showCommentsPopup(productId));


        // Button listeners
        btnAddToCart.setOnClickListener(v -> addToCart(productId, tenSP, giaSP, hinhSP, quantity));
        btnBuyNow.setOnClickListener(v -> proceedToPayment(productId, tenSP, giaSP, hinhSP, quantity));

        calculateAndDisplayAverageRating(productId);

        Log.d("ProductDetailActivity", "Activity đã mở");
    }

    private void showCommentsPopup(String productId) {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_comments, null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewComments);
        Button btnClose = view.findViewById(R.id.btnClose);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        // Đặt nền trong suốt và popup ở dưới
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // RecyclerView setup
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, this);  // Passing context here
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

        // Lấy dữ liệu từ Firebase và hiển thị bình luận
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(productId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Comment c = snap.getValue(Comment.class);
                    if (c != null) {
                        commentList.add(c);
                    }
                }

                // Cập nhật lại RecyclerView
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void calculateAndDisplayAverageRating(String productId) {
        // Lấy dữ liệu từ Firebase và tính toán điểm trung bình cho sản phẩm với productId đó
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(productId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Comment c = snap.getValue(Comment.class);
                    if (c != null) {
                        comments.add(c);
                    }
                }

                // Tính điểm trung bình từ danh sách bình luận
                double averageRating = calculateAverageRating(comments);

                // Hiển thị điểm trung bình
                displayAverageRating(averageRating);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Phương thức tính toán điểm trung bình từ danh sách bình luận
    private double calculateAverageRating(List<Comment> comments) {
        double totalRating = 0;
        int numRatings = 0;

        for (Comment c : comments) {
            totalRating += c.getRating();
            numRatings++;
        }

        return numRatings > 0 ? totalRating / numRatings : 0; // Trả về điểm trung bình
    }

    // Phương thức cập nhật điểm trung bình liên tục
    private void updateAverageRatingFromComments(List<Comment> comments) {
        double averageRating = calculateAverageRating(comments);
        displayAverageRating(averageRating); // Hiển thị điểm trung bình
    }


    private void displayAverageRating(double averageRating) {
        TextView tvAverageRating = findViewById(R.id.tvAverageRating);  // Assuming you've added this TextView in XML
        tvAverageRating.setText("⭐ " + String.format("%.1f", averageRating));  // Display average rating
    }



    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }

    private void addToCart(String productId, String tenSP, double giaSP, String hinhSP, int soLuong) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy tham chiếu đến giỏ hàng của người dùng
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());

        // Chỉ lưu số lượng sản phẩm trong giỏ, không lưu toàn bộ thông tin sản phẩm
        cartRef.child(productId).child("quantity").setValue(soLuong)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm giỏ hàng", Toast.LENGTH_SHORT).show());
    }



    private void proceedToPayment(String productId, String tenSP, double giaSP, String hinhSP, int quantity) {
        List<SanPham> singleProductList = new ArrayList<>();
        SanPham product = new SanPham(tenSP, giaSP, hinhSP, "", "");
        product.setSoLuong(quantity);
        product.setProductId(productId); // Gán productId
        singleProductList.add(product);

        double totalPrice = giaSP * quantity;

        Intent intent = new Intent(this, PaymentActivity.class);
        Gson gson = new Gson();
        intent.putExtra("cartList", gson.toJson(singleProductList));
        intent.putExtra("totalPrice", totalPrice);
        startActivity(intent);

        Toast.makeText(this, "Đã đặt hàng", Toast.LENGTH_SHORT).show();
    }

}