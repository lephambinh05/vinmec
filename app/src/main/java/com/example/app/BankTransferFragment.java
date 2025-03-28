package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class BankTransferFragment extends Fragment {
    private TextView bankInfoText, transactionInfoText;
    private ImageView qrCodeImage;
    private Button completeButton;
    private FirebaseFirestore db;
    private String userId;
    private String fullName, phoneNumber, address;
    private long totalAmount = 0;
    private String bankName, accountNumber, accountHolder;
    private String transactionContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bank_transfer, container, false);

        db = FirebaseFirestore.getInstance();
        bankInfoText = view.findViewById(R.id.bank_info_text);
        transactionInfoText = view.findViewById(R.id.transaction_info_text);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        completeButton = view.findViewById(R.id.complete_button);

        // Lấy userId từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Lấy thông tin giao dịch từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            fullName = bundle.getString("fullName");
            phoneNumber = bundle.getString("phoneNumber");
            address = bundle.getString("address");
        }

        // Lấy thông tin ngân hàng từ Firestore
        loadBankInfo();

        return view;
    }

    private void loadBankInfo() {
        db.collection("BankInfo").document("default").get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        bankName = document.getString("bankName");
                        accountNumber = document.getString("accountNumber");
                        accountHolder = document.getString("accountHolder");

                        // Hiển thị thông tin ngân hàng
                        bankInfoText.setText("Ngân hàng: " + bankName + "\nSố tài khoản: " + accountNumber + "\nChủ tài khoản: " + accountHolder);

                        // Tạo nội dung giao dịch tự động
                        generateTransactionContent();

                        // Hiển thị nội dung giao dịch (tạm thời, số tiền sẽ được cập nhật sau)
                        transactionInfoText.setText("Nội dung chuyển khoản: " + transactionContent + "\nSố tiền: Đang tính toán...");

                        // Tính tổng số tiền từ giỏ hàng
                        calculateTotalAmount();
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin ngân hàng!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải thông tin ngân hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generateTransactionContent() {
        // Tạo nội dung giao dịch: DH_<userId>_<timestamp>_<random>
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String randomNum = String.format("%04d", new Random().nextInt(10000)); // Số ngẫu nhiên 4 chữ số
        transactionContent = "DH_" + randomNum;
    }

    private void calculateTotalAmount() {
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        totalAmount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long price = document.getLong("price");
                            Long quantity = document.getLong("quantity");
                            if (price != null && quantity != null) {
                                totalAmount += price * quantity;
                            }
                        }

                        // Cập nhật giao diện với số tiền
                        transactionInfoText.setText("Nội dung chuyển khoản: " + transactionContent + "\nSố tiền: " + totalAmount + " VNĐ");

                        // Tạo mã QR qua VietQR.io
                        generateQRCode(accountNumber, totalAmount, transactionContent);

                        // Gán sự kiện cho nút "Hoàn tất"
                        completeButton.setOnClickListener(v -> completeOrder(transactionContent));
                    }
                });
    }

    private void generateQRCode(String accountNumber, long amount, String transactionContent) {
        new Thread(() -> {
            try {
                // Tạo URL API VietQR.io
                String urlString = "https://img.vietqr.io/image/" + bankName + "-" + accountNumber + "-compact.jpg?amount=" + amount + "&addInfo=" + transactionContent;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Kiểm tra mã phản hồi
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Tải hình ảnh QR từ URL
                    getActivity().runOnUiThread(() -> {
                        Glide.with(getContext())
                                .load(urlString)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(qrCodeImage);
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi khi tạo mã QR: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Lỗi khi tạo mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void completeOrder(String transactionContent) {
        // Lưu đơn hàng vào Firestore
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String medicineName = document.getString("medicineName");
                            Long price = document.getLong("price");
                            Long quantity = document.getLong("quantity");

                            if (medicineName != null && price != null && quantity != null) {
                                Map<String, Object> order = new HashMap<>();
                                order.put("medicineName", medicineName);
                                order.put("price", price * quantity);
                                order.put("userId", userId);
                                order.put("fullName", fullName);
                                order.put("phoneNumber", phoneNumber);
                                order.put("address", address);
                                order.put("paymentMethod", "Bank Transfer");
                                order.put("transactionContent", transactionContent);
                                order.put("timestamp", System.currentTimeMillis());

                                DocumentReference orderRef = db.collection("Orders").document();
                                batch.set(orderRef, order);
                                batch.delete(document.getReference());
                            }
                        }

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đặt hàng thành công! Vui lòng chuyển khoản để hoàn tất.", Toast.LENGTH_SHORT).show();
                                    // Quay lại màn hình chính
                                    getActivity().getSupportFragmentManager().popBackStack(null, getActivity().getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.fragment_container, new HomeFragment());
                                    transaction.commit();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi đặt hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}