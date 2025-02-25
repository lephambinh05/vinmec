package com.example.app;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database; // Biến database toàn cục

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SQLiteDatabase
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String dbPath = dbHelper.getWritableDatabase().getPath(); // Bỏ comment và gán giá trị cho dbPath
        Log.d("Database Path", "Path: " + dbPath);
        database = dbHelper.getWritableDatabase();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load màn hình chính
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Để có thể quay lại fragment trước đó
        transaction.commit();
    }

    // Phương thức mở BookingFragment
    public void openBookingFragment() {
        BookingFragment bookingFragment = new BookingFragment();
        bookingFragment.setDatabase(database);
        loadFragment(bookingFragment);
    }

}
