package com.example.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class AppointmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment);

        // Lấy dữ liệu từ Intent để xác định Fragment cần mở
        String fragmentType = getIntent().getStringExtra("fragment_type");

        Fragment fragment;
        if ("quick_booking".equals(fragmentType)) {
            fragment = new BookingFragment(); // Nếu click vào layoutBooking
        } else {
            fragment = new NoAppointmentFragment(); // Mặc định
        }

        // Hiển thị Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}