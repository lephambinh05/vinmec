package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

//import com.example.app.ui.login.LoginFragment;
import com.example.app.BookingFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db; // Firestore Database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Button  btnOpenDrawer = findViewById(R.id.btnOpenDrawer);

        btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Cấu hình Firestore (Bật cache offline)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.d("Firebase", "Firestore initialized successfully");

        // Load màn hình đăng nhập khi ứng dụng mở
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
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
