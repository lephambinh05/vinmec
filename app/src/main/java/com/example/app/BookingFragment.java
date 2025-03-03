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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingFragment extends Fragment {

    private EditText edtName, edtDate, edtPhone, edtReason;
    private Button btnMale, btnFemale, btnBook;
    private String selectedDate;
    private FirebaseFirestore firestoreDb;
    private String selectedGender = "Nam";  // Mặc định

    public BookingFragment() {
        // Constructor mặc định
    }
    public void setFirestore(FirebaseFirestore firestore) {
        this.firestoreDb = firestore;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Khởi tạo Firestore
        firestoreDb = FirebaseFirestore.getInstance();

        // Ánh xạ view
        edtName = view.findViewById(R.id.editTextText);
        edtPhone = view.findViewById(R.id.editTextPhone);
        edtReason = view.findViewById(R.id.editTextReason);
        btnMale = view.findViewById(R.id.btnMale);
        btnFemale = view.findViewById(R.id.btnFemale);
        btnBook = view.findViewById(R.id.btnBook);

        // Ánh xạ các nút chọn ngày
        Button btnToday = view.findViewById(R.id.tvSelectedDateToday);
        Button btnTomorrow = view.findViewById(R.id.tvSelectedDateTomorrow);
        Button btnNextDay = view.findViewById(R.id.tvSelectedDateNextDay);
        Button btnOtherDate = view.findViewById(R.id.tvSelectedDateOther);

        // Mặc định chọn ngày hôm nay
        selectedDate = btnToday.getText().toString();
        btnToday.setBackgroundColor(getResources().getColor(R.color.blue_sky));

        // Xử lý chọn ngày khám mong muốn
        View.OnClickListener dateClickListener = v -> {
            resetDateButtons(btnToday, btnTomorrow, btnNextDay, btnOtherDate);
            Button clickedButton = (Button) v;
            selectedDate = clickedButton.getText().toString();
            clickedButton.setBackgroundColor(getResources().getColor(R.color.blue_sky));

            // Nếu bấm vào "Ngày khác", mở DatePicker
            if (v.getId() == R.id.tvSelectedDateOther) {
                showDatePicker(clickedButton);
            }
        };

        btnToday.setOnClickListener(dateClickListener);
        btnTomorrow.setOnClickListener(dateClickListener);
        btnNextDay.setOnClickListener(dateClickListener);
        btnOtherDate.setOnClickListener(dateClickListener);

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
    private void resetDateButtons(Button... buttons) {
        for (Button button : buttons) {
            button.setBackgroundColor(getResources().getColor(R.color.gray));
        }
    }


    private void showDatePicker(Button targetButton) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, month1, day1) -> {
                    String date = day1 + "/" + (month1 + 1) + "/" + year1;
                    targetButton.setText(date);
                    selectedDate = date; // Lưu ngày đã chọn
                }, year, month, day);
        datePickerDialog.show();
    }


    public void saveAppointment() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String reason = edtReason.getText().toString().trim();

        if (name.isEmpty() || selectedDate.isEmpty() || phone.isEmpty() || reason.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        saveToFirestore(name, selectedDate, phone, reason);
    }


    private void saveToFirestore(String name, String date, String phone, String reason) {
        if (firestoreDb == null) {
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

        firestoreDb.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    showToast("Đặt lịch thành công!");
                    resetFields();
                })
                .addOnFailureListener(e -> showToast("Lỗi khi đặt lịch: " + e.getLocalizedMessage()));
    }

    private void resetFields() {
        edtName.setText("");
        edtDate.setText("");
        edtPhone.setText("");
        edtReason.setText("");
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
