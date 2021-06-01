package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationsActivity extends AppCompatActivity {

    private TextView appbar_title;
    private ImageView appbar_logo, appbar_imageview_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

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
                startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
            }
        });

        // Change the title of the appBar
        appbar_title = findViewById(R.id.appbar_title);
        appbar_title.setText("Notifications");

        // Open Drawer Layout
        appbar_imageview_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });

    }
}