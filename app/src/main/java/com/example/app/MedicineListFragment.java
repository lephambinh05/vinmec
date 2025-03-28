package com.example.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MedicineListFragment extends Fragment {
    private LinearLayout medicineContainer;
    private FirebaseFirestore db;
    private Button historyButton, cartButton, searchButton;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine_list, container, false);

        db = FirebaseFirestore.getInstance();
        medicineContainer = view.findViewById(R.id.medicine_container);
        historyButton = view.findViewById(R.id.history_button);
        cartButton = view.findViewById(R.id.cart_button);
        searchButton = view.findViewById(R.id.search_button);
        searchEditText = view.findViewById(R.id.search_edit_text);

        historyButton.setOnClickListener(v -> {
            OrderHistoryFragment historyFragment = new OrderHistoryFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, historyFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        cartButton.setOnClickListener(v -> {
            CartFragment cartFragment = new CartFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, cartFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            loadMedicinesFromFirestore(searchQuery);
        });

        // Tải toàn bộ danh sách thuốc khi Fragment được tạo
        loadMedicinesFromFirestore("");

        return view;
    }

    private void loadMedicinesFromFirestore(String searchQuery) {
        Query query = db.collection("medicine");
        if (!searchQuery.isEmpty()) {
            String searchQueryLower = searchQuery.toLowerCase();
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    TextView noDataText = new TextView(getContext());
                    noDataText.setText("Không có thuốc nào.");
                    noDataText.setTextSize(16);
                    noDataText.setPadding(16, 16, 16, 16);
                    medicineContainer.addView(noDataText);
                    return;
                }

                medicineContainer.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name_Pro");
                    String img = document.getString("img");
                    String amount = document.getString("Amount");

                    Integer price = null;
                    Object priceObj = document.get("Price");
                    if (priceObj instanceof Number) {
                        price = ((Number) priceObj).intValue();
                    } else if (priceObj instanceof String) {
                        try {
                            price = Integer.parseInt((String) priceObj);
                        } catch (NumberFormatException e) {
                            Log.e("Firestore", "Price không thể chuyển thành số: " + priceObj);
                        }
                    }

                    if (name != null && price != null) {
                        if (searchQuery.isEmpty() || name.toLowerCase().contains(searchQuery.toLowerCase())) {
                            final String finalName = name;
                            final Integer finalPrice = price;
                            final String finalAmount = amount;
                            final String finalImg = img;

                            LinearLayout itemLayout = new LinearLayout(getContext());
                            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                            itemLayout.setPadding(16, 16, 16, 16);
                            itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);

                            ImageView medicineImage = new ImageView(getContext());
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, 100);
                            imageParams.setMargins(0, 0, 16, 0);
                            medicineImage.setLayoutParams(imageParams);
                            if (finalImg != null && !finalImg.isEmpty()) {
                                Glide.with(getContext())
                                        .load(finalImg)
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_menu_report_image)
                                        .into(medicineImage);
                            } else {
                                medicineImage.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                            itemLayout.addView(medicineImage);

                            TextView medicineText = new TextView(getContext());
                            medicineText.setText(finalName + "\nGiá: " + finalPrice + "\nSố lượng: " + finalAmount);
                            medicineText.setTextSize(16);
                            itemLayout.addView(medicineText);

                            itemLayout.setOnClickListener(v -> {
                                MedicineDetailFragment detailFragment = new MedicineDetailFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("name", finalName);
                                bundle.putInt("price", finalPrice);
                                bundle.putString("amount", finalAmount);
                                bundle.putString("img", finalImg);
                                detailFragment.setArguments(bundle);

                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, detailFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            });

                            medicineContainer.addView(itemLayout);
                        }
                    }
                }
            } else {
                Log.e("Firestore", "Lỗi tải danh sách thuốc", task.getException());
            }
        });
    }
}