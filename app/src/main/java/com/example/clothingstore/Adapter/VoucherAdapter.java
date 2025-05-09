package com.example.clothingstore.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.R;
import com.example.clothingstore.Domain.Voucher;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;
    private OnApplyClickListener listener;

    public VoucherAdapter(List<Voucher> voucherList, OnApplyClickListener listener) {
        this.voucherList = voucherList;
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
        holder.txtExpiry.setText("Hết hạn sau " + voucher.getExpireDays() + " ngày");
        holder.txtVoucherTitle.setText(voucher.getTitle());
        holder.txtDiscount.setText("Giảm " + voucher.getDiscountPercent() + "%");

        holder.btnApply.setOnClickListener(v -> listener.onApplyClicked(voucher));
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView txtVoucherTitle, txtExpiry, txtDiscount;
        Button btnApply;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
            txtVoucherTitle = itemView.findViewById(R.id.txtVoucherTitle);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            btnApply = itemView.findViewById(R.id.btnApply);
        }
    }

    public interface OnApplyClickListener {
        void onApplyClicked(Voucher voucher);
    }
}

