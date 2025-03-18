package com.example.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Button btnSave, btnCancel, btnBack;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userRef = db.collection("users").document(currentUser.getUid());
            loadUserProfile();
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSave.setOnClickListener(v -> saveUserProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        if (userRef == null) return;

        userRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                etName.setText(snapshot.getString("username"));
                etPhone.setText(snapshot.getString("phone"));
            }
        });
    }

    private void saveUserProfile() {
        if (userRef == null) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Số điện thoại không được để trống");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", name);
        userData.put("phone", phone);

        userRef.update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
