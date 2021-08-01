package com.example.kitbag;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.kitbag.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
        } else {
            // No user is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
        }

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.EditTextContact);

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Login");
    }

    public void onLoginButtonClick(View view) {
        if (TextUtils.isEmpty(binding.EditTextContact.getText().toString())) {
            binding.EditTextContact.setError("Required");
            binding.EditTextContact.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(binding.EditTextPassword.getText().toString())) {
            binding.EditTextPassword.setError("Required");
            binding.EditTextPassword.requestFocus();
            return;
        }
        // Get user input data
        String phone = binding.cpp.getFullNumber().trim();
        String fakeEmail = phone + "@gmail.com";
        String password = binding.EditTextPassword.getText().toString();

        // Check user already registered or not
        mAuth.fetchSignInMethodsForEmail(fakeEmail)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                        if (isNewUser) {
                            binding.EditTextContact.setError("User Not Found!");
                            binding.EditTextContact.requestFocus();
                        } else {
                            // Sign in with email and password
                            SignIn(fakeEmail, password);
                        }

                    }
                });
    }

    // Sign in with email and password
    private void SignIn(String fakeEmail, String password) {
        mAuth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            binding.EditTextPassword.setError("Password Didn't Match!");
                            binding.EditTextPassword.requestFocus();
                        }
                    }
                });
    }


    // on Signup Button Clicked
    public void onSignupButtonClicked(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    // on Forgot Password Button Clicked
    public void onForgotPassButtonClicked(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
}