package com.example.kitbag;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.chrisbanes.photoview.PhotoView;

public class PostInfoActivity extends AppCompatActivity {

    private TextView appbar_title;
    private TextView textViewTitle, textViewUserTime, TextViewDescription,
            TextViewWeight, TextViewStatus, TextViewSource, TextViewDestination,
            TextViewUserId, TextViewUserType, TextViewChat, TextViewCall, TextViewMail;
    private PhotoView imageViewPhoto;
    private ImageView appbar_imageview_profile, appbar_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

        // remove search icon and notification icon from appBar
        findViewById(R.id.appbar_imageview_search).setVisibility(View.GONE);
        findViewById(R.id.appbar_notification_icon).setVisibility(View.GONE);

        // Adding back arrow in the appBar
        appbar_logo = findViewById(R.id.appbar_logo);
        appbar_logo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        appbar_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
            }
        });

        // Change the title of the appBar
        appbar_title = findViewById(R.id.appbar_title);
        appbar_title.setText("Post Informations");

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        // Init TextViews and ImageView
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewUserTime = findViewById(R.id.textViewUserTime);
        TextViewDescription = findViewById(R.id.TextViewDescription);
        TextViewWeight = findViewById(R.id.TextViewWeight);
        TextViewStatus = findViewById(R.id.TextViewStatus);
        TextViewSource = findViewById(R.id.TextViewSource);
        TextViewDestination = findViewById(R.id.TextViewDestination);
        TextViewUserId = findViewById(R.id.TextViewUserId);
        TextViewUserType = findViewById(R.id.TextViewUserType);
        TextViewChat = findViewById(R.id.TextViewChat);
        TextViewCall = findViewById(R.id.TextViewCall);
        TextViewMail = findViewById(R.id.TextViewMail);

        appbar_imageview_profile = findViewById(R.id.appbar_imageview_profile);
        imageViewPhoto = (PhotoView) findViewById(R.id.imageViewPhoto);

        // Click profile to open drawer
        appbar_imageview_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });

    }

    // On add to cart button click
    public void onAddToCartButtonClick(View view) {
        // Code here...
    }
}