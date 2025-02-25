package com.example.app;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.util.Calendar;

public class BookingFragment extends Fragment {

    private EditText edtName, edtDate, edtPhone, edtReason;
    private Button btnMale, btnFemale, btnBook;
    private SQLiteDatabase db;
    private String selectedGender = "Nam";  // Mặc định

    public BookingFragment() {
        // Constructor mặc định
    }

    public void setDatabase(SQLiteDatabase database) {
        this.db = database;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Ánh xạ view
        edtName = view.findViewById(R.id.editTextText);
        edtDate = view.findViewById(R.id.editTextDate);
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

        // Chọn ngày sinh
        edtDate.setOnClickListener(v -> showDatePicker());

        // Xử lý đặt hẹn
        btnBook.setOnClickListener(v -> saveAppointment());

        return view;
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int year1, int month1, int day1) -> {
                    String date = day1 + "/" + (month1 + 1) + "/" + year1;
                    edtDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    public void saveAppointment() {
        if (db == null) {
            showToast("Lỗi: Database chưa được khởi tạo!");
            return;
        }

        String name = edtName.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String reason = edtReason.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || phone.isEmpty() || reason.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra xem bệnh nhân đã có tài khoản chưa
        int patientId = getPatientId(name, phone);
        if (patientId == -1) {
            showToast("Không tìm thấy bệnh nhân trong hệ thống!");
            return;
        }

        // Lấy bác sĩ đầu tiên trong danh sách (hoặc có thể chọn bác sĩ khác)
        int doctorId = getDoctorId();
        if (doctorId == -1) {
            showToast("Không có bác sĩ nào trong hệ thống!");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("patient_id", patientId);
        values.put("doctor_id", doctorId);
        values.put("appointment_date", date);
        values.put("status", "Chờ khám");
        values.put("notes", reason);

        long result = db.insert("appointments", null, values);

        if (result == -1) {
            showToast("Lỗi khi đặt lịch!");
        } else {
            showToast("Đặt lịch thành công!");
            resetFields();
        }
    }

    private int getPatientId(String name, String phone) {
        Cursor cursor = db.rawQuery("SELECT id FROM patients WHERE full_name = ? AND phone = ?", new String[]{name, phone});
        if (cursor.moveToFirst()) {
            int patientId = cursor.getInt(0);
            cursor.close();
            return patientId;
        }
        cursor.close();
        return -1;
    }

    private int getDoctorId() {
        Cursor cursor = db.rawQuery("SELECT id FROM doctors LIMIT 1", null);
        if (cursor.moveToFirst()) {
            int doctorId = cursor.getInt(0);
            cursor.close();
            return doctorId;
        }
        cursor.close();
        return -1;
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
