package com.example.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditHealthInfoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etHeight, etWeight, etMedicalHistory, etAllergy;
    private Spinner spinnerBloodType;
    private Switch switchSmoking, switchDrinking;
    private Button btnSaveHealthInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_health_info);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etAllergy = findViewById(R.id.etAllergy);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        switchSmoking = findViewById(R.id.switchSmoking);
        switchDrinking = findViewById(R.id.switchDrinking);
        btnSaveHealthInfo = findViewById(R.id.btnSaveHealthInfo);

        // Set dữ liệu nhóm máu vào Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);

        // Xử lý sự kiện lưu thông tin sức khỏe
        btnSaveHealthInfo.setOnClickListener(v -> saveHealthInfo());
    }

    private void saveHealthInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        String height = etHeight.getText().toString();
        String weight = etWeight.getText().toString();
        String bloodType = spinnerBloodType.getSelectedItem().toString();
        String medicalHistory = etMedicalHistory.getText().toString();
        String allergy = etAllergy.getText().toString();
        boolean isSmoking = switchSmoking.isChecked();
        boolean isDrinking = switchDrinking.isChecked();

        // Tạo HashMap để lưu vào Firestore
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("height", height);
        healthInfo.put("weight", weight);
        healthInfo.put("bloodType", bloodType);
        healthInfo.put("medicalHistory", medicalHistory);
        healthInfo.put("allergy", allergy);
        healthInfo.put("smoking", isSmoking);
        healthInfo.put("drinking", isDrinking);

        db.collection("users").document(userId)
                .collection("healthInfo")
                .document("main")
                .set(healthInfo)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
