package com.example.app.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.example.app.data.model.User;
import com.google.gson.Gson;

public class RegisterActivity extends AppCompatActivity {
    private Button buttonRegister;
    private Button btBack;
    private RadioGroup radioGroupGender;
    private EditText edMK, edNLMK, edMail, edSDT, edTDN;
    private SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    private final Gson gson=new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        // Ánh xạ các View
        anhxadulieu();
        //khai bao share pre
        sharedPreferences=getSharedPreferences(Utils.SHARE_PREFERENCES_APP, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        // lay du lieu
        anhxadulieu();
        taosukien();
    }
    void anhxadulieu(){
        // Ánh xạ các View
        btBack=findViewById(R.id.btBack);
        edMK = findViewById(R.id.edMK);
        edNLMK = findViewById(R.id.edNLMK);
        edMail = findViewById(R.id.edMail);
        edSDT = findViewById(R.id.edSDT);
        edTDN = findViewById(R.id.edTDN);
        buttonRegister = findViewById(R.id.buttonRegister);
        radioGroupGender = findViewById(R.id.radioGroupGender);
    }
    void taosukien(){
        buttonRegister.setOnClickListener(view ->sukienRegister() );
        btBack.setOnClickListener(view -> finish());
    }
    void sukienRegister(){
        String tdn = edTDN.getText().toString().trim();
        String mk = edMK.getText().toString().trim();
        String nlmk = edNLMK.getText().toString().trim();
        String mail = edMail.getText().toString().trim();
        String sdt = edSDT.getText().toString().trim();
        // neu gender=1 thi la nam con =0 thi la nu
        int sex=1;
        boolean isvalid=checkUserName(tdn)&&checkPassWord(mk,nlmk);
        // neu du lieu hop le, tao doi tuong user de luu vao share preference
        if (isvalid){
            User userNew=new User();
            userNew.setUsername(tdn);
            userNew.setPassword(mk);
            userNew.setEmail(mail);
            userNew.setPhone(sdt);
            // lay radio button ma ng dung tick
            int sexSelected=radioGroupGender.getCheckedRadioButtonId();
            if(sexSelected==R.id.radioButtonFemale){
                sex=0;
            }
            userNew.setSex(sex);
            //vì user là 1 object nên phải convert qua dạng string với format là gson để lưu trữ trong share preference (vì đã biến đổi thành file xml nên p chuyển thành string mới lưu trữ được
            String userString=gson.toJson(userNew);
            editor.putString(Utils.KEY_USER,userString);
            editor.commit();
            // dùng TOAST để thông báo đăng ký thành công
            Toast.makeText(RegisterActivity.this,"đăng ký tài khoản thành công",Toast.LENGTH_LONG).show();
            //finish regis activity
            finish();
        }
    }
    private boolean checkUserName(String tdn){
        if (tdn.isEmpty() ){
            edTDN.setError("vui lòng nhập tên đăng nhập");
            return false;
        }
        if (tdn.length()<=5){
            edTDN.setError("tên đăng nhập không đc dưới 5 kí tự");
            return false;
        }
        return true;
    }
    private boolean checkPassWord(String mk,String nlmk){
        if(mk.isEmpty()){
            edMK.setError("vui lòng nhập mật khẩu");
            return false;
        }
        if(mk.length()<=5){
            edMK.setError("mật khẩu k đc dưới 5 kí tự");
            return false;
        }
        if(!mk.equals(nlmk)){
            edNLMK.setError("xác nhận mật khẩu không khớp");
            return false;
        }
        return true;
    }
}