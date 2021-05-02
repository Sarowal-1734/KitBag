package com.example.kitbag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

public class PostActivity extends AppCompatActivity {
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        setTitle("Post Your Item");

        // Open Drawer Layout
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.RIGHT);
            }
        });

    }
}