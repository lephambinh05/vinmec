package com.example.app;

import android.content.Intent;
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


        LinearLayout bookingLayout = view.findViewById(R.id.layout_booking);
        LinearLayout layoutBooking = view.findViewById(R.id.layoutBooking);

        bookingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AppointmentActivity.class);
            intent.putExtra("fragment_type", "booking");
            startActivity(intent);
        });

        layoutBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AppointmentActivity.class);
            intent.putExtra("fragment_type", "quick_booking");
            startActivity(intent);
        });


        LinearLayout layoutCustomerCare = view.findViewById(R.id.layoutCustomerCare);
        layoutCustomerCare.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomerCareActivity.class);
            startActivity(intent);
        });
        LinearLayout layoutContact = view.findViewById(R.id.layoutContact);
        layoutContact.setOnClickListener(v -> {
            BottomSheetContact bottomSheet = new BottomSheetContact(requireContext());
            bottomSheet.showBottomSheet();
        });
        return view;
    }
}
