package com.example.app;

public class Appointment {
    private String id;
    private String date;
    private String gender;
    private String name;
    private String phone;
    private String reason;
    private String userId; // Thêm userId

    public Appointment() {}

    public Appointment(String date, String gender, String name, String phone, String reason, String userId) {
        this.date = date;
        this.gender = gender;
        this.name = name;
        this.phone = phone;
        this.reason = reason;
        this.userId = userId; // Gán giá trị userId
    }

    public String getDate() { return date; }
    public String getGender() { return gender; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getReason() { return reason; }
    public String getUserId() { return userId; } // Getter cho userId

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
