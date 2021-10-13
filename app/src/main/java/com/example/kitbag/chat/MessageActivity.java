package com.example.kitbag.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.kitbag.R;
import com.example.kitbag.adapter.FragmentAdapter;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;
    FirebaseFirestore database;
    FirebaseUser user;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMessageBinding binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(),getLifecycle());
        binding.viewPager.setAdapter(fragmentAdapter);

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setting image and username on toolbar
        database.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                Picasso.get().load(userModel.getImageUrl()).fit().placeholder(R.drawable.logo).into(binding.circularImageViewToolbarUser);
                binding.textViewUserNameToolbar.setText(userModel.getUserName());
            }
        });

        new TabLayoutMediator(
                binding.tabLayout,
                binding.viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        //Sets tabs names as mentioned in ViewPagerAdapter fragmentNames array, this can be implemented in many different ways.
                       if(position == 0){
                           tab.setText("CHATS");
                       }else {
                           tab.setText("USERS");
                       }
                    }
                }
        ).attach();

    }
}