package com.example.app;

public class Appointment {
    private String title; // Thêm thuộc tính title
    private String date;
    private String time;
    private String userId;

    // Constructor mặc định cần thiết cho Firebase
    public Appointment() {}

    public Appointment(String title, String date, String time, String userId) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUserId() {
        return userId;
    }
}
