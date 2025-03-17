package com.example.app.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterFragment extends Fragment {

    private EditText edTDN, edMK, edNLMK, edMail, edSDT;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button buttonRegister, btBack;

    private FirebaseAuth mAuth; // Firebase Authentication

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        edTDN = view.findViewById(R.id.edTDN);
        edMK = view.findViewById(R.id.edMK);
        edNLMK = view.findViewById(R.id.edNLMK);
        edMail = view.findViewById(R.id.edMail);
        edSDT = view.findViewById(R.id.edSDT);
        radioGroupGender = view.findViewById(R.id.radioGroupGender);
        radioButtonMale = view.findViewById(R.id.radioButtonMale);
        radioButtonFemale = view.findViewById(R.id.radioButtonFemale);
        buttonRegister = view.findViewById(R.id.buttonRegister);
        btBack = view.findViewById(R.id.btBack);

        // Xử lý khi bấm nút Đăng Ký
        buttonRegister.setOnClickListener(v -> {
            String email = edMail.getText().toString().trim();
            String password = edMK.getText().toString().trim();
            String confirmPassword = edNLMK.getText().toString().trim();

            // Kiểm tra đầu vào
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng ký tài khoản bằng Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();

                            // Chuyển sang LoginFragment
                            loadFragment(new LoginFragment());
                        } else {
                            Toast.makeText(getActivity(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Xử lý khi bấm nút Quay lại
        btBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    // Hàm chuyển Fragment nhé
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
