package com.example.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.MainActivity;
import com.example.app.R;
import com.example.app.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText edUsername, edPassword, edConfirmPassword, edEmail, edPhone;
    private RadioGroup radioGroupGender;
    private Button buttonRegister, buttonBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edUsername = findViewById(R.id.edTDN);
        edPassword = findViewById(R.id.edMK);
        edConfirmPassword = findViewById(R.id.edNLMK);
        edEmail = findViewById(R.id.edMail);
        edPhone = findViewById(R.id.edSDT);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBack = findViewById(R.id.btBack);

        buttonRegister.setOnClickListener(view -> registerUser());
        buttonBack.setOnClickListener(view -> finish());
    }

    private void registerUser() {
        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();

        if (!validateInput(username, password, confirmPassword, email)) return;

        // Đăng ký trên Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Lưu dữ liệu vào Firestore
                            saveUserToFirestore(firebaseUser.getUid(), username, email, phone);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email, String phone) {
        int gender = (radioGroupGender.getCheckedRadioButtonId() == R.id.radioButtonFemale) ? 0 : 1;

        User newUser = new User(userId, username, email, phone, gender);

        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu!", Toast.LENGTH_LONG).show());
    }

    private boolean validateInput(String username, String password, String confirmPassword, String email) {
        if (username.isEmpty() || username.length() <= 5) {
            edUsername.setError("Tên đăng nhập phải dài hơn 5 ký tự");
            return false;
        }
        if (password.isEmpty() || password.length() <= 5) {
            edPassword.setError("Mật khẩu phải dài hơn 5 ký tự");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Xác nhận mật khẩu không khớp");
            return false;
        }
        if (email.isEmpty()) {
            edEmail.setError("Vui lòng nhập email");
            return false;
        }
        return true;
    }
}
