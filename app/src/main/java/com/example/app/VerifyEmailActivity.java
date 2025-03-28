package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.app.ui.login.LoginActivity;


public class VerifyEmailActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button btnCheckVerification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        auth = FirebaseAuth.getInstance();
        btnCheckVerification = findViewById(R.id.btnCheckVerification);

        btnCheckVerification.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.reload(); // Cập nhật trạng thái từ Firebase
                if (user.isEmailVerified()) {
                    Toast.makeText(this, "Email đã xác minh! Đăng nhập ngay!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email chưa được xác minh!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
