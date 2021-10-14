package com.example.kitbag.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.kitbag.PostInfoActivity;
import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityChatDetailsBinding;
import com.example.kitbag.model.ModelClassPost;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class ChatDetailsActivity extends AppCompatActivity {
    ActivityChatDetailsBinding binding;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // id for chat
    String userId;
    String postId;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // SetUp toolbar for chat Details Activity
        setSupportActionBar(binding.toolbarChatDetail);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // getting id for chatting

        userId = getIntent().getStringExtra("userId");
        postId = getIntent().getStringExtra("postId");

        // Display username, post Title and post image in Message details activity
        db.collection("All_Post")
                .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ModelClassPost modelClassPost = new ModelClassPost();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            Picasso.get().load(modelClassPost.getImageUrl()).fit().placeholder(R.drawable.logo)
                                    .into(binding.circularImageViewToolbarItemPhotoChat);
                            binding.textViewToolbarItemTitleChat.setText(modelClassPost.getTitle());
                            binding.textViewToolbarUsernameChat.setText("Posted by "+modelClassPost.getUserName());
                        }
                    }
                });

    }
}