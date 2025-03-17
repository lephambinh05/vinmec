package com.example.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointmentList;

    public AppointmentAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.textName.setText(appointment.getName() != null ? appointment.getName() : "Không có tên");
        holder.textDate.setText("Ngày: " + (appointment.getDate() != null ? appointment.getDate() : "Chưa có ngày"));
        holder.textPhone.setText("SĐT: " + (appointment.getPhone() != null ? appointment.getPhone() : "Không có SĐT"));
        holder.txtGender.setText("Giới tính: " + (appointment.getGender() != null ? appointment.getGender() : "Chưa chon giới tính"));
        holder.txtReason.setText("Lí do: " + (appointment.getReason() != null ? appointment.getReason() : "Chưa có lí do"));
    }

    @Override
    public int getItemCount() {
        return (appointmentList != null) ? appointmentList.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDate, textPhone, txtGender,txtReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textDate = itemView.findViewById(R.id.textDate);
            textPhone = itemView.findViewById(R.id.textPhone);
            txtGender = itemView.findViewById(R.id.textGender);
            txtReason = itemView.findViewById(R.id.textReason);
        }
    }
}
