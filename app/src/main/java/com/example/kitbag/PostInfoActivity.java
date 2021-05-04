package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

public class PostInfoActivity extends AppCompatActivity {

    private TextView textViewTitle, textViewUserTime, TextViewDescription,
            TextViewWeight, TextViewStatus, TextViewSource, TextViewDestination,
            TextViewUserId, TextViewUserType, TextViewChat, TextViewCall, TextViewMail;
    private PhotoView imageViewPhoto;
    private ImageView imageViewProfile;
    private ImageView imageViewSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

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

        imageViewProfile = findViewById(R.id.imageViewProfile);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageViewPhoto = (PhotoView) findViewById(R.id.imageViewPhoto);

        // Click profile to open drawer
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.RIGHT);
            }
        });

    }

    // On add to cart button click
    public void onAddToCartButtonClick(View view) {
        // Code here...
    }
}