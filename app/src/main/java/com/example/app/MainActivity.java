package com.example.app;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Sử dụng Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Cấu hình Firestore nếu cần (chỉ dùng khi muốn tắt cache hoặc bật chế độ offline)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Cho phép lưu cache offline
                .build();
        db.setFirestoreSettings(settings);

        Log.d("Firebase", "Firestore initialized successfully");

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load màn hình chính
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Phương thức mở BookingFragment với Firestore
    public void openBookingFragment() {
        BookingFragment bookingFragment = new BookingFragment();
        bookingFragment.setFirestore(db); // Truyền Firestore vào Fragment
        loadFragment(bookingFragment);
    }

}
