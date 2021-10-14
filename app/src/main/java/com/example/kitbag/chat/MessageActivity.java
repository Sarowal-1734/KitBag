package com.example.kitbag.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatUserAdapter;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;
    FirebaseFirestore database;
    FirebaseUser user;
    FirebaseAuth auth;

    List<ModelClassPost> modelClassPostListUser = new ArrayList<>();
    ChatUserAdapter chatUserAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMessageBinding binding = ActivityMessageBinding.inflate(getLayoutInflater());
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
        ChatUserAdapter chatUserAdapter = new ChatUserAdapter(this,modelClassPostListUser);
        binding.recyclerViewUser.setAdapter(chatUserAdapter);
        binding.recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUser.setHasFixedSize(true);

        //Populate User on Message Activity
        database.collection("All_Post")
                .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
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