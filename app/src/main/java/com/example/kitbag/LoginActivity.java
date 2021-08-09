package com.example.kitbag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.kitbag.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // Swipe to back
    private SlidrInterface slidrInterface;

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

        /// Swipe to back
        slidrInterface = Slidr.attach(this);

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

        // Active Inactive Slider to back based on drawer
        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                getCurrentFocus().clearFocus();
                slidrInterface.lock();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                slidrInterface.unlock();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
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

        // Check the internet connection then do the background tasks
        if (isConnected()) {
            // Connected

            // Show progressBar
            binding.progressBar.setVisibility(View.VISIBLE);

            // Check user already registered or not
            mAuth.fetchSignInMethodsForEmail(fakeEmail)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                // Hide progressBar
                                binding.progressBar.setVisibility(View.GONE);

                                binding.EditTextContact.setError("User Not Found!");
                                binding.EditTextContact.requestFocus();
                            } else {
                                // Sign in with email and password
                                SignIn(fakeEmail, password);
                            }
                        }
                    });
        } else {
            View parentLayout = findViewById(R.id.snackBarContainer);
            // create an instance of the snackBar
            final Snackbar snackbar = Snackbar.make(parentLayout, "", Snackbar.LENGTH_LONG);
            // inflate the custom_snackBar_view created previously
            View customSnackView = getLayoutInflater().inflate(R.layout.snackbar_disconnected, null);
            // set the background of the default snackBar as transparent
            snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
            // now change the layout of the snackBar
            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
            // set padding of the all corners as 0
            snackbarLayout.setPadding(0, 0, 0, 0);
            // add the custom snack bar layout to snackbar layout
            snackbarLayout.addView(customSnackView, 0);
            snackbar.show();
        }
    }

    // Sign in with email and password
    private void SignIn(String fakeEmail, String password) {
        mAuth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Hide progressBar
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Hide progressBar
                            binding.progressBar.setVisibility(View.GONE);
                            binding.EditTextPassword.setError("Password Didn't Match!");
                            binding.EditTextPassword.requestFocus();
                        }
                    }
                });
    }

    // Close Drawer on back pressed
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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