package com.example.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "vinmec.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL CHECK (role IN ('patient', 'doctor', 'admin'))" +
                ");");

        db.execSQL("CREATE TABLE patients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "full_name TEXT NOT NULL, " +
                "date_of_birth TEXT, " +
                "gender TEXT CHECK (gender IN ('Nam', 'Nữ', 'Khác')), " +
                "phone TEXT, " +
                "address TEXT, " +
                "blood_type TEXT CHECK (blood_type IN ('A', 'B', 'AB', 'O')), " +
                "insurance_number TEXT UNIQUE, " +
                "FOREIGN KEY (user_id) REFERENCES users (id)" +
                ");");

        db.execSQL("CREATE TABLE doctors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "full_name TEXT NOT NULL, " +
                "specialization TEXT NOT NULL, " +
                "phone TEXT, " +
                "address TEXT, " +
                "license_number TEXT UNIQUE, " +
                "FOREIGN KEY (user_id) REFERENCES users (id)" +
                ");");

        db.execSQL("CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "doctor_id INTEGER NOT NULL, " +
                "appointment_date TEXT NOT NULL, " +
                "status TEXT DEFAULT 'Chờ khám' CHECK (status IN ('Chờ khám', 'Đã khám', 'Hủy bỏ')), " +
                "notes TEXT, " +
                "FOREIGN KEY (patient_id) REFERENCES patients(id), " +
                "FOREIGN KEY (doctor_id) REFERENCES doctors(id)" +
                ");");

        db.execSQL("CREATE TABLE medical_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "doctor_id INTEGER NOT NULL, " +
                "appointment_id INTEGER UNIQUE, " +
                "diagnosis TEXT NOT NULL, " +
                "treatment TEXT, " +
                "created_at TEXT DEFAULT '', " +  // Không dùng CURRENT_TIMESTAMP
                "FOREIGN KEY (patient_id) REFERENCES patients (id), " +
                "FOREIGN KEY (doctor_id) REFERENCES doctors (id), " +
                "FOREIGN KEY (appointment_id) REFERENCES appointments (id)" +
                ");");

        db.execSQL("CREATE TABLE medicine (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "usage TEXT" +
                ");");

        db.execSQL("CREATE TABLE prescriptions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medical_record_id INTEGER NOT NULL, " +
                "medicine_id INTEGER NOT NULL, " +
                "dosage TEXT NOT NULL, " +
                "duration TEXT NOT NULL, " +
                "FOREIGN KEY (medical_record_id) REFERENCES medical_records (id), " +
                "FOREIGN KEY (medicine_id) REFERENCES medicine (id)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS patients");
        db.execSQL("DROP TABLE IF EXISTS doctors");
        db.execSQL("DROP TABLE IF EXISTS appointments");
        db.execSQL("DROP TABLE IF EXISTS medical_records");
        db.execSQL("DROP TABLE IF EXISTS medicine");
        db.execSQL("DROP TABLE IF EXISTS prescriptions");
        onCreate(db);
    }
}
