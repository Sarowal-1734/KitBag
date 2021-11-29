package com.example.kitbag.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ChatModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
        //holder.textViewTime.setText(getDateTimeFormat(chatModel.getTime()));
        holder.textViewStatus.setText(chatModel.getStatus());
    }

    private String getDateTimeFormat(Timestamp timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timeStamp.getSeconds() * 1000);
        return DateFormat.format("dd MMM hh:mm", cal).toString();
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTime, textViewStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.showMessage);
            //textViewTime = itemView.findViewById(R.id.time);
            textViewStatus = itemView.findViewById(R.id.status);
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
