package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SignUpActivity extends AppCompatActivity {

    private TextView appbar_title;
    private ImageView appbar_logo, appbar_imageview_profile;
    private EditText editTextUsername, editTextContact, editTextEmail, editTextPassword, editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        appbar_imageview_profile = findViewById(R.id.appbar_imageview_profile);

        // remove search icon and notification icon from appBar
        findViewById(R.id.appbar_imageview_search).setVisibility(View.GONE);
        findViewById(R.id.appbar_notification_icon).setVisibility(View.GONE);

        // Adding back arrow in the appBar
        appbar_logo = findViewById(R.id.appbar_logo);
        appbar_logo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        appbar_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

        // Change the title of the appBar
        appbar_title = findViewById(R.id.appbar_title);
        appbar_title.setText("Sign Up");

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextContact = findViewById(R.id.editTextContact);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        // Open Drawer Layout
        appbar_imageview_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });


    }

    public void onLoginButtonClick(View view) {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    public void onSignUpButtonClick(View view) {
        boolean valid = validation();
        if (valid){
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        }
    }

    private boolean validation() {
        if (TextUtils.isEmpty(editTextUsername.getText().toString())) {
            editTextUsername.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(editTextContact.getText().toString())) {
            editTextContact.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(editTextPassword.getText().toString())) {
            editTextPassword.setError("Required");
            return false;
        } else if (editTextPassword.getText().toString().length() < 8) {
            editTextPassword.setError("Minimum 8 characters");
            return false;
        }
        if (TextUtils.isEmpty(editTextConfirmPassword.getText().toString())) {
            editTextConfirmPassword.setError("Required");
            return false;
        } else if (editTextConfirmPassword.getText().toString().length() < 8) {
            editTextConfirmPassword.setError("Minimum 8 characters");
            return false;
        }
        if (!editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())){
            Toast.makeText(SignUpActivity.this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}