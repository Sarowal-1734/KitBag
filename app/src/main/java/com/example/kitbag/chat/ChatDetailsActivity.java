package com.example.kitbag.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatAdapter;
import com.example.kitbag.databinding.ActivityChatDetailsBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
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
import java.util.List;

public class ChatDetailsActivity extends AppCompatActivity {

    private ActivityChatDetailsBinding binding;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatModel> chatModelList = new ArrayList<>();

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //firebase database For storing message
    DatabaseReference databaseReference;

    // For message
    private String message;
    private String postReference;

    // id for chat
    String receiverId, postedBy;
    boolean gotUserId = true;


    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get post reference from intent
        postReference = getIntent().getStringExtra("postReference");
        postedBy = getIntent().getStringExtra("userId");

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

        // SetUp toolbar for chat Details Activity
        setSupportActionBar(binding.toolbarChatDetail);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initially disable the send button
        binding.buttonSendMessage.setEnabled(false);

        // Display username, post Title and post image in Message details activity
        db.collection("All_Post")
                .whereEqualTo("postReference", postReference)
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

        // On send button clicked
        binding.buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = binding.editTextSendText.getText().toString().trim();
                // Select the receiver user id
                if (!currentUser.getUid().equals(postedBy)) {
                    receiverId = postedBy;
                }
                sendMessage(currentUser.getUid(), receiverId, message);
                binding.editTextSendText.setText("");
            }
        });

        // Read Message and show into recyclerview
        showMessage();
    }

    private void sendMessage(String userId, String receiverId, String message) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Store user info in Database
        ChatModel chatModel = new ChatModel();
        chatModel.setSender(userId);
        chatModel.setReceiver(receiverId);
        chatModel.setMessage(message);
        databaseReference.child("Chats").child(postReference).push().setValue(chatModel);
    }

    private void showMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(postReference);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatModelList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                    if (!chatModel.getSender().equals(currentUser.getUid()) && gotUserId) {
                        done(chatModel.getSender());
                    }
                    if (chatModel.getSender().equals(currentUser.getUid()) && chatModel.getReceiver().equals(receiverId)) {
                        chatModelList.add(chatModel);
                    } else if (chatModel.getReceiver().equals(currentUser.getUid()) && chatModel.getSender().equals(receiverId)) {
                        chatModelList.add(chatModel);
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void done(String sender) {
        receiverId = sender;
        gotUserId = false;
    }
}