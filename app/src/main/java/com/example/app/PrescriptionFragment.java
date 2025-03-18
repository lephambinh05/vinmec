package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrescriptionFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtHoTen, edtSoDienThoai, edtGhiChu;
    private ImageView imgDonThuoc;
    private Uri imageUri;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    public PrescriptionFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prescription, container, false);

        // Khởi tạo Firebase Auth, Firestore & Storage
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        edtHoTen = view.findViewById(R.id.edtHoTen);
        edtSoDienThoai = view.findViewById(R.id.edtSoDienThoai);
        edtGhiChu = view.findViewById(R.id.edtGhiChu);
        imgDonThuoc = view.findViewById(R.id.imgDonThuoc);
        progressBar = view.findViewById(R.id.progressBar);
        Button btnUpload = view.findViewById(R.id.btnUpload);
        Button btnGui = view.findViewById(R.id.btnGui);

        btnUpload.setOnClickListener(v -> openFileChooser());
        btnGui.setOnClickListener(v -> uploadPrescription());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgDonThuoc.setImageURI(imageUri);
        }
    }

    private void uploadPrescription() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getActivity(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(getActivity(), "Vui lòng chọn ảnh đơn thuốc!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String fileName = UUID.randomUUID().toString();  // Tạo tên file ngẫu nhiên
        StorageReference fileRef = storageReference.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL ảnh sau khi upload thành công
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveToFirestore(imageUrl, user.getUid()); // Lưu vào Firestore kèm UID
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Tải ảnh lên Firebase thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void saveToFirestore(String imageUrl, String userId) {
        String hoTen = edtHoTen.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();

        if (hoTen.isEmpty() || soDienThoai.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng để lưu vào Firestore
        Map<String, Object> prescription = new HashMap<>();
        prescription.put("userId", userId);
        prescription.put("hoTen", hoTen);
        prescription.put("soDienThoai", soDienThoai);
        prescription.put("ghiChu", ghiChu);
        prescription.put("anhDonThuoc", imageUrl);
        prescription.put("timestamp", System.currentTimeMillis());

        firestore.collection("don_thuoc").add(prescription)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Gửi đơn thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Lưu đơn thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }
}
