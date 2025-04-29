package com.example.clothingstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdatedListener {
    private RecyclerView recyclerView;
    private LinearLayout emptyCartLayout;
    private TextView totalPriceTextView;
    private CartAdapter cartAdapter;
    private List<SanPham> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        totalPriceTextView = findViewById(R.id.txtTotalPrice);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList, this);
        recyclerView.setAdapter(cartAdapter);

        findViewById(R.id.btnCheckout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToPayment();
            }
        });


        loadCartData();
    }

    private void proceedToPayment() {
        double total = 0;
        for (SanPham sanPham : cartList) {
            total += sanPham.getGia() * sanPham.getSoLuong();
        }

        // Chuyển qua màn PaymentActivity
        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);

        // Truyền tổng tiền
        intent.putExtra("totalPrice", total);

        // Truyền giỏ hàng dưới dạng JSON
        Gson gson = new Gson();
        String cartJson = gson.toJson(cartList);
        intent.putExtra("cartList", cartJson);

        startActivity(intent);
    }

    private void loadCartData() {
        SharedPreferences sharedPreferences = getSharedPreferences("CartPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cartList", "[]");
        Type type = new TypeToken<ArrayList<SanPham>>() {}.getType();
        List<SanPham> savedCart = gson.fromJson(json, type);

        if (savedCart != null && !savedCart.isEmpty()) {
            cartList.clear();
            cartList.addAll(savedCart);
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();

            emptyCartLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (SanPham sanPham : cartList) {
            total += sanPham.getGia() * sanPham.getSoLuong();
        }
        totalPriceTextView.setText(formatPrice(total));
    }

    private String formatPrice(double price) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "Tổng: " + numberFormat.format(price) + " VNĐ";
    }
}
