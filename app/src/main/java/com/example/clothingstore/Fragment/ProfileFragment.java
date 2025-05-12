package com.example.clothingstore.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clothingstore.Activity.Login;
import com.example.clothingstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private TextView textViewUsername, textNotLoggedIn, textEmail, textPhone, textAddress;
    private EditText editUsername, editPhone, editAddress;
    private Button btnSaveUsername, btnLogout, btnLogin, btnSaveAddress;
    private LinearLayout profileLayout, loginLayout;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        profileLayout = view.findViewById(R.id.profile_layout);
        loginLayout = view.findViewById(R.id.login_layout);

        textEmail = view.findViewById(R.id.email);
        textPhone = view.findViewById(R.id.phone);
        textAddress = view.findViewById(R.id.address);
        editPhone = view.findViewById(R.id.edit_phone);
        editAddress = view.findViewById(R.id.edit_address);
        btnSaveAddress = view.findViewById(R.id.btn_save_address);

        textViewUsername = view.findViewById(R.id.username);
        editUsername = view.findViewById(R.id.edit_username);
        btnSaveUsername = view.findViewById(R.id.btn_save_username);
        btnLogout = view.findViewById(R.id.btn_logout);

        textNotLoggedIn = view.findViewById(R.id.text_not_logged_in);
        btnLogin = view.findViewById(R.id.btn_login);

        if (user == null) {
            // Chưa đăng nhập
            profileLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
            });
        } else {
            // Đã đăng nhập
            profileLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);

            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserInfo();

            btnSaveUsername.setOnClickListener(v -> saveUsername());
            btnSaveAddress.setOnClickListener(v -> saveAddressAndPhone());
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                // Sau khi logout, refresh lại fragment
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .detach(ProfileFragment.this)
                        .attach(ProfileFragment.this)
                        .commit();
            });
        }

        return view;
    }

    private void saveUsername() {
        String username = editUsername.getText().toString().trim();
        if (username.isEmpty()) {
            editUsername.setError("Username is required");
            editUsername.requestFocus();
            return;
        }
        databaseReference.child("username").setValue(username);
        Toast.makeText(getContext(), "Username saved", Toast.LENGTH_SHORT).show();
        loadUserInfo();
    }

    private void saveAddressAndPhone() {
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if (phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("phone").setValue(phone);
        databaseReference.child("address").setValue(address);
        Toast.makeText(getContext(), "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();

        textPhone.setText("Số điện thoại: " + phone);
        textAddress.setText("Địa chỉ: " + address);
    }

    private void loadUserInfo() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    if (username != null) {
                        textViewUsername.setText(username);
                        textEmail.setText("Email: " + user.getEmail());
                    } else {
                        textViewUsername.setText(user.getEmail());
                        textEmail.setVisibility(View.GONE);
                    }

                    if (phone != null) {
                        textPhone.setText("Số điện thoại: " + phone);
                        editPhone.setText(phone);
                    }

                    if (address != null) {
                        textAddress.setText("Địa chỉ: " + address);
                        editAddress.setText(address);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
