package com.example.kitbag.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<ChatModel> chatList;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == 1) {
            View view = layoutInflater.inflate(R.layout.sample_sender_chat, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        } else {
            View view = layoutInflater.inflate(R.layout.sample_receiver_chat, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatModel chatModel = chatList.get(position);
        holder.textViewMessage.setText(chatModel.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.showMessage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel chatModel = chatList.get(position);
        String currenUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currenUserId.equals(chatModel.getSender())) {
            return 1;
        }
        return 0;
    }
}

    /*public ChatAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender_chat,parent,false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver_chat,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel chatModel = chatModelList.get(position);
        holder.textViewMessage.setText(chatModel.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.showMessage);
        }
    }*/
