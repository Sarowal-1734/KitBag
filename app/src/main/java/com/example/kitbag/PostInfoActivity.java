package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PostInfoActivity extends AppCompatActivity {

    private TextView textViewTitle, textViewUserTime, TextViewDescription,
            TextViewWeight, TextViewStatus, TextViewSource, TextViewDestination,
            TextViewUserId, TextViewUserType, TextViewChat, TextViewCall, TextViewMail;
    private ImageView imageViewPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

        // Init TextViews and ImageButton
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
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

    }

    // On add to cart button click
    public void onAddToCartButtonClick(View view) {
        // Code here...
    }
}