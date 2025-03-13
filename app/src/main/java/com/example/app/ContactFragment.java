package com.example.app; // Đảm bảo đây là tên package đúng của ứng dụng bạn

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ContactFragment extends Fragment {
    private ListView listView;
    private ArrayList<String> hospitalNames;
    private Map<String, String> hospitalPhones;
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        listView = view.findViewById(R.id.listView);
        db = FirebaseFirestore.getInstance();

        hospitalNames = new ArrayList<>();
        hospitalPhones = new HashMap<>();
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, hospitalNames);
        listView.setAdapter(adapter);

        loadHospitalsFromFirestore();

        // Xử lý khi click vào bệnh viện
        listView.setOnItemClickListener((AdapterView<?> parent, View view1, int position, long id) -> {
            String hospitalName = hospitalNames.get(position);
            String phoneNumber = hospitalPhones.get(hospitalName);

            if (phoneNumber != null) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy số điện thoại!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadHospitalsFromFirestore() {
        db.collection("Hospitals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                hospitalNames.clear();
                hospitalPhones.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    String phone = document.getString("phone");
                    if (name != null && phone != null) {
                        hospitalNames.add(name);
                        hospitalPhones.put(name, phone);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
