package com.example.app.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app.MainActivity;
import com.example.app.R;
import com.example.app.data.model.User;
import com.google.gson.Gson;


public class LoginActivity extends AppCompatActivity {


    EditText edUser,edPass;
    Button btRegis,btLogin;
    SharedPreferences.Editor editor;
    private final Gson gson=new Gson();
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        //anh xa
        anhxa();
        sharedPreferences = getSharedPreferences(Utils.SHARE_PREFERENCES_APP, Context.MODE_PRIVATE);
        //
        taosukien();

    }
    private void taosukien(){
        btLogin.setOnClickListener(view -> checkUserLogin());
        btRegis.setOnClickListener(funRegister());
    }

    @NonNull
    private View.OnClickListener funRegister() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        };
    }

    private void checkUserLogin(){
        String userPref=sharedPreferences.getString(Utils.KEY_USER,null);
        User user=gson.fromJson(userPref, User.class);
        //user= null la chua co nguoi dang ki
        if(user==null){
            return;
        }
        //ktra username và pass có trùng với đối tượng user trong preference không
        boolean isvalid=edUser.getText().toString().trim().equals(user.getUsername())&&edPass.getText().toString().trim().equals(user.getPassword());
        if(isvalid){
            Intent intent=new Intent(this, MainActivity.class);
            //khởi tạo bundle để truyền dữ liệu user sang mainactivity
            Bundle bundle=new Bundle();
            //vì user là object nên dùng putSerializable
            bundle.putSerializable(Utils.KEY_USER_PROFILE,user);
            //có thể dùng putString nếu chỉ truyền mỗi username.
            //bundle.putString(Utils.KEY_USER_PROFILE, user.getUsername());

            //put bundle vào intent
            intent.putExtras(bundle);
            startActivity(intent);
            //sau khi start thì finish
            finish();

        }
    }

    private void anhxa(){
        edUser=findViewById(R.id.edUser);
        edPass=findViewById(R.id.edPass);
        btLogin=findViewById(R.id.btLogin);
        btRegis=findViewById(R.id.btRegis);
    }

}