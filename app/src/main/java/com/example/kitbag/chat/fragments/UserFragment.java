package com.example.kitbag.chat.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatUserAdapter;
import com.example.kitbag.databinding.FragmentUserBinding;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {
    List<UserModel> userModelList = new ArrayList<>();
    ChatUserAdapter chatUserAdapter;
    FirebaseFirestore database;
    FirebaseAuth auth;
    FirebaseUser user;

    FragmentUserBinding binding;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false);

        chatUserAdapter = new ChatUserAdapter(getContext(), userModelList);
        binding.recyclerViewUserList.setAdapter(chatUserAdapter);
        binding.recyclerViewUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUserList.setHasFixedSize(true);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        database.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                userModelList.clear();

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel userModel = document.toObject(UserModel.class);
                    if (!userModel.getUserId().equals(user.getUid())) {
                        userModelList.add(userModel);
                    }
                }
                chatUserAdapter.notifyDataSetChanged();
            }
        });


        return binding.getRoot();
    }
}