package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LoginActivity extends AppCompatActivity {
    private EditText EditTextContact, EditTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditTextContact = findViewById(R.id.EditTextContact);
        EditTextPassword = findViewById(R.id.EditTextPassword);
    }

    public void onLoginButtonClick(View view) {
        if (TextUtils.isEmpty(EditTextContact.getText().toString())) {
            EditTextContact.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(EditTextPassword.getText().toString())) {
            EditTextPassword.setError("Required");
            return;
        }
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }


    public void onSignupButtonClick(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }
}