package com.example.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class AppointmentScheduleFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private FirebaseFirestore db;
    private Button btnBack;
    private FirebaseUser currentUser;
    public AppointmentScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appointmentList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_schedule, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());



        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadAppointments();
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch hẹn", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadAppointments() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Log.d("FirestoreCheck", "🔍 Truy vấn lịch hẹn cho userId: " + userId);

        CollectionReference appointmentsRef = db.collection("appointments");
        appointmentsRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("FirestoreCheck", "📡 Truy vấn thành công, số lượng kết quả: " + queryDocumentSnapshots.size());

                    appointmentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Appointment appointment = document.toObject(Appointment.class);
                        appointment.setId(document.getId()); // Lưu ID Firestore vào đối tượng
                        Log.d("FirestoreCheck", "📄 Lịch hẹn: " + document.getId() + " - " + document.getData());
                        appointmentList.add(appointment);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreCheck", "🔥 Lỗi truy vấn Firestore", e);
                    Toast.makeText(getContext(), "Lỗi tải lịch hẹn", Toast.LENGTH_SHORT).show();
                });
    }
}
