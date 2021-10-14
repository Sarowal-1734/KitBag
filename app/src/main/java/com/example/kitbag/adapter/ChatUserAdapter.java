package com.example.kitbag.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.chat.ChatDetailsActivity;
import com.example.kitbag.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private Context context;
    private List<UserModel> userModelList = new ArrayList<>();
    private String friendId;

    public ChatUserAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_user_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);

        Picasso.get().load(userModel.getImageUrl()).placeholder(R.drawable.logo).fit().into(holder.circleImageViewSampleUserChat);
        holder.textViewSampleUserNameChat.setText(userModel.getUserName());
        holder.textViewSampleLastMessageChat.setText(userModel.getLastMessage());
        holder.textViewSampleProductTitle.setText(userModel.getProductTitle());

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView circleImageViewSampleUserChat;
        private TextView textViewSampleUserNameChat;
        private TextView textViewSampleLastMessageChat;
        private TextView textViewSampleProductTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewSampleUserChat = itemView.findViewById(R.id.circularImageViewSampleChatUser);
            textViewSampleUserNameChat = itemView.findViewById(R.id.textViewSampleUsernameChat);
            textViewSampleLastMessageChat = itemView.findViewById(R.id.textViewSampleLastMessageChat);
            textViewSampleProductTitle = itemView.findViewById(R.id.textViewUserTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            UserModel userModel = userModelList.get(getAdapterPosition());
            friendId = userModel.getUserId();
            Intent intent = new Intent(context,ChatDetailsActivity.class);
            intent.putExtra("friendId",friendId);
            Toast.makeText(context, friendId, Toast.LENGTH_SHORT).show();
            intent.putExtra("userName",userModel.getUserName());
            intent.putExtra("imageUrl",userModel.getImageUrl());
            context.startActivity(intent);
        }
    }
}
