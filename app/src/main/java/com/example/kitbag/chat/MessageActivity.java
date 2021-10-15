package com.example.kitbag.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatUserAdapter;
import com.example.kitbag.adapter.PostAdapter;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
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

public class MessageActivity extends AppCompatActivity {
    private ActivityMessageBinding binding;

    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private String postReference;

    // Show progressBar
    private ProgressDialog progressDialog;

    private ModelClassPost modelClassPost;

    private boolean repeatPostId = false;

    private List<ModelClassPost> modelClassPostUserList = new ArrayList<>();
    private ChatUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Authenticate Firebase and Firebase User
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // SetUp Toolbar of Message Activity
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Messenger");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // setting image and username on toolbar Message Activity
        db.collection("Users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                Picasso.get().load(userModel.getImageUrl()).fit().placeholder(R.drawable.logo).into(binding.circularImageViewToolbarUser);
                binding.textViewUserNameToolbar.setText(userModel.getUserName());
            }
        });

        // setting up adapter
        binding.recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUser.setHasFixedSize(true);
        adapter = new ChatUserAdapter(MessageActivity.this, modelClassPostUserList);
        binding.recyclerViewUser.setAdapter(adapter);

        // Show progressBar
        progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);

        // Get my chat list from Firebase and fireStore and set to the recyclerView
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelClassPostUserList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                        ChatModel chatModel = snapshot1.getValue(ChatModel.class);
                        if (chatModel.getSender().equals(currentUser.getUid()) || chatModel.getReceiver().equals(currentUser.getUid())) {
                            postReference = dataSnapshot.getKey();
                            db.collection("All_Post")
                                    .whereEqualTo("postReference", postReference)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                                                modelClassPostUserList.add(modelClassPost);
                                            }
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                            // On recycler item click listener
                            adapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(ModelClassPost post) {
                                    Intent intent = new Intent(MessageActivity.this, ChatDetailsActivity.class);
                                    intent.putExtra("postReference", post.getPostReference());
                                    intent.putExtra("userId", post.getUserId());
                                    startActivity(intent);
                                }
                            });
                            break;
                        }
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

}