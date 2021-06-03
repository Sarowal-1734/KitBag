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

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LoginActivity extends AppCompatActivity {

    private TextView appbar_title;
    private ImageView appbar_logo, appbar_imageview_profile;
    private EditText EditTextContact, EditTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        appbar_imageview_profile = findViewById(R.id.appbar_imageview_profile);

        // remove search icon and notification icon from appBar
        findViewById(R.id.appbar_imageview_search).setVisibility(View.GONE);
        findViewById(R.id.appbar_notification_icon).setVisibility(View.GONE);

        // Open Drawer Layout
        appbar_imageview_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });

        // Adding back arrow in the appBar
        appbar_logo = findViewById(R.id.appbar_logo);
        appbar_logo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        appbar_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        appbar_title = findViewById(R.id.appbar_title);
        appbar_title.setText("Login");

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
    }
}