package com.example.kitbag;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.kitbag.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Sign Up");

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.editTextContact);

    }

    public void onLoginButtonClick(View view) {
        onBackPressed();
    }

    // On get OTP button clicked
    public void onGetOTPButtonClicked(View view) {
        boolean valid = validation();
        if (valid) {
            // Check user already registered or not
            String email = binding.cpp.getFullNumber().trim() + "@gmail.com";
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                Intent intent = new Intent(SignUpActivity.this, OTP_Verification.class);
                                intent.putExtra("whatToDo", "registration");
                                intent.putExtra("username", binding.editTextUsername.getText().toString());
                                intent.putExtra("mobile", binding.cpp.getFullNumberWithPlus().trim());
                                intent.putExtra("email", binding.editTextEmail.getText().toString());
                                intent.putExtra("password", binding.editTextPassword.getText().toString());
                                startActivity(intent);
                            } else {
                                binding.editTextContact.setError("User Already Registered!");
                                binding.editTextContact.requestFocus();
                            }

                        }
                    });
        }
    }

    private boolean validation() {
        if (TextUtils.isEmpty(binding.editTextUsername.getText().toString())) {
            binding.editTextUsername.setError("Required");
            binding.editTextUsername.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextContact.getText().toString())) {
            binding.editTextContact.setError("Required");
            binding.editTextContact.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextPassword.getText().toString())) {
            binding.editTextPassword.setError("Required");
            return false;
        } else if (binding.editTextPassword.getText().toString().length() < 8) {
            binding.editTextPassword.setError("Minimum 8 characters");
            binding.editTextPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextConfirmPassword.getText().toString())) {
            binding.editTextConfirmPassword.setError("Required");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        } else if (binding.editTextConfirmPassword.getText().toString().length() < 8) {
            binding.editTextConfirmPassword.setError("Minimum 8 characters");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        }
        if (!binding.editTextPassword.getText().toString().equals(binding.editTextConfirmPassword.getText().toString())) {
            binding.editTextConfirmPassword.setError("Password doesn't match");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }
}