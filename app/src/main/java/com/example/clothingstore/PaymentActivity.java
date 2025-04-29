package com.example.clothingstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
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
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private TextView totalAmountTextView;
    private RadioGroup paymentMethodGroup;
    private Button btnPlaceOrder;
    private List<SanPham> cartList;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        totalAmountTextView = findViewById(R.id.txtTotalAmount);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Load cart data from Intent or SharedPreferences
        cartList = loadCartData();
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(new CartAdapter(this, cartList, null));

        // Calculate and display total amount
        totalAmount = calculateTotalAmount();
        totalAmountTextView.setText(formatPrice(totalAmount));

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private List<SanPham> loadCartData() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<SanPham>>() {}.getType();

        // Check if cartList is passed via Intent (from ProductDetailActivity "Buy Now")
        Intent intent = getIntent();
        String cartJson = intent.getStringExtra("cartList");
        if (cartJson != null && !cartJson.isEmpty()) {
            List<SanPham> cartList = gson.fromJson(cartJson, type);
            if (cartList != null) {
                return cartList;
            }
        }

        // Fallback to SharedPreferences (from CartActivity)
        SharedPreferences sharedPreferences = getSharedPreferences("CartPrefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("cartList", "[]");
        List<SanPham> cartList = gson.fromJson(json, type);
        return cartList != null ? cartList : new ArrayList<>();
    }

    private double calculateTotalAmount() {
        double total = 0;
        for (SanPham sanPham : cartList) {
            total += sanPham.getGia() * sanPham.getSoLuong();
        }
        // Override with totalPrice from Intent if available (for consistency)
        Intent intent = getIntent();
        if (intent.hasExtra("totalPrice")) {
            total = intent.getDoubleExtra("totalPrice", total);
        }
        return total;
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(price) + " VNĐ";
    }

    private void placeOrder() {
        int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String paymentMethod = selectedRadioButton.getText().toString();

        if (paymentMethod.equals("Thanh toán khi nhận hàng")) {
            saveOrderToFirebase("COD");
        } else if (paymentMethod.equals("MoMo")) {
            // Thanh toán qua MoMo
            payWithMomo();
        }
    }

    private void saveOrderToFirebase(String paymentMethod) {
        String orderId = FirebaseDatabase.getInstance().getReference("orders").push().getKey();

        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (SanPham sp : cartList) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", sp.getProductId()); // Lấy productId đã gán
            item.put("tenSP", sp.getTenSP());
            item.put("gia", sp.getGia());
            item.put("hinh", sp.getHinh());
            item.put("moTa", sp.getMoTa());
            item.put("theLoai", sp.getTheLoai());
            item.put("soLuong", sp.getSoLuong());
            orderItems.add(item);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("totalAmount", totalAmount);
        orderData.put("orderItems", orderItems); // dùng danh sách mới có chứa productId
        orderData.put("status", "delivering");   // <-- thêm dòng này để gán trạng thái mặc định

        FirebaseDatabase.getInstance().getReference("orders")
                .child(orderId)
                .setValue(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    clearCart();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show());
    }


    private void clearCart() {
        // Only clear SharedPreferences cart if coming from CartActivity
        Intent intent = getIntent();
        if (!intent.hasExtra("cartList")) {
            SharedPreferences.Editor editor = getSharedPreferences("CartPrefs", MODE_PRIVATE).edit();
            editor.remove("cartList");
            editor.apply();
        }
    }

    private void payWithMomo() {
        // Tích hợp MoMo sẽ thêm ở bước tiếp theo
        Toast.makeText(this, "Đang chuyển hướng tới MoMo...", Toast.LENGTH_SHORT).show();
        saveOrderToFirebase("MoMo");
    }
}