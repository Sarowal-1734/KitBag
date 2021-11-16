package com.example.kitbag.chat;

import static com.example.kitbag.ui.MainActivity.fromChatDetailsActivity;
import static com.example.kitbag.ui.MainActivity.getOpenFromActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatAdapter;
import com.example.kitbag.databinding.ActivityChatDetailsBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.ui.EditProfileActivity;
import com.example.kitbag.ui.PostInfoActivity;
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
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatDetailsActivity extends AppCompatActivity {

    private ActivityChatDetailsBinding binding;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatModel> chatModelList = new ArrayList<>();

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //firebase database For storing message
    DatabaseReference databaseReference;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // For message
    private String message;
    private String postReference;

    // id for chat
    String receiverId, postedBy, childKeyUserId;
    ModelClassPost modelClassPost;

    // To display userInfo
    private String userId;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Swipe to back
        slidrInterface = Slidr.attach(this);

        // Get post reference from intent
        postReference = getIntent().getStringExtra("postReference");
        postedBy = getIntent().getStringExtra("userId");
        childKeyUserId = getIntent().getStringExtra("childKeyUserId");

        // Setting Up Recycler view for showing message
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewChatDetails.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChatDetails.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(ChatDetailsActivity.this, chatModelList);
        binding.recyclerViewChatDetails.setAdapter(chatAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // SetUp toolbar for chat Details Activity
        binding.imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initially disable the send button
        binding.buttonSendMessage.setEnabled(false);

        // Display username, postType and user image in toolbar
        displayUserInfo();

        // Display username, post Title and post image in cardView
        displayPostInfo();

        // On click the user info to see the user profile
        binding.userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailsActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        // On click the post info to see the post info
        binding.cardViewPostInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailsActivity.this, PostInfoActivity.class);
                intent.putExtra("postReference", postReference);
                intent.putExtra("userId", modelClassPost.getUserId());
                intent.putExtra("statusCurrent", modelClassPost.getStatusCurrent());
                intent.putExtra(getOpenFromActivity, fromChatDetailsActivity);
                startActivity(intent);
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
                } else {
                    receiverId = childKeyUserId;
                }
                sendMessage(currentUser.getUid(), receiverId, message);
                binding.editTextSendText.setText("");
            }
        });

        // Read Message and show into recyclerview
        showMessage();
    }

    // Display username, postType and user image in toolbar
    private void displayUserInfo() {
        userId = postedBy;
        if (currentUser.getUid().equals(postedBy)) {
            userId = childKeyUserId;
        }
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel model = documentSnapshot.toObject(UserModel.class);
                        binding.textViewChatWithUserName.setText(model.getUserName());
                        binding.textViewChatWithUserType.setText(model.getUserType());
                        Picasso.get().load(model.getImageUrl()).fit().placeholder(R.drawable.logo)
                                .into(binding.circularImageViewChatWithUser);
                    }
                });
    }

    // Display username, post Title and post image in cardView
    private void displayPostInfo() {
        db.collection("All_Post")
                .whereEqualTo("postReference", postReference)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        modelClassPost = new ModelClassPost();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            Picasso.get().load(modelClassPost.getImageUrl()).fit().placeholder(R.drawable.logo)
                                    .into(binding.circularImageViewToolbarItemPhotoChat);
                            binding.textViewToolbarItemTitleChat.setText(modelClassPost.getTitle());
                            db.collection("Users").document(postedBy)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            binding.textViewToolbarUsernameChat.setText("Posted by " + documentSnapshot.getString("userName"));
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendMessage(String userId, String receiverId, String message) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Store user info in Database
        ChatModel chatModel = new ChatModel();
        chatModel.setSender(userId);
        chatModel.setReceiver(receiverId);
        chatModel.setMessage(message);
        //Toast.makeText(ChatDetailsActivity.this, postReference+"\n" +receiverId, Toast.LENGTH_LONG).show();
        if (userId.equals(postedBy)) {
            databaseReference.child("Chats").child(postReference).child(receiverId).push().setValue(chatModel);
        } else {
            databaseReference.child("Chats").child(postReference).child(userId).push().setValue(chatModel);
        }
    }

    private void showMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats").child(postReference).child(childKeyUserId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatModelList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                        chatModelList.add(chatModel);
                    if (chatAdapter.getItemCount() > 0) {
                        binding.recyclerViewChatDetails.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}