package com.example.app;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Cấu hình Firestore (Bật cache offline)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.d("Firebase", "Firestore initialized successfully");

        // Load màn hình đăng nhập khi ứng dụng mở
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return; // Không load lại nếu fragment đã mở
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // Mở BookingFragment
    public void openBookingFragment() {
        BookingFragment bookingFragment = new BookingFragment();
        bookingFragment.setFirestore(db); // Truyền Firestore vào Fragment
        loadFragment(bookingFragment, true);
    }
}
