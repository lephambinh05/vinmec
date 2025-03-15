package com.example.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app.MainActivity;
import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    private EditText edUser, edPass;
    private Button btLogin, btRegis;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edUser = view.findViewById(R.id.edUser);
        edPass = view.findViewById(R.id.edPass);
        btLogin = view.findViewById(R.id.btLogin);
        btRegis = view.findViewById(R.id.btRegis);

        btLogin.setOnClickListener(v -> loginUser());
        btRegis.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterActivity.class)));

        return view;
    }

    private void loginUser() {
        String email = edUser.getText().toString().trim();
        String password = edPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            // Điều hướng đến MainActivity hoặc nơi cần thiết
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(getContext(), "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

    }
}
