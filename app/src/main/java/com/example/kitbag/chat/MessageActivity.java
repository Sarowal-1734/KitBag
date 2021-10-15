package com.example.kitbag.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatUserAdapter;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
    ActivityMessageBinding binding;
    FirebaseFirestore database;
    DatabaseReference databaseReference;
    FirebaseUser user;
    FirebaseAuth auth;

    String postId = null;
    boolean repeatPostId = false;

    List<ModelClassPost> modelClassPostListUser = new ArrayList<>();
    ChatUserAdapter chatUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Authenticate Firebase and Firebase User
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // SetUp Toolbar of Message Activity
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Messenger");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // setting image and username on toolbar Message Activity
        database.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
        ChatUserAdapter chatUserAdapter = new ChatUserAdapter(MessageActivity.this, modelClassPostListUser);
        binding.recyclerViewUser.setAdapter(chatUserAdapter);


        // For Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = snapshot.getValue(ChatModel.class);
                    postId = chatModel.getReceiver();
                    Toast.makeText(MessageActivity.this, postId, Toast.LENGTH_SHORT).show();
                    if (postId != null && chatModel.getReceiver().equals(postId)) {
                        repeatPostId = true;
                    }
                    if (repeatPostId == false) {
                        //Populate Post on Message Activity
                        database.collection("All_Post")
                                .whereEqualTo("postReference", postId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        ModelClassPost modelClassPost = new ModelClassPost();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                                            modelClassPostListUser.add(modelClassPost);
                                        }
                                        chatUserAdapter.notifyDataSetChanged();
                                    }
                                });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); }

}