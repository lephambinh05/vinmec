package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.Button;
import android.widget.LinearLayout;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Tìm LinearLayout (hoặc nút) để bắt sự kiện click
        LinearLayout bookingLayout = view.findViewById(R.id.layout_booking);

        bookingLayout.setOnClickListener(v -> {
            // Chuyển đến FragmentBooking
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new BookingFragment());
            transaction.addToBackStack(null); // Quay lại được Fragment trước đó
            transaction.commit();
        });

        LinearLayout layoutContact = view.findViewById(R.id.layoutContact);
        layoutContact.setOnClickListener(v -> {
            BottomSheetContact bottomSheet = new BottomSheetContact(requireContext());
            bottomSheet.showBottomSheet();
        });
        return view;
    }
}
