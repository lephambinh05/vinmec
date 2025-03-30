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

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private String orderId;

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
        } else {
            Toast.makeText(getContext(), "Thiếu thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Kiểm tra thông tin người dùng
        if (fullName == null || phoneNumber == null || address == null) {
            Toast.makeText(getContext(), "Vui lòng cung cấp đầy đủ thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return view;
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
                        accountHolder = document.getString("accountHolder");

                        // Xử lý accountNumber
                        Object accountNumberObj = document.get("accountNumber");
                        if (accountNumberObj != null) {
                            if (accountNumberObj instanceof String) {
                                accountNumber = (String) accountNumberObj;
                            } else if (accountNumberObj instanceof Number) {
                                accountNumber = String.valueOf(((Number) accountNumberObj).longValue());
                            } else {
                                Toast.makeText(getContext(), "Số tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getContext(), "Số tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Hiển thị thông tin ngân hàng
                        bankInfoText.setText("Ngân hàng: " + bankName + "\nSố tài khoản: " + accountNumber + "\nChủ tài khoản: " + accountHolder);

                        // Tạo nội dung giao dịch tự động
                        generateTransactionContent();

                        // Hiển thị nội dung giao dịch (tạm thời, số tiền sẽ được cập nhật sau)
                        transactionInfoText.setText("Nội dung chuyển khoản: " + transactionContent + "\nSố tiền: Đang tính toán...");

                        // Tính tổng số tiền và lấy danh sách thuốc
                        calculateTotalAmountAndOrderItems();
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin ngân hàng!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải thông tin ngân hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generateTransactionContent() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String randomNum = String.format("%04d", new Random().nextInt(10000));
        transactionContent = "DH" + randomNum;
    }

    private void calculateTotalAmountAndOrderItems() {
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        totalAmount = 0;
                        JSONArray orderItemsArray = new JSONArray();

                        // Duyệt qua từng mục trong giỏ hàng
                        for (QueryDocumentSnapshot cartItem : task.getResult()) {
                            String medicineName = cartItem.getString("medicineName");
                            Long quantity = cartItem.getLong("quantity");

                            if (medicineName == null || quantity == null) {
                                Toast.makeText(getContext(), "Dữ liệu giỏ hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Truy vấn bảng medicine (thay vì Medicines)
                            db.collection("medicine")
                                    .whereEqualTo("name_Pro", medicineName)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            for (QueryDocumentSnapshot medicineDoc : queryDocumentSnapshots) {
                                                String amountStr = medicineDoc.getString("Amount");
                                                Long price = medicineDoc.getLong("Price");

                                                if (amountStr == null || price == null) {
                                                    Toast.makeText(getContext(), "Dữ liệu thuốc không hợp lệ: " + medicineName, Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                // Chuyển Amount từ string sang số
                                                int amountInStock;
                                                try {
                                                    amountInStock = Integer.parseInt(amountStr);
                                                } catch (NumberFormatException e) {
                                                    Toast.makeText(getContext(), "Số lượng thuốc không hợp lệ: " + medicineName, Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                // Kiểm tra số lượng trong kho
                                                if (amountInStock < quantity) {
                                                    Toast.makeText(getContext(), "Không đủ số lượng trong kho cho thuốc: " + medicineName, Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                // Tính tổng tiền
                                                totalAmount += price * quantity;

                                                // Tạo JSON object cho mỗi mục thuốc
                                                try {
                                                    JSONObject item = new JSONObject();
                                                    item.put("medicineName", medicineName);
                                                    item.put("price", price);
                                                    item.put("quantity", quantity);
                                                    orderItemsArray.put(item);
                                                } catch (Exception e) {
                                                    Toast.makeText(getContext(), "Lỗi khi tạo danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                // Cập nhật giao diện với số tiền
                                                transactionInfoText.setText("Nội dung chuyển khoản: " + transactionContent + "\nSố tiền: " + totalAmount + " VNĐ");

                                                // Tạo mã QR qua VietQR.io
                                                generateQRCode(accountNumber, totalAmount, transactionContent);

                                                // Gán sự kiện cho nút "Hoàn tất"
                                                completeButton.setOnClickListener(v -> completeOrder(orderItemsArray.toString()));
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Không tìm thấy thuốc trong kho: " + medicineName, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Lỗi khi truy vấn thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi truy vấn giỏ hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateQRCode(String accountNumber, long amount, String transactionContent) {
        new Thread(() -> {
            try {
                String urlString = "https://img.vietqr.io/image/" + bankName + "-" + accountNumber + "-compact.jpg?amount=" + amount + "&addInfo=" + transactionContent;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
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

    private void completeOrder(String orderItemsJson) {
        // Lưu đơn hàng vào Firestore với trạng thái "Pending"
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        WriteBatch batch = db.batch();
                        DocumentReference orderRef = db.collection("Orders").document();
                        orderId = orderRef.getId();

                        // Lưu đơn hàng vào Firestore
                        Map<String, Object> order = new HashMap<>();
                        order.put("orderId", orderId);
                        order.put("userId", userId);
                        order.put("fullName", fullName);
                        order.put("phoneNumber", phoneNumber);
                        order.put("address", address);
                        order.put("paymentMethod", "Bank Transfer");
                        order.put("transactionContent", transactionContent);
                        order.put("totalAmount", totalAmount);
                        order.put("status", "Pending");
                        order.put("timestamp", System.currentTimeMillis());

                        batch.set(orderRef, order);

                        // Xóa giỏ hàng sau khi đặt hàng
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đặt hàng thành công! Đang chờ xác nhận giao dịch...", Toast.LENGTH_SHORT).show();
                                    // Lập lịch kiểm tra giao dịch
                                    scheduleTransactionCheck(orderItemsJson);
                                    // Điều hướng về màn hình chính
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi đặt hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleTransactionCheck(String orderItemsJson) {
        // Gọi TransactionCheckWorker.scheduleTransactionCheck() với thông tin người dùng
        TransactionCheckWorker.scheduleTransactionCheck(
                getContext(),
                orderId,
                transactionContent,
                accountNumber,
                orderItemsJson,
                address,
                fullName,
                "Bank Transfer" // Phương thức thanh toán
        );
    }

    private void navigateToHome() {
        getActivity().getSupportFragmentManager().popBackStack(null, getActivity().getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new HomeFragment());
        transaction.commit();
    }
}