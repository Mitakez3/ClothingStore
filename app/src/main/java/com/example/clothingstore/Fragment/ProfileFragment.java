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

    private TextView textViewUsername, textNotLoggedIn;
    private EditText editUsername;
    private Button btnSaveUsername, btnLogout, btnLogin;
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
            loadUsername();

            btnSaveUsername.setOnClickListener(v -> saveUsername());
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
        loadUsername();
    }

    private void loadUsername() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        textViewUsername.setText(username);
                    } else {
                        textViewUsername.setText(user.getEmail());
                    }
                } else {
                    textViewUsername.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
