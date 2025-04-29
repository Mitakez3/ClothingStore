package com.example.clothingstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        edtComment = findViewById(R.id.edtComment);
        ratingBar = findViewById(R.id.ratingBar);
        btnSendComment = findViewById(R.id.btnSendComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);

        // Khởi tạo RecyclerView bình luận
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

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

        // Button listeners
        btnAddToCart.setOnClickListener(v -> addToCart(productId, tenSP, giaSP, hinhSP, quantity));
        btnBuyNow.setOnClickListener(v -> proceedToPayment(productId, tenSP, giaSP, hinhSP, quantity));

        // Sự kiện gửi bình luận


        Log.d("ProductDetailActivity", "Activity đã mở");
    }

    private void loadCommentsFromFirebase(String productId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(productId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProductDetailActivity", "Error loading comments: " + error.getMessage());
            }
        });
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }

    private void addToCart(String productId, String tenSP, double giaSP, String hinhSP, int quantity) {
        SharedPreferences sharedPreferences = getSharedPreferences("CartPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = sharedPreferences.getString("cartList", "[]");
        Type type = new TypeToken<ArrayList<SanPham>>() {}.getType();
        List<SanPham> cartList = gson.fromJson(json, type);

        boolean exists = false;
        for (SanPham sp : cartList) {
            if (sp.getProductId().equals(productId)) {
                sp.setSoLuong(sp.getSoLuong() + quantity);
                exists = true;
                break;
            }
        }

        if (!exists) {
            SanPham newProduct = new SanPham(tenSP, giaSP, hinhSP, "", "");
            newProduct.setSoLuong(quantity);
            newProduct.setProductId(productId); // Gán productId
            cartList.add(newProduct);
        }

        editor.putString("cartList", gson.toJson(cartList));
        editor.apply();
        Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
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


    private void addCommentToFirebase(String productId, String userId, String username, int rating, String comment) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(productId);

        String commentId = commentsRef.push().getKey(); // Tạo ID bình luận tự động
        long timestamp = System.currentTimeMillis(); // Lấy thời gian hiện tại

        HashMap<String, Object> commentData = new HashMap<>();
        commentData.put("userId", userId);
        commentData.put("username", username);
        commentData.put("rating", rating);
        commentData.put("comment", comment);
        commentData.put("timestamp", timestamp);

        assert commentId != null;
        commentsRef.child(commentId).setValue(commentData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Bình luận đã được thêm!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi thêm bình luận!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}