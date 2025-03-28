package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OrderHistoryFragment extends Fragment {
    private LinearLayout orderContainer;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        db = FirebaseFirestore.getInstance();
        orderContainer = view.findViewById(R.id.order_container);

        // Lấy userId từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            TextView noDataText = new TextView(getContext());
            noDataText.setText("Vui lòng đăng nhập để xem lịch sử đơn hàng.");
            noDataText.setTextSize(16);
            noDataText.setPadding(16, 16, 16, 16);
            orderContainer.addView(noDataText);
            return view;
        }

        loadOrderHistory();

        return view;
    }

    private void loadOrderHistory() {
        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            TextView noDataText = new TextView(getContext());
                            noDataText.setText("Không có đơn hàng nào.");
                            noDataText.setTextSize(16);
                            noDataText.setPadding(16, 16, 16, 16);
                            orderContainer.addView(noDataText);
                            return;
                        }

                        orderContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String medicineName = document.getString("medicineName");
                            Long price = document.getLong("price");
                            String userId = document.getString("userId");
                            Long timestamp = document.getLong("timestamp");

                            if (medicineName != null && price != null) {
                                TextView orderText = new TextView(getContext());
                                orderText.setText("Thuốc: " + medicineName + "\nGiá: " + price + "\nUser: " + userId + "\nThời gian: " + (timestamp != null ? new java.util.Date(timestamp).toString() : "N/A"));
                                orderText.setTextSize(16);
                                orderText.setPadding(16, 16, 16, 16);
                                orderText.setBackgroundResource(android.R.drawable.list_selector_background);

                                orderContainer.addView(orderText);
                            }
                        }
                    } else {
                        Log.e("Firestore", "Lỗi tải lịch sử đơn hàng", task.getException());
                    }
                });
    }
}