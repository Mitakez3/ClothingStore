package com.example.clothingstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clothingstore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccount extends AppCompatActivity {

    TextInputEditText emailEditText, passwordEditText, phoneEditText;
    Button btnCreateAcc;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_account);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.EmailEditText);
        passwordEditText = findViewById(R.id.PasswordEditText);
        phoneEditText = findViewById(R.id.PhoneEditText);
        btnCreateAcc = findViewById(R.id.btn_create_acc);
        progressBar = findViewById(R.id.progressBar);

        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = String.valueOf(emailEditText.getText());
                String password = String.valueOf(passwordEditText.getText());
                String phone = String.valueOf(phoneEditText.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(CreateAccount.this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(CreateAccount.this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(CreateAccount.this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String uid = user.getUid();
                                        updateVoucherStatusForUser(uid);
                                        // Lưu userId vào SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("userId", uid);
                                        editor.apply();

                                        // Lưu phone vào database
                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child(uid)
                                                .child("phone")
                                                .setValue(phone)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> phoneTask) {
                                                        if (phoneTask.isSuccessful()) {
                                                            Toast.makeText(CreateAccount.this, "Tài khoản đã được tạo.",
                                                                    Toast.LENGTH_SHORT).show();
                                                            // Chuyển sang HomeActivity
                                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(CreateAccount.this, "Lưu số điện thoại thất bại.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(CreateAccount.this, "Đăng ký thất bại.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });



        TextView cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_account), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void updateVoucherStatusForUser (String userId) {
        DatabaseReference vouchersRef = FirebaseDatabase.getInstance().getReference("Vouchers");

        vouchersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    for (DataSnapshot voucherSnap : snapshot.getChildren()) {
                        // VoucherId, ví dụ voucher1
                        String voucherId = voucherSnap.getKey();

                        // Kiểm tra xem trong status đã có userId chưa
                        Object statusObj = voucherSnap.child("status").child(userId).getValue();
                        if (statusObj == null) {
                            // Nếu chưa có, cập nhật thành "ready"
                            vouchersRef.child(voucherId).child("status").child(userId).setValue("ready");
                        }
                    }
                }
            } else {
                Toast.makeText(CreateAccount.this, "Lỗi tải voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
