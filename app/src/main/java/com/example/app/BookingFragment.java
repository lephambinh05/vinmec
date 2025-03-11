package com.example.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingFragment extends Fragment {

    private EditText edtName, edtPhone, edtReason;
    private Button btnMale, btnFemale, btnBook;
    private String selectedDate;
    private FirebaseFirestore db;
    private String selectedGender = "Nam"; // Mặc định là Nam

    public BookingFragment() {
        // Constructor mặc định
    }

    // Nhận Firestore từ MainActivity
    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Ánh xạ view
        edtName = view.findViewById(R.id.editTextText);
        edtPhone = view.findViewById(R.id.editTextPhone);
        edtReason = view.findViewById(R.id.editTextReason);
        btnMale = view.findViewById(R.id.btnMale);
        btnFemale = view.findViewById(R.id.btnFemale);
        btnBook = view.findViewById(R.id.btnBook);

        // Xử lý chọn giới tính
        btnMale.setOnClickListener(v -> {
            selectedGender = "Nam";
            btnMale.setBackgroundColor(getResources().getColor(R.color.blue_sky));
            btnFemale.setBackgroundColor(getResources().getColor(R.color.gray));
        });

        btnFemale.setOnClickListener(v -> {
            selectedGender = "Nữ";
            btnFemale.setBackgroundColor(getResources().getColor(R.color.blue_sky));
            btnMale.setBackgroundColor(getResources().getColor(R.color.gray));
        });

        // Xử lý đặt hẹn
        btnBook.setOnClickListener(v -> saveAppointment());

        return view;
    }

    public void saveAppointment() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String reason = edtReason.getText().toString().trim();

        if (name.isEmpty() || selectedDate == null || phone.isEmpty() || reason.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        saveToFirestore(name, selectedDate, phone, reason);
    }

    private void saveToFirestore(String name, String date, String phone, String reason) {
        if (db == null) {
            showToast("Lỗi: Firestore chưa được khởi tạo!");
            return;
        }

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("name", name);
        appointment.put("date", date);
        appointment.put("phone", phone);
        appointment.put("reason", reason);
        appointment.put("gender", selectedGender);
        appointment.put("status", "Chờ xác nhận");

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    showToast("Đặt lịch thành công!");
                    resetFields();
                })
                .addOnFailureListener(e -> showToast("Lỗi khi đặt lịch: " + e.getLocalizedMessage()));
    }

    private void resetFields() {
        edtName.setText("");
        edtPhone.setText("");
        edtReason.setText("");
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
