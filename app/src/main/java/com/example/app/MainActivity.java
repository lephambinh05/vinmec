package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Cấu hình Firestore (Bật cache offline)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.d("Firebase", "Firestore initialized successfully");

        // Thiết lập Bottom Navigation
        setupBottomNavigation();

        // Load màn hình chính khi ứng dụng mở
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load HomeFragment mặc định
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    loadFragment(new HomeFragment()); // Hiển thị HomeFragment khi chọn "Home"
                    return true;
                } else if (item.getItemId() == R.id.nav_account) {
                    openUserProfile(); // Mở UserProfileActivity khi chọn "Tài khoản"
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    loadFragment(new ProfileFragment()); // Mở UserProfileActivity khi chọn "Tài khoản"
                    return true;
                }
                return false;
            }
        });
    }


    private void openUserProfile() {
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
        startActivity(intent);
    }


}
