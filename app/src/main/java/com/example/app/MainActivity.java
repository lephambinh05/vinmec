package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.example.app.BookingFragment;
import com.example.app.data.model.User;
import com.example.app.ui.login.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.example.app.AccountFragment;
import com.example.app.BookingFragment;
import com.example.app.NotificationFragment;
import com.example.app.HomeFragment;

import com.example.app.ui.login.LoginActivity;


import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private static final int PICK_IMAGE_REQUEST = 2;
    private ImageView imgAvatar;
    private Button btnChangeAvatar;
    private String avatarPath;
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


        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Lấy user từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.SHARE_PREFERENCES_APP, Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString(Utils.KEY_USER, null);
        if (userJson != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            updateNavHeader(user);
        }

        // Khởi tạo Navigation Drawer
        initMenu();
    }
    private void updateNavHeader(User user) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView tvUsername = headerView.findViewById(R.id.tv_user_name);
        TextView tvEmailPhone = headerView.findViewById(R.id.tv_email);
        imgAvatar = headerView.findViewById(R.id.iv_Avatar);
        btnChangeAvatar = headerView.findViewById(R.id.btnChangeAvatar);

        // Cập nhật dữ liệu từ User
        tvUsername.setText(user.getUsername());
        tvEmailPhone.setText(user.getEmail() != null ? user.getEmail() : "No email provided");

        // Hiển thị ảnh avatar nếu có, nếu không hiển thị ảnh mặc định
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            imgAvatar.setImageURI(Uri.parse(user.getAvatarUrl()));
        } else {
            imgAvatar.setImageResource(R.drawable.logolab2);
        }

        // Xử lý sự kiện đổi ảnh
        btnChangeAvatar.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                avatarPath = selectedImageUri.toString(); // Lưu đường dẫn ảnh mới
                imgAvatar.setImageURI(selectedImageUri); // Hiển thị ảnh lên ImageView

                // Cập nhật vào SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(Utils.SHARE_PREFERENCES_APP, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                String userJson = sharedPreferences.getString(Utils.KEY_USER, null);
                if (userJson != null) {
                    Gson gson = new Gson();
                    User user = gson.fromJson(userJson, User.class);
                    user.setAvatarUrl(avatarPath);

                    // Lưu lại thông tin user đã cập nhật
                    String updatedUserJson = gson.toJson(user);
                    editor.putString(Utils.KEY_USER, updatedUserJson);
                    editor.apply();
                }
            }
        }
    }

    void initMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Drawer");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fmNew = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    fmNew = new HomeFragment();
                } else if (id == R.id.nav_image) {
                    fmNew = new NotificationFragment();
                } else if (id == R.id.nav_contact) {
                    fmNew = new AccountFragment();
                } else if (id == R.id.nav_favorite) {
                    fmNew = new ProfileFragment();
                } else if (id == R.id.nav_logout) {
                    showLogoutDialog(); // Gọi hàm hiển thị xác nhận đăng xuất
                    return true;
                }

                if (fmNew != null) {
                    loadFragmentlogin(fmNew);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout(); // Gọi hàm logout
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Chỉ chuyển về màn hình đăng nhập mà không xóa dữ liệu SharedPreferences
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    void loadFragmentlogin(Fragment fmNew) {
        FragmentTransaction fmTran = getSupportFragmentManager().beginTransaction();
        fmTran.replace(R.id.fragment_container, fmNew);
        fmTran.addToBackStack(null);
        fmTran.commit();
        drawerLayout.closeDrawer(GravityCompat.START);
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
