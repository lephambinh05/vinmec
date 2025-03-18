package com.example.app; // Thay bằng package name thực tế của bạn

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private CircleImageView ivAvatar;
    private TextView tvEmail, tvUsername, tvGender, tvPhone;
    private Button btnEditProfile,btnLogout;

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();

        // Lấy thông tin người dùng hiện tại
        currentUser = mAuth.getCurrentUser();
        // Khởi tạo các view
        initViews();
        setupClickListeners();

        // Tải thông tin người dùng từ Firebase Auth và Firestore
        loadUserProfile();
        loadUserData();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);

        tvEmail = findViewById(R.id.tv_email);
        tvUsername = findViewById(R.id.tv_username);
        tvGender = findViewById(R.id.tv_gender);
        tvPhone = findViewById(R.id.tv_phone);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileActivity();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }


    private void loadUserProfile() {
        // Hiển thị email từ FirebaseAuth
        tvEmail.setText(currentUser.getEmail());

        // Hiển thị ảnh đại diện từ FirebaseAuth nếu có
        Uri photoUrl = currentUser.getPhotoUrl();
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_avatar_default)
                    .into(ivAvatar);
        }
    }

    private void loadUserData() {
        // Hiển thị thông báo đang tải
        showLoading(true);

        // Lấy dữ liệu từ Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                updateUIWithFirestoreData(document);
                            } else {
                                // Không tìm thấy document cho user này
                                Toast.makeText(UserProfileActivity.this,
                                        "Không tìm thấy dữ liệu người dùng",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Lỗi khi truy vấn
                            Toast.makeText(UserProfileActivity.this,
                                    "Lỗi khi lấy dữ liệu: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUIWithFirestoreData(DocumentSnapshot document) {
        // Hiển thị email từ dữ liệu Firebase
        String email = document.getString("email");
        if (email != null && !email.isEmpty()) {
            tvEmail.setText(email);
        }

        // Hiển thị username
        String username = document.getString("username");
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
        }

        // Hiển thị giới tính
        Object genderObj = document.get("gender");
        if (genderObj != null) {
            int gender = 0;
            if (genderObj instanceof Long) {
                gender = ((Long) genderObj).intValue();
            } else if (genderObj instanceof Integer) {
                gender = (Integer) genderObj;
            } else if (genderObj instanceof String) {
                try {
                    gender = Integer.parseInt((String) genderObj);
                } catch (NumberFormatException e) {
                    gender = 0;
                }
            }

            // Chuyển đổi giá trị số thành văn bản
            String genderText;
            switch (gender) {
                case 1:
                    genderText = "Nam";
                    break;
                case 2:
                    genderText = "Nữ";
                    break;
                default:
                    genderText = "Không xác định";
            }
            tvGender.setText(genderText);
        }

        // Hiển thị số điện thoại
        String phone = document.getString("phone");
        if (phone != null && !phone.isEmpty()) {
            tvPhone.setText(phone);
        }
    }

    // Hiển thị trạng thái đang tải (có thể thêm ProgressBar vào layout)
    private void showLoading(boolean isLoading) {
        // Thêm mã để hiển thị/ẩn ProgressBar nếu cần
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void openEditProfileActivity() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        // Đăng xuất khỏi Firebase Authentication
        FirebaseAuth.getInstance().signOut();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            ivAvatar.setImageURI(imageUri);

            // Upload ảnh lên Firebase Storage và cập nhật photoURL trong Authentication
            uploadImageAndUpdateProfile(imageUri);
        }
    }

    private void uploadImageAndUpdateProfile(Uri imageUri) {
        // Hiển thị thông báo đang tải lên
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        // Tạo tên file duy nhất
        String filename = "avatars/" + currentUser.getUid() + "/" + UUID.randomUUID().toString();
        final StorageReference fileRef = storageRef.child(filename);

        // Tải file lên
        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Lấy URL tải xuống
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Cập nhật URL ảnh đại diện vào Firebase Authentication
                                updateProfilePicture(downloadUri);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this,
                                "Lỗi tải lên: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfilePicture(Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this,
                                    "Cập nhật ảnh đại diện thành công",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this,
                                    "Lỗi cập nhật ảnh đại diện",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra xem người dùng còn đăng nhập không
        currentUser = mAuth.getCurrentUser();

        // Tải lại thông tin người dùng khi quay lại màn hình
        loadUserProfile();
        loadUserData();

    }
}