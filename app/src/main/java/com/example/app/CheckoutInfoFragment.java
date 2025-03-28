package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class CheckoutInfoFragment extends Fragment {
    private EditText fullNameEditText, phoneNumberEditText, addressEditText;
    private RadioGroup paymentMethodRadioGroup;
    private RadioButton bankTransferRadio, codRadio;
    private Button confirmButton;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout_info, container, false);

        db = FirebaseFirestore.getInstance();
        fullNameEditText = view.findViewById(R.id.full_name_edit_text);
        phoneNumberEditText = view.findViewById(R.id.phone_number_edit_text);
        addressEditText = view.findViewById(R.id.address_edit_text);
        paymentMethodRadioGroup = view.findViewById(R.id.payment_method_radio_group);
        bankTransferRadio = view.findViewById(R.id.bank_transfer_radio);
        codRadio = view.findViewById(R.id.cod_radio);
        confirmButton = view.findViewById(R.id.confirm_button);

        // Lấy userId từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            return view;
        }

        confirmButton.setOnClickListener(v -> confirmCheckout());

        return view;
    }

    private void confirmCheckout() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Kiểm tra thông tin nhập vào
        if (fullName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu thông tin giao dịch vào Bundle để truyền sang Fragment tiếp theo
        Bundle bundle = new Bundle();
        bundle.putString("fullName", fullName);
        bundle.putString("phoneNumber", phoneNumber);
        bundle.putString("address", address);

        // Kiểm tra phương thức thanh toán
        if (bankTransferRadio.isChecked()) {
            // Chuyển sang màn hình hiển thị mã QR
            BankTransferFragment bankTransferFragment = new BankTransferFragment();
            bankTransferFragment.setArguments(bundle);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, bankTransferFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (codRadio.isChecked()) {
            // Xử lý Ship COD: Lưu đơn hàng và thông báo thành công
            saveOrderAndFinish(bundle, "COD");
        }
    }

    private void saveOrderAndFinish(Bundle bundle, String paymentMethod) {
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
                                order.put("fullName", bundle.getString("fullName"));
                                order.put("phoneNumber", bundle.getString("phoneNumber"));
                                order.put("address", bundle.getString("address"));
                                order.put("paymentMethod", paymentMethod);
                                order.put("timestamp", System.currentTimeMillis());

                                DocumentReference orderRef = db.collection("Orders").document();
                                batch.set(orderRef, order);
                                batch.delete(document.getReference());
                            }
                        }

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đặt hàng thành công! Phương thức: " + paymentMethod, Toast.LENGTH_SHORT).show();
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