package com.example.clothingstore.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clothingstore.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;

    TextView textView, textPhone, textAddress, textEmail;
    Button logout, btnManageInventory, btnLogin;
    ShapeableImageView userAvatar;

    LinearLayout profileLayout, loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile); // giữ nguyên tên layout

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Ánh xạ view
        textView = findViewById(R.id.username);
        textEmail = findViewById(R.id.email);
        textPhone = findViewById(R.id.phone);
        textAddress = findViewById(R.id.address);
        logout = findViewById(R.id.btn_logout);
        btnManageInventory = findViewById(R.id.btn_manage_inventory);
        userAvatar = findViewById(R.id.useravatar);
        btnLogin = findViewById(R.id.btn_login);

        profileLayout = findViewById(R.id.profile_layout);
        loginLayout = findViewById(R.id.login_layout);

        // Kiểm tra đăng nhập
        if (user == null) {
            profileLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(Profile.this, Login.class);
                startActivity(intent);
            });

        } else {
            profileLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);

            String uid = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

            loadUserInfo();

            logout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                recreate(); // Load lại activity sau khi đăng xuất
            });

            // Admin UID
            if ("KPU4Ds064WWW10hCfzx5MzQUnmD2".equals(uid)) {
                btnManageInventory.setVisibility(View.VISIBLE);
                btnManageInventory.setOnClickListener(v -> {
                    startActivity(new Intent(Profile.this, InventoryActivity.class));
                });
            } else {
                btnManageInventory.setVisibility(View.GONE);
            }

            userAvatar.setOnClickListener(v -> showEditDialog());
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
                Toast.makeText(Profile.this, "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        Dialog dialog = new Dialog(this);
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

            Toast.makeText(Profile.this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
