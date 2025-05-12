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

        for (SanPham item : cartList) {
            // Kiểm tra nếu sản phẩm đã có trong giỏ hàng, nếu có thì cập nhật số lượng
            cartRef.child(item.getProductId()).child("quantity").setValue(item.getSoLuong());
            cartRef.child(item.getProductId()).child("size").setValue(item.getSize()); // Cập nhật size
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
                    String size = itemSnap.child("size").getValue(String.class);  // Assuming size is stored like this
                    if (productId != null && quantity != null && size != null) {
                        // Now load product details from SanPham table using productId
                        loadProductDetails(productId, quantity, size);
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

    private void loadProductDetails(String productId, Integer quantity, String size) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("SanPham").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SanPham product = snapshot.getValue(SanPham.class);
                if (product != null) {
                    product.setProductId(productId);
                    product.setSoLuong(quantity);
                    product.setSize(size);
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
        List<SanPham> selectedItems = new ArrayList<>();

        // Lọc các sản phẩm đã chọn
        for (SanPham sanPham : cartList) {
            if (sanPham.isSelected()) {
                selectedItems.add(sanPham);
            }
        }

        // Kiểm tra nếu không có sản phẩm nào được chọn
        if (selectedItems.isEmpty()) {
            Toast.makeText(CartActivity.this, "Chọn ít nhất 1 sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;  // Dừng lại và không tiếp tục đến màn thanh toán
        }

        // Chuyển qua màn PaymentActivity với giỏ hàng đã chọn
        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);

        // Chuyển giỏ hàng đã chọn dưới dạng JSON
        Gson gson = new Gson();
        String cartJson = gson.toJson(selectedItems);
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
            if (sanPham.isSelected()) {
                total += sanPham.getGia() * sanPham.getSoLuong();  // Tính tổng tiền chỉ cho sản phẩm đã chọn
            }
        }
        totalPriceTextView.setText(formatPrice(total));
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }
}
