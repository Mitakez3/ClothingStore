package com.example.clothingstore.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clothingstore.Adapter.OrderAdapter;
import com.example.clothingstore.Domain.Order;
import com.example.clothingstore.R;
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
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private TextView txtLoginPrompt;

    public OrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_myorders, container, false);

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getContext());
        recyclerViewOrders.setAdapter(orderAdapter);

        String currentCustomerId = getCurrentUserId();  // Lấy userId từ SharedPreferences

        if (currentCustomerId == null) {
            // Nếu chưa đăng nhập
            txtLoginPrompt.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            // Nếu đã đăng nhập, lọc đơn hàng theo customerID
            txtLoginPrompt.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
            loadOrdersFromFirebase(currentCustomerId);
        }

        return view;
    }

    // Hàm lấy userId từ SharedPreferences
    private String getCurrentUserId() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("userId", null);  // Trả về null nếu chưa có userId
    }

    private void loadOrdersFromFirebase(String customerId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && customerId.equals(order.getCustomerId())) {
                        order.setOrderId(orderSnapshot.getKey());
                        orderList.add(order);
                    }
                }
                Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
