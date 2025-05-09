package com.example.clothingstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clothingstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    FirebaseAuth auth;
    Button logout;
    TextView textView;
    FirebaseUser user;
    EditText editUsername;
    Button btnSaveUsername;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);

        auth = FirebaseAuth.getInstance();
        logout = findViewById(R.id.btn_logout);
        textView = findViewById(R.id.username);
        editUsername = findViewById(R.id.edit_username);
        btnSaveUsername = findViewById(R.id.btn_save_username);


        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(Profile.this, Login.class);
            startActivity(intent);
            finish();
        }
        else {
            String uid = user.getUid();
            // Lấy tham chiếu đến Realtime Database
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
            // Lấy username từ Realtime Database
            loadUsername();
        }
        // Thêm sự kiện click cho nút "Save Username"
        btnSaveUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUsername(); // Gọi phương thức saveUsername() khi nút được nhấn
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Profile.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void saveUsername() {
        String username = editUsername.getText().toString().trim();
        if (username.isEmpty()) {
            editUsername.setError("Username is required");
            editUsername.requestFocus();
            return;
        }
        // Lưu username vào Realtime Database
        databaseReference.child("username").setValue(username);
        Toast.makeText(this, "Username saved", Toast.LENGTH_SHORT).show();
        loadUsername();
    }
    private void loadUsername() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        textView.setText(username);
                    } else {
                        textView.setText(user.getEmail());
                    }
                } else {
                    textView.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}