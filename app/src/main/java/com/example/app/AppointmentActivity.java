package com.example.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AppointmentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String fragmentType = getIntent().getStringExtra("fragment_type");

        if (fragmentType != null) {
            switch (fragmentType) {
                case "booking":
                case "quick_booking":
                    showFragment(new BookingFragment());
                    break;
                case "schedule":
                    checkAppointments();
                    break;
            }
        }
    }

    private void checkAppointments() {
        if (auth.getCurrentUser() == null) {
            showFragment(new NoAppointmentFragment());
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            showFragment(new AppointmentScheduleFragment());
                        } else {
                            showFragment(new NoAppointmentFragment());
                        }
                    } else {
                        showFragment(new NoAppointmentFragment());
                    }
                });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_Appointment, fragment)
                .commit();
    }
}
