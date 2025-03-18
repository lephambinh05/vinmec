package com.example.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etBio;
    private Button btnSave;
    private Button btnCancel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo các view
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etBio = findViewById(R.id.et_bio);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Lấy thông tin hiện tại và hiển thị
        loadCurrentProfileInfo();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileInfo();
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadCurrentProfileInfo() {
        // Đây là nơi bạn sẽ lấy dữ liệu người dùng hiện tại
        // Ví dụ với SharedPreferences:

        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = preferences.getString("name", "");
        String email = preferences.getString("email", "");
        String phone = preferences.getString("phone", "");
        String bio = preferences.getString("bio", "");

        etName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);
        etBio.setText(bio);
    }

    private void saveProfileInfo() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return;
        }

        // Lưu thông tin vào SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("bio", bio);
        editor.apply();

        Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
    }
}