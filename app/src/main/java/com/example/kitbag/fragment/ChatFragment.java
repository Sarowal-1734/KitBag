package com.example.kitbag.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatAdapter;
import com.example.kitbag.databinding.FragmentChatBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.notification.FcmNotificationsSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;

    private ChatAdapter chatAdapter;
    private ArrayList<ChatModel> chatModelList = new ArrayList<>();

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Firebase database For storing message
    private DatabaseReference databaseReference;

    // For message
    private String message;

    // id for chat
    String receiverId = "2GH1wgT5qTMLqjBTPvt5j37iK1y1";

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);

        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText("Live Chat");

        // Setting Up Recycler view for showing message
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerViewChatDetails.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChatDetails.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(getActivity(), chatModelList);
        binding.recyclerViewChatDetails.setAdapter(chatAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initially disable the send button
        binding.buttonSendMessage.setEnabled(false);
        binding.buttonSendMessage.setColorFilter(R.color.gray);

        // Enable and disable send Button
        binding.editTextSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    binding.buttonSendMessage.setEnabled(true);
                    binding.buttonSendMessage.clearColorFilter();
                } else {
                    binding.buttonSendMessage.setEnabled(false);
                    binding.buttonSendMessage.setColorFilter(R.color.gray);
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
                if (currentUser != null) {
                    if (isConnected()) {
                        message = binding.editTextSendText.getText().toString().trim();
                        sendMessage(currentUser.getUid(), receiverId, message);
                        binding.editTextSendText.setText("");
                        sendNotification(message);
                    } else {
                        Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please login", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Read Message and show into recyclerview
        showMessage();

        return binding.getRoot();
    }

    // Send Notification
    private void sendNotification(String message) {
        FirebaseDatabase.getInstance().getReference("AdminToken")
                //.child("adminToken")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            String title = "You have a new text message";
                            String token = (String) snapshot1.getValue();
                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token,
                                    receiverId, title, message, getActivity().getApplicationContext(), getActivity());
                            notificationsSender.SendNotifications();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendMessage(String userId, String receiverId, String message) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Store user info in Database
        ChatModel chatModel = new ChatModel();
        chatModel.setSender(userId);
        chatModel.setReceiver(receiverId);
        chatModel.setMessage(message);
        chatModel.setStatus("Sent");
        databaseReference.child("ChatWithAdmin").child(currentUser.getUid()).push().setValue(chatModel);
    }

    private void showMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatWithAdmin").child(currentUser.getUid());
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