package com.example.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AppointmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        String fragmentType = getIntent().getStringExtra("fragment_type");

        if (fragmentType != null) {
            if (fragmentType.equals("booking")) {
                showBookingFragment();
            } else if (fragmentType.equals("quick_booking")) {
                showQuickBookingFragment();
            }
        }
    }

    private void showBookingFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_Appointment, new NoAppointmentFragment())
                .commit();
    }

    // Phương thức hiển thị trang quick booking
    private void showQuickBookingFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_Appointment, new BookingFragment())
                .commit();
    }

}