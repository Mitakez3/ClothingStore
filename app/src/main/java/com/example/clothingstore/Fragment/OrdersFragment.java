package com.example.clothingstore.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Adapter.OrderAdapter;
import com.example.clothingstore.Domain.Order;
import com.example.clothingstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerViewOrders;
    private TextView txtLoginPrompt;
    private LinearLayout emptyOrderLayout;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_myorders, container, false);

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt);
        emptyOrderLayout = view.findViewById(R.id.emptyOrderLayout);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getContext());
        recyclerViewOrders.setAdapter(orderAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Chưa đăng nhập
            txtLoginPrompt.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            emptyOrderLayout.setVisibility(View.GONE);
        } else {
            // Đã đăng nhập
            txtLoginPrompt.setVisibility(View.GONE);
            loadOrdersFromFirebase(currentUser.getUid());
        }

        return view;
    }

    private void loadOrdersFromFirebase(String userId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.orderByChild("customerId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                order.setOrderId(orderSnapshot.getKey());
                                orderList.add(order);
                            }
                        }

                        Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        orderAdapter.notifyDataSetChanged();

                        if (orderList.isEmpty()) {
                            emptyOrderLayout.setVisibility(View.VISIBLE);
                            recyclerViewOrders.setVisibility(View.GONE);
                        } else {
                            emptyOrderLayout.setVisibility(View.GONE);
                            recyclerViewOrders.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
