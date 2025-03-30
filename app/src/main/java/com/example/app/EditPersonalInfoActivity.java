package com.example.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditPersonalInfoActivity extends AppCompatActivity {

    private EditText etCMT, etJob, etAddress, etNationality, etEthnicity;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        etCMT = findViewById(R.id.etCMT);
        etJob = findViewById(R.id.etJob);
        etAddress = findViewById(R.id.etAddress);
        etNationality = findViewById(R.id.etNationality);
        etEthnicity = findViewById(R.id.etEthnicity);
        btnSave = findViewById(R.id.btnSave);

        // Nút lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePersonalInfo();
            }
        });
    }

    private void savePersonalInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DocumentReference docRef = db.collection("users").document(userId);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("cmt", etCMT.getText().toString().trim());
        userInfo.put("job", etJob.getText().toString().trim());
        userInfo.put("address", etAddress.getText().toString().trim());
        userInfo.put("nationality", etNationality.getText().toString().trim());
        userInfo.put("ethnicity", etEthnicity.getText().toString().trim());

        docRef.update(userInfo)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
