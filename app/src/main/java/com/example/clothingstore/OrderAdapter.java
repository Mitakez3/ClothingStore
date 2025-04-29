package com.example.clothingstore;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
        holder.tvOrderId.setText("Mã đơn: " + order.getOrderId());
        holder.tvStatus.setText(order.getStatus());
        holder.tvItemCount.setText(order.getOrderItems().size() + " sản phẩm");

        if ("delivering".equals(order.getStatus())) {
            holder.btnAction.setText("Track");
        } else if ("delivered".equals(order.getStatus())) {
            holder.btnAction.setText("Review");
        }

        holder.btnAction.setOnClickListener(v -> {
            if ("Review".equals(holder.btnAction.getText())) {
                showReviewDialog(order.getOrderItems());
            } else if ("Track".equals(holder.btnAction.getText())) {
                Toast.makeText(context, "Theo dõi đơn hàng " + order.getOrderId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewDialog(List<OrderItem> orderItems) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_review);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewReview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Dùng trực tiếp danh sách orderItems
        ReviewProductAdapter adapter = new ReviewProductAdapter(orderItems, context);
        recyclerView.setAdapter(adapter);

        dialog.show();

        // Thiết lập kích thước dialog
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
