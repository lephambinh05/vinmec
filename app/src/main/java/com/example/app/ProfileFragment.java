package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvUsername, tvPersonalInfo, tvHealthInfo;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        ivAvatar = view.findViewById(R.id.imgAvatar);
        tvUsername = view.findViewById(R.id.tvUserNameProfile);
        tvPersonalInfo = view.findViewById(R.id.tv_personal_info);
        tvHealthInfo = view.findViewById(R.id.tv_health_info);

        loadUserProfile();

        // Chuyển sang màn hình chỉnh sửa thông tin cá nhân
        tvPersonalInfo.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditPersonalInfoActivity.class));
        });

        // Chuyển sang màn hình chỉnh sửa thông tin sức khỏe
        tvHealthInfo.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditHealthInfoActivity.class));
        });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Lấy ID từ FirebaseAuth

            // Truy xuất thông tin từ Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name"); // Lấy tên từ Firestore
                            String avatarUrl = document.getString("avatarUrl"); // Ảnh đại diện từ Firestore

                            if (name != null) {
                                tvUsername.setText(name);
                            } else {
                                tvUsername.setText("Người dùng chưa cập nhật");
                            }

                            // Load ảnh đại diện
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(requireContext()).load(avatarUrl).into(ivAvatar);
                            } else {
                                ivAvatar.setImageResource(R.drawable.ic_avatar_default);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải dữ liệu", e));
        }
    }
}
