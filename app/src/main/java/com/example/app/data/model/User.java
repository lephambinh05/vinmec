package com.example.app.data.model;

public class User {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private int gender; // 1 = Nam, 0 = Nữ

    // Constructor mặc định (cần thiết cho Firestore)
    public User() {
    }

    // Constructor có tham số
    public User(String userId, String username, String email, String phone, int gender) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
    }

    // Getter và Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}