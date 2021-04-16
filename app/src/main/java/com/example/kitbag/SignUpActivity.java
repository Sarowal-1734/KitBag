package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextContact, editTextEmail, editTextPassword, editTextConfirmPassword;
    private String username, contact, email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        //getUserData();
    }

    private void init() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextContact = findViewById(R.id.editTextContact);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
    }

    public void onLoginButtonClick(View view) {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    public void onSignUpButtonClick(View view) {
        boolean valid = validation();
        if (valid){
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        }
    }

//    private void getUserData() {
//        username = editTextUsername.getText().toString().trim();
//        contact = editTextContact.getText().toString().trim();
//        email = editTextEmail.getText().toString().trim();
//        password = editTextPassword.getText().toString().trim();
//        confirmPassword = editTextConfirmPassword.getText().toString().trim();
//    }



    private boolean validation() {
        if (TextUtils.isEmpty(editTextUsername.getText().toString())) {
            editTextUsername.setError("Recuired");
            return false;
        }
        if (TextUtils.isEmpty(editTextContact.getText().toString())) {
            editTextContact.setError("Recuired");
            return false;
        }
        if (TextUtils.isEmpty(editTextPassword.getText().toString())) {
            editTextPassword.setError("Recuired");
            return false;
        } else if (editTextPassword.getText().toString().length() < 8) {
            editTextPassword.setError("Minimum 8 characters");
            return false;
        }
        if (TextUtils.isEmpty(editTextConfirmPassword.getText().toString())) {
            editTextConfirmPassword.setError("Recuired");
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