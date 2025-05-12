package com.example.clothingstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
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
    private TextView totalAmountTextView, tvAddress, tvChangeAddress;
    private RadioGroup paymentMethodGroup;
    private Button btnPlaceOrder;
    private List<SanPham> cartList;
    private double totalAmount;
    private String userId;
    private Voucher selectedVoucher = null;
    private double originalTotalAmount;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        totalAmountTextView = findViewById(R.id.txtTotalAmount);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        tvAddress = findViewById(R.id.tvAddress);
        tvChangeAddress = findViewById(R.id.tvChangeAddress);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserAddress();
        }
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeAddressDialog();
            }
        });

        tvChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeAddressDialog();
            }
        });


        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        Gson gson = new Gson();
        String cartJson = getIntent().getStringExtra("cartList");
        Type type = new TypeToken<List<SanPham>>() {}.getType();
        cartList = gson.fromJson(cartJson, type);
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

    private void loadUserAddress() {
        databaseReference.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.getValue(String.class);
                if (address != null && !address.isEmpty()) {
                    tvAddress.setText(address);
                    tvAddress.setTextColor(getResources().getColor(R.color.black));
                } else {
                    tvAddress.setText("Nhập địa chỉ của bạn");
                    tvAddress.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Lỗi khi tải địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangeAddressDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_dialog_address, null);
        dialog.setContentView(view);

        EditText edtPhone = view.findViewById(R.id.edtDialogPhone);
        EditText edtAddress = view.findViewById(R.id.edtDialogAddress);
        Button btnSave = view.findViewById(R.id.btnDialogSave);

        // Load dữ liệu hiện tại
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String phone = snapshot.child("phone").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);

                if (phone != null) edtPhone.setText(phone);
                if (address != null) edtAddress.setText(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnSave.setOnClickListener(v -> {
            String newPhone = edtPhone.getText().toString().trim();
            String newAddress = edtAddress.getText().toString().trim();

            if (newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(PaymentActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseReference.child("phone").setValue(newPhone);
            databaseReference.child("address").setValue(newAddress).addOnSuccessListener(unused -> {
                tvAddress.setText(newAddress);
                tvAddress.setTextColor(getResources().getColor(R.color.black));
                Toast.makeText(PaymentActivity.this, "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
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

    private double calculateTotalAmount() {
        double total = 0;
        if (cartList != null) {
            for (SanPham sp : cartList) {
                total += sp.getGia() * sp.getSoLuong();
            }
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
        List<SanPham> selectedItems = new ArrayList<>();

        for (SanPham sp : cartList) {
            // Kiểm tra nếu sản phẩm này đã được chọn
            if (sp.isSelected()) {
                OrderItem item = new OrderItem(
                        sp.getProductId(),
                        sp.getSoLuong(),
                        sp.getTenSP(),
                        sp.getHinh(),
                        sp.getGia()
                );
                orderItems.add(item);
                selectedItems.add(sp);
            }
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("address").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);

                if (address == null || address.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                    Toast.makeText(PaymentActivity.this, "Vui lòng nhập đầy đủ địa chỉ và số điện thoại trước khi đặt hàng", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", orderId);
                orderData.put("customerId", userId);
                orderData.put("paymentMethod", paymentMethod);
                orderData.put("totalAmount", totalAmount);
                orderData.put("orderItems", orderItems);
                orderData.put("status", "delivering");
                orderData.put("timestamp", ServerValue.TIMESTAMP);
                orderData.put("address", address);
                orderData.put("phone", phone);

                FirebaseDatabase.getInstance().getReference("orders")
                        .child(orderId)
                        .setValue(orderData)
                        .addOnSuccessListener(aVoid -> {
                            // giữ nguyên phần xử lý sau khi đặt hàng thành công (update soldCount, xóa giỏ, toast, chuyển màn...)

                            for (SanPham sp : selectedItems) {
                                DatabaseReference soldRef = FirebaseDatabase.getInstance()
                                        .getReference("soldCount")
                                        .child(sp.getProductId());

                                soldRef.runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        Integer currentCount = currentData.getValue(Integer.class);
                                        if (currentCount == null) {
                                            currentData.setValue(sp.getSoLuong());
                                        } else {
                                            currentData.setValue(currentCount + sp.getSoLuong());
                                        }
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    }
                                });
                            }

                            if (selectedVoucher != null) {
                                FirebaseDatabase.getInstance().getReference("UsedVouchers")
                                        .child(selectedVoucher.getVoucherId())
                                        .child(userId)
                                        .setValue(true);
                            }

                            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
                            List<com.google.android.gms.tasks.Task<Void>> deleteTasks = new ArrayList<>();

                            for (SanPham sp : selectedItems) {
                                deleteTasks.add(cartRef.child(sp.getProductId()).removeValue());
                            }

                            Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(task -> {
                                Toast.makeText(PaymentActivity.this, "Đặt hàng thành công và các sản phẩm đã bị xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });

                            clearCart();
                        })
                        .addOnFailureListener(e -> Toast.makeText(PaymentActivity.this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
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
                int random = 1000000 + new Random().nextInt(9000000);
                newOrderId = String.valueOf(random);
            } while (existingIds.contains(newOrderId));

            saveOrderToFirebase(newOrderId, paymentMethod);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Không thể tạo mã đơn hàng", Toast.LENGTH_SHORT).show();
        });
    }


    private void clearCart() {
        Intent intent = getIntent();
        if (!intent.hasExtra("cartList")) {
            SharedPreferences.Editor editor = getSharedPreferences("CartPrefs", MODE_PRIVATE).edit();
            editor.remove("cartList");
            editor.apply();
        }
    }

    private void payWithMomo() {
        Toast.makeText(this, "Đang chuyển hướng tới MoMo...", Toast.LENGTH_SHORT).show();
        generateUniqueOrderIdAndSave("Momo");
    }
}