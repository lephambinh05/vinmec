package com.example.app.models;

public class Prescription {
    private String hoTen, soDienThoai, ghiChu, anhDonThuoc;
    private long timestamp;

    public Prescription() {
        // Constructor mặc định cho Firestore
    }

    public Prescription(String hoTen, String soDienThoai, String ghiChu, String anhDonThuoc, long timestamp) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.ghiChu = ghiChu;
        this.anhDonThuoc = anhDonThuoc;
        this.timestamp = timestamp;
    }

    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getGhiChu() { return ghiChu; }
    public String getAnhDonThuoc() { return anhDonThuoc; }
    public long getTimestamp() { return timestamp; }
}
