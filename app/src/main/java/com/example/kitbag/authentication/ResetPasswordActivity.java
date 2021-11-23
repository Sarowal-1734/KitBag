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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityResetPasswordBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;

    // for alert dialog
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
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        loadLocale();
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Set drawer menu based on Login/Logout
        // Set as no user is signed in
        binding.navigationView.getMenu().clear();
        binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
        // Hide DarkMode button in drawer in MainActivity
        binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Reset Password");

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
                Intent intentFragment = new Intent(ResetPasswordActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_login:
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                        break;
                    case R.id.nav_language:
                        showChangeLanguageDialog();
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

    }//ending OnCreate

    // showing language alert Dialog to pick one language
    private void showChangeLanguageDialog() {
        final String[] multiLanguage = {"বাংলা","English"};
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Language..");
        builder.setSingleChoiceItems(multiLanguage, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    setLocale("bn");
                    recreate();
                }else {
                    setLocale("en");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }
    // setting chosen language to system
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        // Save data to SharedPreference
        SharedPreference.setLanguageValue(this, lang);
    }

    // get save value from sharedPreference and set It to as local language
    public void loadLocale(){
        setLocale(SharedPreference.getLanguageValue(this));
    }

    public void onResetPasswordButtonClicked(View view) {
        if (isConnected()) {
            if (validation()) {
                showProgressBar();
                // Retrieve the password from RealTime Database for login and update password
                FirebaseDatabase.getInstance().getReference().child("Passwords")
                        .child(getIntent().getStringExtra("phoneNumber"))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String password = (String) task.getResult().getValue();
                                    String fakeEmail = getIntent().getStringExtra("phoneNumber") + "@gmail.com";
                                    // Sign in
                                    mAuth.signInWithEmailAndPassword(fakeEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Update Password
                                                mAuth.getCurrentUser().updatePassword(binding.editTextPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        // Update the password in RealTime Database for ForgotPassword
                                                        FirebaseDatabase.getInstance().getReference().child("Passwords")
                                                                .child(getIntent().getStringExtra("phoneNumber"))
                                                                .setValue(binding.editTextPassword.getText().toString());
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ResetPasswordActivity.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ResetPasswordActivity.this, "Password Reset Failed!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }
        } else {
            progressDialog.dismiss();
            showMessageNoConnection();
        }
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(ResetPasswordActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    private boolean validation() {
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