package com.mobileshop.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobileshop.app.admin.AdminDashboardActivity;
import com.mobileshop.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String uid = auth.getCurrentUser().getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("phone", phone);

                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference(Constants.NODE_USERS).child(uid);
                        ref.setValue(userMap).addOnCompleteListener(dbTask -> {
                            showLoading(false);
                            if (Constants.ADMIN_EMAIL.equalsIgnoreCase(email)) {
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                            } else {
                                startActivity(new Intent(this, MainActivity.class));
                            }
                            finish();
                        });
                    } else {
                        showLoading(false);
                        String msg = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}
