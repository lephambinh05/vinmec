package com.example.app;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.example.app.ui.login.LoginFragment;
import com.example.app.HomeFragment;
import com.example.app.BookingFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore Database
    private FirebaseAuth mAuth; // Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Cấu hình Firestore (Bật cache offline)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.d("Firebase", "Firestore initialized successfully");

        // Kiểm tra trạng thái đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (savedInstanceState == null) {
            if (currentUser != null) {
                Log.d("Auth", "User is logged in: " + currentUser.getEmail());
                loadFragment(new HomeFragment()); // Nếu đã đăng nhập, mở HomeFragment
            } else {
                Log.d("Auth", "User is NOT logged in");
                loadFragment(new LoginFragment()); // Nếu chưa đăng nhập, mở LoginFragment
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    // Mở BookingFragment
    public void openBookingFragment() {
        BookingFragment bookingFragment = new BookingFragment();
        bookingFragment.setFirestore(db); // Truyền Firestore vào Fragment
        loadFragment(bookingFragment);
    }
}
