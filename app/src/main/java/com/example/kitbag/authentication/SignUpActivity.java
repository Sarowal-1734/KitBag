package com.example.kitbag.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivitySignUpBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    //for alert Dialog
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show progressBar
    private ProgressDialog progressDialog;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (SharedPreference.getDarkModeEnableValue(this)) {
            setTheme(R.style.DarkMode);
        } else {
            setTheme(R.style.LightMode);
        }
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
        } else {
            // No user is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
            binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
        }

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

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(SignUpActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_login:
                        finish();
                        break;
                    case R.id.nav_discover_kitbag:
                        intentFragment.putExtra("whatToDo","discoverKitBag");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_terms_conditions:
                        intentFragment.putExtra("whatToDo","termsAndCondition");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_contact:
                        intentFragment.putExtra("whatToDo","contactUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_about:
                        intentFragment.putExtra("whatToDo","aboutUs");
                        startActivity(intentFragment);
                        break;
                }
                return false;
            }
        });

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.editTextContact);

    }// ending onCreate
    public void onLoginButtonClick(View view) {
        onBackPressed();
    }

    // On get OTP button clicked
    public void onGetOTPButtonClicked(View view) {
        if (isConnected()) {
            if (validation()) {
                // Show progressBar
                progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                // Check user already registered or not
                String email = binding.cpp.getFullNumber().trim() + "@gmail.com";
                mAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                if (isNewUser) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(SignUpActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("whatToDo", "registration");
                                    intent.putExtra("password", binding.editTextPassword.getText().toString());
                                    intent.putExtra("userName", binding.editTextUsername.getText().toString());
                                    intent.putExtra("phoneNumber", binding.cpp.getFullNumberWithPlus().trim());
                                    startActivity(intent);
                                } else {
                                    progressDialog.dismiss();
                                    binding.editTextContact.setError("User Already Registered!");
                                    binding.editTextContact.requestFocus();
                                }
                            }
                        });
            }
        } else {
            showMessageNoConnection();
        }
    }

    // EditText Validation
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
        } else if (binding.editTextPassword.getText().toString().length() < 6) {
            binding.editTextPassword.setError("Minimum 6 characters");
            binding.editTextPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextConfirmPassword.getText().toString())) {
            binding.editTextConfirmPassword.setError("Required");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        } else if (binding.editTextConfirmPassword.getText().toString().length() < 6) {
            binding.editTextConfirmPassword.setError("Minimum 6 characters");
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

    // Close Drawer on back pressed
    @Override
    public void onBackPressed() {
        // progressDialog.dismiss(); //produce bug
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

    // Message no connection
    private void showMessageNoConnection() {
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