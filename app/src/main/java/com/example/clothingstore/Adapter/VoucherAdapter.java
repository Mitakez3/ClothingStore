package com.example.clothingstore.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.R;
import com.example.clothingstore.Domain.Voucher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private final List<Voucher> voucherList;
    private final OnVoucherApplyListener listener;
    private Voucher selectedVoucher;
    private final Map<String, Boolean> usedVoucherMap;
    private final String userId;


    public interface OnVoucherApplyListener {
        void onApply(Voucher voucher);
        void onCancel();
    }

    public VoucherAdapter(List<Voucher> vouchers, String userId, Map<String, Boolean> usedVoucherMap, Voucher selectedVoucher, OnVoucherApplyListener listener) {
        this.voucherList = vouchers;
        this.userId = userId;
        this.usedVoucherMap = usedVoucherMap;
        this.selectedVoucher = selectedVoucher;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        holder.txtTitle.setText(voucher.getTitle());
        holder.txtDiscount.setText(voucher.getDiscountPercent() + "% off");
        holder.txtExpiry.setText("Valid until: " + voucher.getExpireDays());

        // So sánh ngày hết hạn với ngày hiện tại
        boolean isExpired = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();
            Date expireDate = sdf.parse(voucher.getExpireDays());
            if (expireDate != null && today.after(expireDate)) {
                isExpired = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean isUsed = usedVoucherMap.containsKey(voucher.getVoucherId());


        if (isUsed) {
            holder.btnApply.setText("Đã sử dụng");
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.gray)));
            holder.btnApply.setEnabled(false);
            return;
        }


        if (isExpired) {
            holder.btnApply.setText("Hết hạn");
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.gray)));
            holder.btnApply.setEnabled(false);
            return; // Không xử lý sự kiện click
        }

        // Nếu còn hạn thì xử lý Apply/Cancel như trước
        if (selectedVoucher != null && voucher.getVoucherId().equals(selectedVoucher.getVoucherId())) {
            holder.btnApply.setText("Hủy");
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.red)));
        } else {
            holder.btnApply.setText("Áp dụng");
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.blue)));
        }
        holder.btnApply.setEnabled(true);

        holder.btnApply.setOnClickListener(v -> {
            if ("Áp dụng".equals(holder.btnApply.getText().toString())) {
                selectedVoucher = voucher;
                listener.onApply(voucher);
            } else if ("Hủy".equals(holder.btnApply.getText().toString())) {
                selectedVoucher = null;
                listener.onCancel();
            }
        });

    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDiscount, txtExpiry;
        Button btnApply;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtVoucherTitle);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
            btnApply = itemView.findViewById(R.id.btnApply);
        }
    }
}
