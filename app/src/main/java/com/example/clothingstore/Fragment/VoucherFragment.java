package com.example.clothingstore.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

public class VoucherFragment extends Fragment {

    private RecyclerView recyclerViewVouchers;
    private Button btnAvailable, btnMyVoucher;
    private AvailableVoucherAdapter adapter;
    private DatabaseReference voucherRef;
    private String userId;

    private final List<Voucher> availableVouchers = new ArrayList<>();
    private final List<Voucher> claimedVouchers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_voucher, container, false);

        recyclerViewVouchers = view.findViewById(R.id.recyclerViewVouchers);
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAvailable = view.findViewById(R.id.btnAvailableVouchers);
        btnMyVoucher = view.findViewById(R.id.btnMyVouchers);

        voucherRef = FirebaseDatabase.getInstance().getReference("Vouchers");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("VoucherFragment", "Current userId: " + userId);
            loadVouchersFromFirebase();
        } else {
            Toast.makeText(getContext(), "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
        }

        btnAvailable.setOnClickListener(v -> {
            updateTabUI(true);
            showAvailableVouchers();
        });

        btnMyVoucher.setOnClickListener(v -> {
            updateTabUI(false);
            showClaimedVouchers();
        });

        return view;
    }

    private void updateTabUI(boolean availableSelected) {
        if (availableSelected) {
            btnAvailable.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.ic_colors));
            btnAvailable.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            btnMyVoucher.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_gray));
            btnMyVoucher.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        } else {
            btnMyVoucher.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.ic_colors));
            btnMyVoucher.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            btnAvailable.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_gray));
            btnAvailable.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
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

                    Log.d("VoucherFragment", "Voucher ID: " + voucher.getVoucherId() + ", StatusMap: " + statusMap);
                    Log.d("VoucherFragment", "Voucher: " + voucher.getTitle() + ", userStatus: " + userStatus);

                    if ("claimed".equals(userStatus)) {
                        claimedVouchers.add(voucher);
                    } else {
                        availableVouchers.add(voucher);
                    }
                }

                Log.d("VoucherFragment", "Available vouchers size: " + availableVouchers.size());
                Log.d("VoucherFragment", "Claimed vouchers size: " + claimedVouchers.size());

                updateTabUI(true);
                showAvailableVouchers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải voucher!", Toast.LENGTH_SHORT).show();
                Log.e("VoucherFragment", "Failed to load vouchers", error.toException());
            }
        });
    }

    private void showAvailableVouchers() {
        adapter = new AvailableVoucherAdapter(availableVouchers, userId, voucher -> {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("status/" + userId, "claimed");

            voucherRef.child(voucher.getVoucherId()).updateChildren(statusUpdate)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã lưu voucher!", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getContext(), "Lỗi khi lưu voucher!", Toast.LENGTH_SHORT).show();
                    });
        });

        recyclerViewVouchers.setAdapter(adapter);
    }

    private void showClaimedVouchers() {
        adapter = new AvailableVoucherAdapter(claimedVouchers, userId, null);
        recyclerViewVouchers.setAdapter(adapter);
    }
}
