package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MedicineDetailFragment extends Fragment {
    private TextView nameText, priceText, amountText;
    private ImageView imageView;
    private Button addToCartButton;
    private FirebaseFirestore db;
    private String medicineName;
    private int price;
    private String img;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine_detail, container, false);

        db = FirebaseFirestore.getInstance();
        nameText = view.findViewById(R.id.detail_name);
        priceText = view.findViewById(R.id.detail_price);
        amountText = view.findViewById(R.id.detail_amount);
        imageView = view.findViewById(R.id.detail_image);
        addToCartButton = view.findViewById(R.id.add_to_cart_button);

        // Lấy userId từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            return view;
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            medicineName = bundle.getString("name");
            price = bundle.getInt("price");
            String amount = bundle.getString("amount");
            img = bundle.getString("img");

            nameText.setText(medicineName);
            priceText.setText(String.valueOf(price));
            amountText.setText("Số lượng: " + amount);

            if (img != null && !img.isEmpty()) {
                Glide.with(getContext())
                        .load(img)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        addToCartButton.setOnClickListener(v -> addToCart());

        return view;
    }

    private void addToCart() {
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("medicineName", medicineName);
        cartItem.put("price", price);
        cartItem.put("quantity", 1);
        cartItem.put("img", img);
        cartItem.put("userId", userId);

        db.collection("Cart").document(userId).collection("Items").add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}