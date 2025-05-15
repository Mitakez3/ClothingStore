package com.example.clothingstore.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clothingstore.Activity.InventoryActivity;
import com.example.clothingstore.Activity.Login;
import com.example.clothingstore.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;

    TextView textView, textPhone, textAddress, textEmail;
    Button logout, btnManageInventory, btnLogin;
    ShapeableImageView userAvatar;

    LinearLayout profileLayout, loginLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Ánh xạ view
        textView = view.findViewById(R.id.username);
        textEmail = view.findViewById(R.id.email);
        textPhone = view.findViewById(R.id.phone);
        textAddress = view.findViewById(R.id.address);
        logout = view.findViewById(R.id.btn_logout);
        btnManageInventory = view.findViewById(R.id.btn_manage_inventory);
        userAvatar = view.findViewById(R.id.useravatar);
        btnLogin = view.findViewById(R.id.btn_login);

        profileLayout = view.findViewById(R.id.profile_layout);
        loginLayout = view.findViewById(R.id.login_layout);

        // Kiểm tra đăng nhập
        if (user == null) {
            profileLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), Login.class));
            });

        } else {
            profileLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);

            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

            loadUserInfo();

            logout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                // Reload fragment
                requireActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
            });

            // Admin UID
            if ("KPU4Ds064WWW10hCfzx5MzQUnmD2".equals(uid)) {
                btnManageInventory.setVisibility(View.VISIBLE);
                btnManageInventory.setOnClickListener(v -> {
                    startActivity(new Intent(getContext(), InventoryActivity.class));
                });
            } else {
                btnManageInventory.setVisibility(View.GONE);
            }

            userAvatar.setOnClickListener(v -> showEditDialog());
        }

        return view;
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
                        textView.setText("Xin chào, " + username);
                        textEmail.setText("Email: " + user.getEmail());
                    } else {
                        textView.setText(user.getEmail());
                        textEmail.setVisibility(View.GONE);
                    }

                    if (phone != null) textPhone.setText("Số điện thoại: " + phone);
                    if (address != null) textAddress.setText("Địa chỉ: " + address);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_profile);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.white);
        }

        EditText editUsername = dialog.findViewById(R.id.edit_username);
        EditText editPhone = dialog.findViewById(R.id.edit_phone);
        EditText editAddress = dialog.findViewById(R.id.edit_address);
        Button btnSave = dialog.findViewById(R.id.btn_save_profile);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editUsername.setText(snapshot.child("username").getValue(String.class));
                editPhone.setText(snapshot.child("phone").getValue(String.class));
                editAddress.setText(snapshot.child("address").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnSave.setOnClickListener(v -> {
            String username = editUsername.getText().toString();
            String phone = editPhone.getText().toString();
            String address = editAddress.getText().toString();

            databaseReference.child("username").setValue(username);
            databaseReference.child("phone").setValue(phone);
            databaseReference.child("address").setValue(address);

            Toast.makeText(getContext(), "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
