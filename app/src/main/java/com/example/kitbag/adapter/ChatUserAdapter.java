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
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private Context context;
    private List<ModelClassPost> modelClassPostListUser = new ArrayList<>();
    private String friendId;

    public ChatUserAdapter(Context context, List<ModelClassPost> modelClassPostListUser) {
        this.context = context;
        this.modelClassPostListUser = modelClassPostListUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_user_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClassPost postModelClass  = modelClassPostListUser.get(position);

        Picasso.get().load(postModelClass.getImageUrl()).placeholder(R.drawable.logo).fit().into(holder.circleImageViewSampleUserChat);
        holder.textViewSampleUserNameChat.setText(postModelClass.getUserName());
        holder.textViewSampleProductTitle.setText(postModelClass.getTitle());

    }

    @Override
    public int getItemCount() {
        return modelClassPostListUser.size();
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
            ModelClassPost postModelClass = modelClassPostListUser.get(getAdapterPosition());
            friendId = postModelClass.getUserId();
            Intent intent = new Intent(context,ChatDetailsActivity.class);
            intent.putExtra("friendId",friendId);
            Toast.makeText(context, friendId, Toast.LENGTH_SHORT).show();
            intent.putExtra("userName",postModelClass.getUserName());
            intent.putExtra("imageUrl",postModelClass.getImageUrl());
            context.startActivity(intent);
        }
    }
}
