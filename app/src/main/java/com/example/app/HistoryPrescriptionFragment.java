package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.adapters.PrescriptionAdapter;
import com.example.app.models.Prescription;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryPrescriptionFragment extends Fragment {
    private RecyclerView recyclerView;
    private PrescriptionAdapter adapter;
    private List<Prescription> prescriptionList;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private Button btnAddPrescription,btnBack;

    public HistoryPrescriptionFragment() {
        // Constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_prescription, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPrescriptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        prescriptionList = new ArrayList<>();
        adapter = new PrescriptionAdapter(getActivity(), prescriptionList);
        recyclerView.setAdapter(adapter);

        btnAddPrescription = view.findViewById(R.id.btnAddPrescription);
        btnAddPrescription.setOnClickListener(v -> {
            Fragment newFragment = new PrescriptionFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_Prescription, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());


        fetchPrescriptions();

        return view;
    }

    private void fetchPrescriptions() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getActivity(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();  // Lấy UID của người dùng hiện tại

        firestore.collection("don_thuoc")
                .whereEqualTo("userId", userId)  // Lọc đơn thuốc theo userId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    prescriptionList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Prescription prescription = document.toObject(Prescription.class);
                        prescriptionList.add(prescription);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Lỗi khi lấy đơn thuốc: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
