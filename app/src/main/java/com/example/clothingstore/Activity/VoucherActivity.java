package com.example.clothingstore.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.AvailableVoucherAdapter;
import com.example.clothingstore.Domain.Voucher;
import com.example.clothingstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoucherActivity extends AppCompatActivity {

    private RecyclerView recyclerViewVouchers;
    private Button btnAvailable, btnMyVoucher;
    private AvailableVoucherAdapter adapter;
    private DatabaseReference voucherRef;
    private String userId;

    private final List<Voucher> availableVouchers = new ArrayList<>();
    private final List<Voucher> claimedVouchers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));

        btnAvailable = findViewById(R.id.btnAvailableVouchers);
        btnMyVoucher = findViewById(R.id.btnMyVouchers);

        voucherRef = FirebaseDatabase.getInstance().getReference("Vouchers");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("VoucherActivity", "Current userId: " + userId);
            loadVouchersFromFirebase();
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAvailable.setOnClickListener(v -> {
            updateTabUI(true);
            showAvailableVouchers();
        });

        btnMyVoucher.setOnClickListener(v -> {
            updateTabUI(false);
            showClaimedVouchers();
        });
    }

    private void updateTabUI(boolean availableSelected) {
        if (availableSelected) {
            btnAvailable.setBackgroundTintList(getColorStateList(R.color.ic_colors));
            btnAvailable.setTextColor(getColor(R.color.white));

            btnMyVoucher.setBackgroundTintList(getColorStateList(R.color.light_gray));
            btnMyVoucher.setTextColor(getColor(R.color.gray));
        } else {
            btnMyVoucher.setBackgroundTintList(getColorStateList(R.color.ic_colors));
            btnMyVoucher.setTextColor(getColor(R.color.white));

            btnAvailable.setBackgroundTintList(getColorStateList(R.color.light_gray));
            btnAvailable.setTextColor(getColor(R.color.gray));
        }
    }

    private void loadVouchersFromFirebase() {
        voucherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableVouchers.clear();
                claimedVouchers.clear();

                for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                    Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                    if (voucher == null) continue;

                    voucher.setVoucherId(voucherSnapshot.getKey());

                    Map<String, String> statusMap = voucher.getStatus();
                    String userStatus = (statusMap != null) ? statusMap.get(userId) : null;

                    Log.d("VoucherActivity", "Voucher ID: " + voucher.getVoucherId() + ", StatusMap: " + statusMap);
                    Log.d("VoucherActivity", "Voucher: " + voucher.getTitle() + ", userStatus: " + userStatus);

                    if ("claimed".equals(userStatus)) {
                        claimedVouchers.add(voucher);
                    } else {
                        availableVouchers.add(voucher);
                    }
                }

                Log.d("VoucherActivity", "Available vouchers size: " + availableVouchers.size());
                Log.d("VoucherActivity", "Claimed vouchers size: " + claimedVouchers.size());

                updateTabUI(true);
                showAvailableVouchers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VoucherActivity.this, "Lỗi khi tải voucher!", Toast.LENGTH_SHORT).show();
                Log.e("VoucherActivity", "Failed to load vouchers", error.toException());
            }
        });
    }

    private void showAvailableVouchers() {
        adapter = new AvailableVoucherAdapter(availableVouchers, userId, voucher -> {
            // Cập nhật trạng thái voucher cho user trên Realtime Database
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("status/" + userId, "claimed");

            voucherRef.child(voucher.getVoucherId()).updateChildren(statusUpdate)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã lưu voucher!", Toast.LENGTH_SHORT).show();

                        Map<String, String> statusMap = voucher.getStatus();
                        if (statusMap == null) {
                            statusMap = new HashMap<>();
                            voucher.setStatus(statusMap);
                        }
                        statusMap.put(userId, "claimed");

                        availableVouchers.remove(voucher);
                        claimedVouchers.add(voucher);
                        showAvailableVouchers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi lưu voucher!", Toast.LENGTH_SHORT).show();
                    });
        });

        recyclerViewVouchers.setAdapter(adapter);
    }

    private void showClaimedVouchers() {
        adapter = new AvailableVoucherAdapter(claimedVouchers, userId, null);
        recyclerViewVouchers.setAdapter(adapter);
    }
}
