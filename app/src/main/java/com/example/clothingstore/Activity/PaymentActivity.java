package com.example.clothingstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.CartAdapter;
import com.example.clothingstore.Adapter.VoucherAdapter;
import com.example.clothingstore.Domain.OrderItem;
import com.example.clothingstore.Domain.Voucher;
import com.example.clothingstore.R;
import com.example.clothingstore.Domain.SanPham;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private TextView totalAmountTextView;
    private RadioGroup paymentMethodGroup;
    private Button btnPlaceOrder;
    private List<SanPham> cartList;
    private double totalAmount;
    private String userId;
    private Voucher selectedVoucher = null;
    private double originalTotalAmount;

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
        originalTotalAmount = totalAmount;

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_LONG).show();
            finish(); // hoặc chuyển hướng đến LoginActivity
            return;
        }

        LinearLayout layoutDiscount = findViewById(R.id.layoutDiscount);
        layoutDiscount.setOnClickListener(v -> showVoucherBottomSheet());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void showVoucherBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_voucher, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerVouchers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference voucherRef = FirebaseDatabase.getInstance().getReference("Vouchers");
        DatabaseReference usedRef = FirebaseDatabase.getInstance().getReference("UsedVouchers");

        Map<String, Boolean> usedMap = new HashMap<>();
        List<Voucher> vouchers = new ArrayList<>();

        // Lấy danh sách UsedVouchers trước
        usedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usedSnapshot) {
                for (DataSnapshot voucherSnap : usedSnapshot.getChildren()) {
                    String voucherId = voucherSnap.getKey();
                    if (voucherSnap.hasChild(userId)) {
                        usedMap.put(voucherId, true);
                    }
                }

                // Sau đó mới lấy danh sách Voucher
                voucherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot voucherSnapshot) {
                        for (DataSnapshot child : voucherSnapshot.getChildren()) {
                            Voucher v = child.getValue(Voucher.class);
                            if (v != null) {
                                v.setVoucherId(child.getKey());
                                vouchers.add(v);
                            }
                        }

                        // Tạo Adapter sau khi có cả vouchers và usedMap
                        VoucherAdapter adapter = new VoucherAdapter(vouchers, userId, usedMap, selectedVoucher, new VoucherAdapter.OnVoucherApplyListener() {
                            @Override
                            public void onApply(Voucher voucher) {
                                selectedVoucher = voucher;
                                applyVoucher(voucher);
                                bottomSheetDialog.dismiss();
                            }

                            @Override
                            public void onCancel() {
                                selectedVoucher = null;
                                resetVoucher();
                                bottomSheetDialog.dismiss();
                            }
                        });


                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PaymentActivity.this, "Lỗi khi tải voucher", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Không thể kiểm tra voucher đã dùng", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }



    private void applyVoucher(Voucher newVoucher) {
        selectedVoucher = newVoucher;

        double discount = originalTotalAmount * (selectedVoucher.getDiscountPercent() / 100.0);
        double discountedTotal = originalTotalAmount - discount;

        TextView tvDiscountDetail = findViewById(R.id.tvDiscountDetail);
        tvDiscountDetail.setText("-" + formatPrice(discount));
        tvDiscountDetail.setTextColor(ContextCompat.getColor(this, R.color.red));

        totalAmountTextView.setText(formatPrice(discountedTotal));
        totalAmount = discountedTotal;
    }



    private void resetVoucher() {
        selectedVoucher = null;

        TextView tvDiscountDetail = findViewById(R.id.tvDiscountDetail);
        tvDiscountDetail.setText("Chọn mã giảm giá");
        tvDiscountDetail.setTextColor(ContextCompat.getColor(this, R.color.gray));

        totalAmount = originalTotalAmount;
        totalAmountTextView.setText(formatPrice(totalAmount));
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
            generateUniqueOrderIdAndSave("COD");
        } else if (paymentMethod.equals("MoMo")) {
            payWithMomo();
        }
    }


    private void saveOrderToFirebase(String orderId, String paymentMethod) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (SanPham sp : cartList) {
            OrderItem item = new OrderItem(
                    sp.getProductId(),
                    sp.getSoLuong(),
                    sp.getTenSP(),
                    sp.getHinh(),
                    sp.getGia()
            );
            orderItems.add(item);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("customerId", userId);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("totalAmount", totalAmount);
        orderData.put("orderItems", orderItems);
        orderData.put("status", "delivering");
        orderData.put("timestamp", ServerValue.TIMESTAMP);
        Log.d("VoucherSave", "SelectedVoucher: " + selectedVoucher.getVoucherId() + ", UserId: " + userId);


        FirebaseDatabase.getInstance().getReference("orders")
                .child(orderId)
                .setValue(orderData)
                .addOnSuccessListener(aVoid -> {
                    if (selectedVoucher != null) {
                        // Lưu voucher đã sử dụng
                        FirebaseDatabase.getInstance().getReference("UsedVouchers")
                                .child(selectedVoucher.getVoucherId())
                                .child(userId)
                                .setValue(true);
                    }
                    Toast.makeText(this, "Đặt hàng thành công! Mã đơn #" + orderId, Toast.LENGTH_LONG).show();
                    clearCart();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show());
    }



    private void generateUniqueOrderIdAndSave(String paymentMethod) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.get().addOnSuccessListener(snapshot -> {
            Set<String> existingIds = new HashSet<>();
            for (DataSnapshot orderSnap : snapshot.getChildren()) {
                String existingId = orderSnap.child("orderId").getValue(String.class);
                if (existingId != null) existingIds.add(existingId);
            }

            String newOrderId;
            do {
                int random = 1000000 + new Random().nextInt(9000000); // 7 chữ số
                newOrderId = String.valueOf(random);
            } while (existingIds.contains(newOrderId));

            saveOrderToFirebase(newOrderId, paymentMethod); // Gọi lưu đơn
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Không thể tạo mã đơn hàng", Toast.LENGTH_SHORT).show();
        });
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
        generateUniqueOrderIdAndSave("Momo");
    }
}