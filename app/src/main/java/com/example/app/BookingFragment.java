package com.example.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    private EditText edtName, edtPhone, edtReason, edtBirthday;
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
        Log.d("Firestore", "Firestore instance set in BookingFragment: " + (db != null));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null) {
            db = FirebaseFirestore.getInstance();
            Log.d("Firestore", "Firestore initialized in BookingFragment: " + (db != null));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        edtName = view.findViewById(R.id.editTextText);
        edtPhone = view.findViewById(R.id.editTextPhone);
        edtReason = view.findViewById(R.id.editTextReason);
        edtBirthday = view.findViewById(R.id.editTextDate);

        btnMale = view.findViewById(R.id.btnMale);
        btnFemale = view.findViewById(R.id.btnFemale);
        btnBook = view.findViewById(R.id.btnBook);

        btnToday = view.findViewById(R.id.tvSelectedDateToday);
        btnTomorrow = view.findViewById(R.id.tvSelectedDateTomorrow);
        btnNextDay = view.findViewById(R.id.tvSelectedDateNextDay);
        btnOtherDate = view.findViewById(R.id.tvSelectedDateOther);

        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        updateButtonDates(calendar);

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

        // Auto-format ngày sinh
        edtBirthday.addTextChangedListener(new TextWatcher() {
            private boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String input = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i == 2 || i == 4) formatted.append("/");
                    formatted.append(input.charAt(i));
                }

                edtBirthday.setText(formatted.toString());
                edtBirthday.setSelection(formatted.length());

                isEditing = false;
            }
        });

        Log.d("Firestore", "Firestore instance in BookingFragment onCreateView: " + (db != null));

        return view;
    }

    private void updateButtonDates(Calendar calendar) {
        Calendar today = (Calendar) calendar.clone();
        Calendar tomorrow = (Calendar) calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar nextDay = (Calendar) calendar.clone();
        nextDay.add(Calendar.DAY_OF_MONTH, 2);

        btnToday.setText(formatDate(today));
        btnTomorrow.setText(formatDate(tomorrow));
        btnNextDay.setText(formatDate(nextDay));
    }

    private void selectDate(Calendar calendar, int daysToAdd) {
        Calendar selectedCalendar = (Calendar) calendar.clone();
        selectedCalendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        selectedDate = formatDate(selectedCalendar);
        showToast("Ngày đã chọn: " + selectedDate);

        btnToday.setBackgroundColor(getResources().getColor(R.color.gray));
        btnTomorrow.setBackgroundColor(getResources().getColor(R.color.gray));
        btnNextDay.setBackgroundColor(getResources().getColor(R.color.gray));
        btnOtherDate.setBackgroundColor(getResources().getColor(R.color.gray));

        if (daysToAdd == 0) {
            btnToday.setBackgroundColor(getResources().getColor(R.color.blue_sky));
        } else if (daysToAdd == 1) {
            btnTomorrow.setBackgroundColor(getResources().getColor(R.color.blue_sky));
        } else if (daysToAdd == 2) {
            btnNextDay.setBackgroundColor(getResources().getColor(R.color.blue_sky));
        }
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year1, month1, dayOfMonth);

                    if (selectedCalendar.before(calendar)) {
                        showToast("Không thể chọn ngày trong quá khứ!");
                    } else {
                        selectedDate = formatDate(selectedCalendar);
                        showToast("Ngày đã chọn: " + selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    public void saveAppointment() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String reason = edtReason.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();

        if (name.isEmpty() || selectedDate == null || phone.isEmpty() || reason.isEmpty() || birthday.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        saveToFirestore(name, birthday, selectedDate, phone, reason);
    }

    private void saveToFirestore(String name, String birthday, String date, String phone, String reason) {
        if (db == null) {
            showToast("Lỗi: Firestore chưa được khởi tạo!");
            db = FirebaseFirestore.getInstance();
            return;
        }

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("name", name);
        appointment.put("birthday", birthday);
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
        edtBirthday.setText("");
        selectedDate = null;
    }

    private String formatDate(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1);
    }


    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
