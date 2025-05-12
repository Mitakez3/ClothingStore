package com.example.clothingstore.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clothingstore.Adapter.CommentAdapter;
import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.Domain.SanPham;
import com.example.clothingstore.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView productTitle, productPrice, productDescription, tvAverageRating;
    private ImageView productImage;
    private Button btnBuyNow, btnAddToCart, btnShowComments;

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
        btnShowComments = findViewById(R.id.btnShowComments);
        tvAverageRating = findViewById(R.id.tvAverageRating);

        // Nhận dữ liệu sản phẩm từ Intent
        String productId = getIntent().getStringExtra("productId");
        String tenSP = getIntent().getStringExtra("tenSP");
        double giaSP = getIntent().getDoubleExtra("giaSP", 0.0);
        String hinhSP = getIntent().getStringExtra("hinhSP");
        String moTa = getIntent().getStringExtra("MoTa");

        // Cập nhật UI
        productTitle.setText(tenSP);
        productPrice.setText(formatPrice(giaSP));
        productDescription.setText(moTa);

        if (hinhSP != null && !hinhSP.isEmpty()) {
            Glide.with(this).load(hinhSP).into(productImage);
        }

        // Button listeners
        btnAddToCart.setOnClickListener(v -> {
            showSizeQuantityBottomSheet("cart", productId, tenSP, (int) giaSP, hinhSP);
        });

        btnBuyNow.setOnClickListener(v -> {
            showSizeQuantityBottomSheet("buy", productId, tenSP, (int) giaSP, hinhSP);
        });


        btnShowComments.setOnClickListener(v -> showCommentsPopup(productId));

        calculateAndDisplayAverageRating(productId);
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
        List<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList, this);
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

    private void showSizeQuantityBottomSheet(String actionType, String productId, String tenSP, int giaSP, String hinhSP) {
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_dialog_select_size_quantity, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        // Size buttons
        final String[] selectedSize = {""};

        Button btnXS = view.findViewById(R.id.btnSizeXS);
        Button btnS = view.findViewById(R.id.btnSizeS);
        Button btnM = view.findViewById(R.id.btnSizeM);
        Button btnL = view.findViewById(R.id.btnSizeL);
        Button btnXL = view.findViewById(R.id.btnSizeXL);
        Button btnXXL = view.findViewById(R.id.btnSizeXXL);
        Button btn3XL = view.findViewById(R.id.btnSize3XL);

        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        ImageView btnIncrease = view.findViewById(R.id.btnIncrease);
        ImageView btnDecrease = view.findViewById(R.id.btnDecrease);
        Button btnAction = view.findViewById(R.id.btnAction);

        // Set label theo loại action
        btnAction.setText(actionType.equals("buy") ? "Đặt hàng" : "Thêm vào giỏ hàng");

        List<Button> sizeButtons = new ArrayList<>();
        sizeButtons.add(btnXS);
        sizeButtons.add(btnS);
        sizeButtons.add(btnM);
        sizeButtons.add(btnL);
        sizeButtons.add(btnXL);
        sizeButtons.add(btnXXL);
        sizeButtons.add(btn3XL);

        // Xử lý chọn size
        View.OnClickListener sizeClickListener = v -> {
            Button selectedButton = (Button) v;
            selectedSize[0] = selectedButton.getText().toString();
            updateSizeSelection(selectedButton, sizeButtons);
        };

        for (Button btn : sizeButtons) {
            btn.setOnClickListener(sizeClickListener);
        }


        // Xử lý tăng giảm số lượng
        final int[] quantity = {1};
        btnIncrease.setOnClickListener(v -> {
            quantity[0]++;
            tvQuantity.setText(String.valueOf(quantity[0]));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        // Xử lý nút hành động
        btnAction.setOnClickListener(v -> {
            if (selectedSize[0] == null || selectedSize[0].isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
                return;
            }

            if (actionType.equals("cart")) {
                addToCart(productId, tenSP, giaSP, hinhSP, quantity[0], selectedSize[0]);
            } else {
                proceedToPayment(productId, quantity[0],selectedSize[0]);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateSizeSelection(Button selectedButton, List<Button> sizeButtons) {
        for (Button btn : sizeButtons) {
            if (btn == selectedButton) {
                btn.setBackgroundTintList(getResources().getColorStateList(R.color.dark_gray));
            } else {
                btn.setBackgroundTintList(getResources().getColorStateList(R.color.light_gray));
            }
        }
    }

    private void calculateAndDisplayAverageRating(String productId) {
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

                if (comments.isEmpty()) {
                    tvAverageRating.setText("Chưa có đánh giá");
                } else {
                    double averageRating = calculateAverageRating(comments);
                    displayAverageRating(averageRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private double calculateAverageRating(List<Comment> comments) {
        double totalRating = 0;
        int numRatings = 0;

        for (Comment c : comments) {
            totalRating += c.getRating();
            numRatings++;
        }

        return numRatings > 0 ? totalRating / numRatings : 0;
    }

    private void displayAverageRating(double averageRating) {
        if (averageRating == 0) {
            tvAverageRating.setText("Chưa có đánh giá");
        } else {
            tvAverageRating.setText("⭐ " + String.format("%.1f", averageRating));
        }
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }

    private void addToCart(String productId, String tenSP, int giaSP, String hinhSP, int soLuong, String size) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());

        HashMap<String, Object> cartItem = new HashMap<>();
        cartItem.put("quantity", soLuong);
        cartItem.put("size", size);

        Log.d("CartDebug", "Lưu sản phẩm vào giỏ: productId=" + productId + ", size=" + size + ", quantity=" + soLuong);

        cartRef.child(productId).setValue(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm giỏ hàng", Toast.LENGTH_SHORT).show());
    }

    private void proceedToPayment(String productId, int quantity, String size) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("SanPham").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SanPham product = snapshot.getValue(SanPham.class);
                if (product != null) {
                    product.setProductId(productId);
                    product.setSoLuong(quantity);
                    product.setSize(size);
                    product.setSelected(true);

                    List<SanPham> singleProductList = new ArrayList<>();
                    singleProductList.add(product);

                    double totalPrice = product.getGia() * quantity;

                    Intent intent = new Intent(ProductDetailActivity.this, PaymentActivity.class);
                    Gson gson = new Gson();
                    intent.putExtra("cartList", gson.toJson(singleProductList));
                    intent.putExtra("totalPrice", totalPrice);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
