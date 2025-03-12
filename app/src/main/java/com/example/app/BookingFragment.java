package com.example.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingFragment extends Fragment {

    private EditText edtName, edtPhone, edtReason;
    private Button btnMale, btnFemale, btnBook;
    private Button btnToday, btnTomorrow, btnNextDay, btnOtherDate;
    private String selectedDate = null;
    private FirebaseFirestore db;
    private String selectedGender = "Nam"; // Mặc định là Nam

    public BookingFragment() {
        // Constructor mặc định
    }

    public void setFirestore(FirebaseFirestore db) {
        this.db = db;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        edtName = view.findViewById(R.id.editTextText);
        edtPhone = view.findViewById(R.id.editTextPhone);
        edtReason = view.findViewById(R.id.editTextReason);
        btnMale = view.findViewById(R.id.btnMale);
        btnFemale = view.findViewById(R.id.btnFemale);
        btnBook = view.findViewById(R.id.btnBook);

        btnToday = view.findViewById(R.id.tvSelectedDateToday);
        btnTomorrow = view.findViewById(R.id.tvSelectedDateTomorrow);
        btnNextDay = view.findViewById(R.id.tvSelectedDateNextDay);
        btnOtherDate = view.findViewById(R.id.tvSelectedDateOther);

        // Xử lý chọn ngày nhanh
        Calendar calendar = Calendar.getInstance();
        btnToday.setOnClickListener(v -> selectDate(calendar, 0));
        btnTomorrow.setOnClickListener(v -> selectDate(calendar, 1));
        btnNextDay.setOnClickListener(v -> selectDate(calendar, 2));

        btnOtherDate.setOnClickListener(v -> showDatePicker());

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

        btnBook.setOnClickListener(v -> saveAppointment());

        return view;
    }

    private void selectDate(Calendar calendar, int daysToAdd) {
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
        showToast("Ngày đã chọn: " + selectedDate);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    showToast("Ngày đã chọn: " + selectedDate);
                }, year, month, day);

        datePickerDialog.show();
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
        selectedDate = null;
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
