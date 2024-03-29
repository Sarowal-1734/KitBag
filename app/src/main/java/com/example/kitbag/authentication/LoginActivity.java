package com.example.kitbag.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityLoginBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // Show progressBar
    private ProgressDialog progressDialog;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initially Check Internet Connection
        if (!isConnected()) {
            displayNoConnection();
        }

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
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
        }

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.EditTextContact);

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);

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

        // On Login Button Clicked
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    if (valid()) {
                        // Get user input number
                        String phone = binding.cpp.getFullNumber().trim();
                        verifyNumberAndLogin(phone);
                    }
                } else {
                    displayNoConnection();
                }
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(LoginActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_login:
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
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

    } // Ending onCreate
    private void verifyNumberAndLogin(String phone) {
        String fakeEmail = phone + "@gmail.com";
        String password = binding.EditTextPassword.getText().toString();
        showProgressBar();
        // Check user already registered or not
        mAuth.fetchSignInMethodsForEmail(fakeEmail)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                // Hide progressBar
                                progressDialog.dismiss();
                                //binding.progressBar.setVisibility(View.GONE);
                                binding.EditTextContact.setError("User Not Found!");
                                binding.EditTextContact.requestFocus();
                            } else {
                                // Sign in with email and password
                                SignIn(fakeEmail, password);
                            }
                        }
                    }
                });
    }

    private boolean valid() {
        // Validation
        if (TextUtils.isEmpty(binding.EditTextContact.getText().toString())) {
            binding.EditTextContact.setError("Required");
            binding.EditTextContact.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextPassword.getText().toString())) {
            binding.EditTextPassword.setError("Required");
            binding.EditTextPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    private void displayNoConnection() {
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

    // Sign in with email and password
    private void SignIn(String fakeEmail, String password) {
        mAuth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isSuccessful()) {
                                                db.collection("Users").document(mAuth.getCurrentUser().getUid())
                                                        .update("userToken", task.getResult());
                                            }
                                        }
                                    });
                            // Hide progressBar
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Hide progressBar
                            progressDialog.dismiss();
                            binding.EditTextPassword.setError("Password doesn't match!");
                            binding.EditTextPassword.requestFocus();
                        }
                    }
                });
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

    // on Signup Button Clicked
    public void onSignupButtonClicked(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    // on Forgot Password Button Clicked
    public void onForgotPassButtonClicked(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
}