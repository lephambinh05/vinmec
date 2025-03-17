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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryPrescriptionFragment extends Fragment {
    private RecyclerView recyclerView;
    private PrescriptionAdapter adapter;
    private List<Prescription> prescriptionList;
    private FirebaseFirestore firestore;
    private Button btnAddPrescription;

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
        prescriptionList = new ArrayList<>();
        adapter = new PrescriptionAdapter(getActivity(), prescriptionList);
        recyclerView.setAdapter(adapter);

        btnAddPrescription = view.findViewById(R.id.btnAddPrescription);
        btnAddPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new PrescriptionFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_Prescription, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        fetchPrescriptions();

        return view;
    }

    private void fetchPrescriptions() {
        firestore.collection("don_thuoc")
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
