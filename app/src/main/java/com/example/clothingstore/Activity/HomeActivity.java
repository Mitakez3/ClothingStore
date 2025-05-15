package com.example.clothingstore.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.clothingstore.Fragment.OrdersFragment;
import com.example.clothingstore.Fragment.ProfileFragment;
import com.example.clothingstore.Fragment.VoucherFragment;
import com.example.clothingstore.R;
import com.example.clothingstore.Fragment.ShopFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mặc định load ShopFragment
        loadFragment(new ShopFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_shop) {
                fragment = new ShopFragment();
            } else if (item.getItemId() == R.id.nav_voucher) {
                fragment = new VoucherFragment();
            } else if (item.getItemId() == R.id.nav_orders) {
                fragment = new OrdersFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
