package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class BottomSheetContact {
    private Context context;
    private FirebaseFirestore db;
    private BottomSheetDialog myBottomSheet;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> hospitalList;

    public BottomSheetContact(Context context){
        this.context = context;
        this.db = FirebaseFirestore.getInstance();

    }
    public void showBottomSheet() {
        myBottomSheet = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_contact, null);
        myBottomSheet.setContentView(view);

        myBottomSheet.setCancelable(false);
        myBottomSheet.setCanceledOnTouchOutside(true);

        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
            layoutParams.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.8);
            parent.setLayoutParams(layoutParams);
        }

        listView = view.findViewById(R.id.listView);
        hospitalList = new ArrayList<>();
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, hospitalList);
        listView.setAdapter(adapter);

        // Sự kiện khi click vào item -> gọi điện
        listView.setOnItemClickListener((parentView, view1, position, id) -> {
            String selectedItem = hospitalList.get(position);
            String phoneNumber = selectedItem.substring(selectedItem.lastIndexOf(" - ") + 3);

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        });

        // Lấy dữ liệu từ Firestore
        loadHospitalData();

        myBottomSheet.show();
    }



    private void loadHospitalData() {
        db = FirebaseFirestore.getInstance();
        CollectionReference hospitalsRef = db.collection("Hospitals");

        hospitalsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                hospitalList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String address = document.getString("Address");
                    String phone = document.getString("phone");

                }
                adapter.notifyDataSetChanged();
            }
        });
    }

}