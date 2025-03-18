package com.example.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.app.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private CircleImageView ivAvatar;
    private TextView tvEmail, tvUsername, tvGender, tvPhone;
    private Button btnEditProfile, btnLogout,btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ View
        initViews();

        // Load ảnh đại diện từ FirebaseAuth
        loadUserProfile();

        // Lắng nghe thay đổi dữ liệu từ Firestore (Cập nhật realtime)
        listenForUserDataChanges();

        // Chỉnh sửa hồ sơ
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvEmail = findViewById(R.id.tv_email);
        tvUsername = findViewById(R.id.tv_username);
        tvGender = findViewById(R.id.tv_gender);
        tvPhone = findViewById(R.id.tv_phone);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


        btnLogout.setOnClickListener(v -> logout());

    }

    private void loadUserProfile() {
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar_default)
                        .into(ivAvatar);
            }
        }
    }

    private void listenForUserDataChanges() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(UserProfileActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (document != null && document.exists()) {
                            updateUIWithFirestoreData(document);
                        }
                    }
                });
    }

    private void updateUIWithFirestoreData(DocumentSnapshot document) {
        tvUsername.setText(document.getString("username"));
        tvPhone.setText(document.getString("phone"));

        // Xử lý giới tính
        Object genderObj = document.get("gender");
        if (genderObj != null) {
            int gender = (genderObj instanceof Long) ? ((Long) genderObj).intValue() : 0;
            tvGender.setText(gender == 1 ? "Nam" : gender == 2 ? "Nữ" : "Không xác định");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();  // Chỉ load lại avatar từ FirebaseAuth
    }

    private void logout() {
        com.example.app.utils.SessionManager sessionManager = new com.example.app.utils.SessionManager(this);
        sessionManager.clearSession(); // Xóa phiên đăng nhập

        FirebaseAuth.getInstance().signOut(); // Đăng xuất Firebase

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
