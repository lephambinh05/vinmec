package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout bookingLayout = view.findViewById(R.id.layout_booking);
        LinearLayout layoutBooking = view.findViewById(R.id.layoutBooking);
        LinearLayout layoutCustomerCare = view.findViewById(R.id.layoutCustomerCare);
        LinearLayout layoutContact = view.findViewById(R.id.layoutContact);
        LinearLayout layoutPrescription = view.findViewById(R.id.layoutPrescription);
        LinearLayout layoutLichHen = view.findViewById(R.id.layoutLichHen);

        layoutLichHen.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AppointmentActivity.class);
            intent.putExtra("fragment_type", "schedule");
            startActivity(intent);
        });

        // Mở trang đặt lịch hẹn
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

        // Mở trang chăm sóc khách hàng
        layoutCustomerCare.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CustomerCareActivity.class);
            startActivity(intent);
        });

        // Mở trang liên hệ
        layoutContact.setOnClickListener(v -> {
            BottomSheetContact bottomSheet = new BottomSheetContact(requireContext());
            bottomSheet.showBottomSheet();
        });

        // Khi nhấn vào "Đơn thuốc", mở HistoryPrescriptionFragment
        layoutPrescription.setOnClickListener(v -> openHistoryPrescriptionFragment());

        // Tìm LinearLayout của nút "Mua thuốc"
        LinearLayout layoutBuyMedicine = view.findViewById(R.id.layoutBuyMedicine);

        // Thêm sự kiện onClick để chuyển sang MedicineListFragment
        layoutBuyMedicine.setOnClickListener(v -> {
            MedicineListFragment medicineListFragment = new MedicineListFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, medicineListFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    private void openHistoryPrescriptionFragment() {
        Intent intent = new Intent(getActivity(), PrescriptionActivity.class);
        startActivity(intent);
    }
}