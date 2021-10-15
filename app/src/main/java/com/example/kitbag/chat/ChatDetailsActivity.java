package com.example.kitbag.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kitbag.PostInfoActivity;
import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatAdapter;
import com.example.kitbag.databinding.ActivityChatDetailsBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatDetailsActivity extends AppCompatActivity {
    ActivityChatDetailsBinding binding;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    List<ChatModel> chatModelList = new ArrayList<>();

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //firebase database For storing message
    DatabaseReference databaseReference;

    // For message
    String message;

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

        // Setting Up Recycler view for showing message
        recyclerView = findViewById(R.id.recyclerViewChatDetails);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(ChatDetailsActivity.this, chatModelList);
        recyclerView.setAdapter(chatAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.toString();

        // getting value from Post Details Activity for chatting
        userId = getIntent().getStringExtra("userId");
        postId = getIntent().getStringExtra("postId");


        // SetUp toolbar for chat Details Activity
        setSupportActionBar(binding.toolbarChatDetail);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initially disable the send button
        binding.buttonSendMessage.setEnabled(false);

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
                            binding.textViewToolbarUsernameChat.setText("Posted by " + modelClassPost.getUserName());
                        }
                    }
                });


        // Enable and disable send Button
        binding.editTextSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    binding.buttonSendMessage.setEnabled(true);
                } else {
                    binding.buttonSendMessage.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = binding.editTextSendText.getText().toString().trim();
                sendMessage(userId, postId, message);
                binding.editTextSendText.setText("");
            }
        });


        // Read Message and show recyclerview
        showMessage(userId, postId);
    }

    private void sendMessage(String userId, String postId, String message) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Store user info in Database
        ChatModel chatModel = new ChatModel();
        chatModel.setSender(userId);
        chatModel.setReceiver(postId);
        chatModel.setMessage(message);
        databaseReference.child("Chats").push().setValue(chatModel);
    }

   private void showMessage(String userId, String postId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatModelList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);

                    if(chatModel.getSender().equals(userId) && chatModel.getReceiver().equals(postId)
                    || chatModel.getSender().equals(postId) && chatModel.getReceiver().equals(userId) ){
                        chatModelList.add(chatModel);
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}