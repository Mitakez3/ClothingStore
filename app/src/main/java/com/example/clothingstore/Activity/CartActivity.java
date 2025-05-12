package com.example.clothingstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.CartAdapter;
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
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdatedListener {
    private RecyclerView recyclerView;
    private LinearLayout emptyCartLayout;
    private TextView totalPriceTextView;
    private CartAdapter cartAdapter;
    private List<SanPham> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        totalPriceTextView = findViewById(R.id.txtTotalPrice);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList, this);
        recyclerView.setAdapter(cartAdapter);

        findViewById(R.id.btnCheckout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToPayment();
            }
        });

        loadCartFromFirebase();
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveCartToFirebase();
    }

    private void saveCartToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());

        cartRef.removeValue(); // Xoá giỏ cũ trước khi ghi lại

        for (SanPham item : cartList) {
            cartRef.child(item.getProductId()).child("quantity").setValue(item.getSoLuong());
        }
    }


    private void loadCartFromFirebase() {
        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        Log.d("CartActivity", "userId từ SharedPreferences: " + userId);

        // Nếu userId không tồn tại thì không thể tải giỏ hàng
        if (userId == null) {
            Toast.makeText(CartActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Truy cập giỏ hàng của người dùng theo userId trong Firebase
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String productId = itemSnap.getKey();  // Assuming productId is the key
                    Integer quantity = itemSnap.child("quantity").getValue(Integer.class);  // Assuming quantity is stored like this

                    if (productId != null && quantity != null) {
                        // Now load product details from SanPham table using productId
                        loadProductDetails(productId, quantity);
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                emptyCartLayout.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductDetails(String productId, Integer quantity) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("SanPham").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SanPham product = snapshot.getValue(SanPham.class);
                if (product != null) {
                    product.setProductId(productId); // GÁN productId CHO ĐỐI TƯỢNG
                    product.setSoLuong(quantity);
                    cartList.add(product);

                    // 👉 Sau khi thêm sản phẩm, cập nhật giao diện
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    emptyCartLayout.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Log.d("CartActivity", "Không tìm thấy sản phẩm với ID: " + productId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void proceedToPayment() {
        double total = 0;
        for (SanPham sanPham : cartList) {
            total += sanPham.getGia() * sanPham.getSoLuong();
        }

        // Chuyển qua màn PaymentActivity
        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);

        // Truyền tổng tiền
        intent.putExtra("totalPrice", total);

        // Truyền giỏ hàng dưới dạng JSON
        Gson gson = new Gson();
        String cartJson = gson.toJson(cartList);
        intent.putExtra("cartList", cartJson);

        startActivity(intent);
    }



    @Override
    public void onCartUpdated() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (SanPham sanPham : cartList) {
            total += sanPham.getGia() * sanPham.getSoLuong();
        }
        totalPriceTextView.setText(formatPrice(total));
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }
}
