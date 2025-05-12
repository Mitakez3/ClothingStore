package com.example.clothingstore.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Domain.Order;
import com.example.clothingstore.Adapter.OrderAdapter;
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

public class MyOrdersActivity extends AppCompatActivity {
    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    TextView txtLoginPrompt;
    LinearLayout emptyOrderLayout;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorders);

        txtLoginPrompt = findViewById(R.id.txtLoginPrompt);
        emptyOrderLayout = findViewById(R.id.emptyOrderLayout);
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this);
        recyclerViewOrders.setAdapter(orderAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Chưa đăng nhập
            txtLoginPrompt.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            emptyOrderLayout.setVisibility(View.GONE);
        } else {
            // Đã đăng nhập, tải đơn hàng
            txtLoginPrompt.setVisibility(View.GONE);
            loadOrdersFromFirebase(currentUser.getUid());
        }
    }

    private void loadOrdersFromFirebase(String userId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.orderByChild("customerId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        orderList.clear();

                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                order.setOrderId(orderSnapshot.getKey());
                                orderList.add(order);
                            }
                        }

                        if (orderList.isEmpty()) {
                            recyclerViewOrders.setVisibility(View.GONE);
                            emptyOrderLayout.setVisibility(View.VISIBLE);
                        } else {
                            Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                            orderAdapter.notifyDataSetChanged();
                            recyclerViewOrders.setVisibility(View.VISIBLE);
                            emptyOrderLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(MyOrdersActivity.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}

