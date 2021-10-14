package com.example.kitbag.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityChatDetailsBinding;
import com.squareup.picasso.Picasso;

public class ChatDetailsActivity extends AppCompatActivity {
    ActivityChatDetailsBinding binding;
    private String username;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // SetUp toolbar for chat Details Activity
        setSupportActionBar(binding.toolbarChatDetail);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Getting value from Message Activity
        username = getIntent().getStringExtra("userName");
        imageUrl = getIntent().getStringExtra("imageUrl");

        // Setting value on Chat Details Activity (User Name  and User Image)

        binding.textViewUserNameToolbarChatDetail.setText(username);
        Picasso.get().load(imageUrl).placeholder(R.drawable.logo).fit().into(binding.circularImageViewToolbarChatDetail);




    }
}