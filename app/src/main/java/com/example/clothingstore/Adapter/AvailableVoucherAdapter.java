package com.example.clothingstore.Adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Domain.Voucher;
import com.example.clothingstore.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvailableVoucherAdapter extends RecyclerView.Adapter<AvailableVoucherAdapter.VoucherViewHolder> {

    private final List<Voucher> voucherList;
    private final OnVoucherSaveListener listener;
    private final String userId;

    public interface OnVoucherSaveListener {
        void onSave(Voucher voucher);
    }

    public AvailableVoucherAdapter(List<Voucher> voucherList, String userId, OnVoucherSaveListener listener) {
        this.voucherList = voucherList;
        this.userId = userId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        holder.txtTitle.setText(voucher.getTitle());
        holder.txtDiscount.setText(voucher.getDiscountPercent() + "% off");
        holder.txtExpiry.setText("Valid until: " + voucher.getExpireDays());

        String userStatus = null;
        if (voucher.getStatus() != null) {
            userStatus = voucher.getStatus().get(userId);
        }

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

        if (isExpired) {
            holder.btnApply.setText("Hết hạn");
            holder.btnApply.setEnabled(false);
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.gray)));
            return;
        }

        // Trạng thái hiển thị theo voucher.status
        if ("claimed".equals(userStatus)) {
            holder.btnApply.setText("Đã lưu");
            holder.btnApply.setEnabled(false);
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.gray)));
        } else {
            holder.btnApply.setText("Lưu");
            holder.btnApply.setEnabled(true);
            holder.btnApply.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getResources().getColor(R.color.blue)));
            holder.btnApply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSave(voucher);
                }
            });
        }
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
