package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.R;
import com.example.app.models.Prescription;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {
    private Context context;
    private List<Prescription> prescriptionList;

    public PrescriptionAdapter(Context context, List<Prescription> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prescription prescription = prescriptionList.get(position);
        holder.txtHoTen.setText("Họ tên: " + prescription.getHoTen());
        holder.txtSoDienThoai.setText("SĐT: " + prescription.getSoDienThoai());
        holder.txtGhiChu.setText("Ghi chú: " + prescription.getGhiChu());

        // Chuyển timestamp thành ngày giờ đọc được
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.txtTimestamp.setText("Ngày tạo: " + sdf.format(prescription.getTimestamp()));

        // Load ảnh từ Firebase Storage
        Glide.with(context).load(prescription.getAnhDonThuoc()).into(holder.imgDonThuoc);
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtHoTen, txtSoDienThoai, txtGhiChu, txtTimestamp;
        ImageView imgDonThuoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHoTen = itemView.findViewById(R.id.txtHoTen);
            txtSoDienThoai = itemView.findViewById(R.id.txtSoDienThoai);
            txtGhiChu = itemView.findViewById(R.id.txtGhiChu);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            imgDonThuoc = itemView.findViewById(R.id.imgDonThuoc);
        }
    }
}
