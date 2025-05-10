package com.example.clothingstore.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Domain.Order;
import com.example.clothingstore.Domain.OrderItem;
import com.example.clothingstore.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        String status = order.getStatus();
        String statusText;
        switch (status) {
            case "delivering":
                statusText = "Đang giao hàng";
                holder.btnAction.setText("Đã nhận hàng?");
                break;
            case "delivered":
                statusText = "Đã giao hàng";
                holder.btnAction.setText("Đánh giá");
                break;
            default:
                statusText = "Không rõ trạng thái";
                holder.btnAction.setVisibility(View.GONE);
                break;
        }

        holder.tvStatus.setText(statusText);
        holder.tvItemCount.setText(order.getOrderItems().size() + " sản phẩm");

        holder.btnAction.setOnClickListener(v -> {
            if ("Đánh giá".equals(holder.btnAction.getText())) {
                showReviewDialog(order.getOrderItems(), order.getCustomerId()); // truyền thêm customerId
            } else if ("Đã nhận hàng?".equals(holder.btnAction.getText())) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có muốn xác nhận đã nhận hàng?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Cập nhật trạng thái trong Firebase
                            DatabaseReference orderRef = FirebaseDatabase.getInstance()
                                    .getReference("orders")
                                    .child(order.getOrderId());

                            orderRef.child("status").setValue("delivered")
                                    .addOnSuccessListener(aVoid -> {
                                        order.setStatus("delivered");
                                        holder.tvStatus.setText("Đã giao hàng");
                                        holder.btnAction.setText("Đánh giá");

                                        Toast.makeText(context, "Cảm ơn bạn đã xác nhận đơn hàng #" + order.getOrderId(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

    }

    private void showReviewDialog(List<OrderItem> orderItems, String customerId) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_review);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewReview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ReviewProductAdapter adapter = new ReviewProductAdapter(orderItems, context, customerId);
        recyclerView.setAdapter(adapter);

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            window.setLayout(width, height / 2);
            window.setGravity(Gravity.BOTTOM);
        }
    }



    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvItemCount;
        Button btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
