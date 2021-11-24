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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityForgotPasswordBinding;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    // for alert dialog
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show progressBar
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        loadLocale();
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
        } else {
            // No user is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
        }

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(ForgotPasswordActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_login:
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

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // On next button clicked
        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.EditTextContact.getText().toString())) {
                    binding.EditTextContact.setError("Required");
                    binding.EditTextContact.requestFocus();
                    return;
                }
                // Check Phone already registered or not
                String email = binding.cpp.getFullNumber().trim() + "@gmail.com";
                if (isConnected()) {
                    // Show progressBar
                    progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    progressDialog.setCancelable(false);
                    mAuth.fetchSignInMethodsForEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                    if (isNewUser) {
                                        // Hide progressBar
                                        progressDialog.dismiss();
                                        binding.EditTextContact.setError("User Not Found!");
                                        binding.EditTextContact.requestFocus();
                                    } else {
                                        // Hide progressBar
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                                        intent.putExtra("whatToDo", "resetPassword");
                                        intent.putExtra("phoneNumber", binding.cpp.getFullNumberWithPlus().trim());
                                        startActivity(intent);
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
        binding.customAppBar.appbarTitle.setText("Forgot Password");

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.EditTextContact);
    }// ending OnCreate


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
}