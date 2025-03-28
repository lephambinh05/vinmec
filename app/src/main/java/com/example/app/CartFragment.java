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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class CartFragment extends Fragment {
    private LinearLayout cartContainer;
    private FirebaseFirestore db;
    private Button checkoutButton, searchButton;
    private EditText searchEditText;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        db = FirebaseFirestore.getInstance();
        cartContainer = view.findViewById(R.id.cart_container);
        checkoutButton = view.findViewById(R.id.checkout_button);
        searchButton = view.findViewById(R.id.search_button);
        searchEditText = view.findViewById(R.id.search_edit_text);

        // Lấy userId từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            return view;
        }

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            loadCartItems(searchQuery);
        });

        checkoutButton.setOnClickListener(v -> checkout());

        loadCartItems("");

        return view;
    }

    private void loadCartItems(String searchQuery) {
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            TextView noDataText = new TextView(getContext());
                            noDataText.setText("Giỏ hàng trống.");
                            noDataText.setTextSize(16);
                            noDataText.setPadding(16, 16, 16, 16);
                            cartContainer.addView(noDataText);
                            return;
                        }

                        cartContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String medicineName = document.getString("medicineName");
                            Long price = document.getLong("price");
                            Long quantity = document.getLong("quantity");
                            String img = document.getString("img");

                            if (medicineName != null && price != null && quantity != null) {
                                if (searchQuery.isEmpty() || medicineName.toLowerCase().contains(searchQuery.toLowerCase())) {
                                    LinearLayout itemLayout = new LinearLayout(getContext());
                                    itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    itemLayout.setPadding(16, 16, 16, 16);
                                    itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);

                                    ImageView cartImage = new ImageView(getContext());
                                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, 100);
                                    imageParams.setMargins(0, 0, 16, 0);
                                    cartImage.setLayoutParams(imageParams);
                                    if (img != null && !img.isEmpty()) {
                                        Glide.with(getContext())
                                                .load(img)
                                                .placeholder(android.R.drawable.ic_menu_gallery)
                                                .error(android.R.drawable.ic_menu_report_image)
                                                .into(cartImage);
                                    } else {
                                        cartImage.setImageResource(android.R.drawable.ic_menu_gallery);
                                    }
                                    itemLayout.addView(cartImage);

                                    TextView cartText = new TextView(getContext());
                                    cartText.setText(medicineName + "\nGiá: " + price + "\nSố lượng: " + quantity);
                                    cartText.setTextSize(16);
                                    cartText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                    itemLayout.addView(cartText);

                                    Button deleteButton = new Button(getContext());
                                    deleteButton.setText("Xóa");
                                    deleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    deleteButton.setOnClickListener(v -> {
                                        document.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getContext(), "Đã xóa sản phẩm khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                                                    loadCartItems(searchQuery);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    });
                                    itemLayout.addView(deleteButton);

                                    cartContainer.addView(itemLayout);
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "Lỗi tải giỏ hàng", task.getException());
                    }
                });
    }

    private void checkout() {
        db.collection("Cart").document(userId).collection("Items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Chuyển sang màn hình nhập thông tin giao dịch
                        CheckoutInfoFragment checkoutInfoFragment = new CheckoutInfoFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, checkoutInfoFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi kiểm tra giỏ hàng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}