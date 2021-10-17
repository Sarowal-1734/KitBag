package com.example.kitbag.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ModelClassPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    //for use onItemClickListener from MainActivity
    private PostAdapter.OnItemClickListener listener;
    private int position;

    private Context context;
    private List<ModelClassPost> modelClassPostList = new ArrayList<>();
    private String postId;

    public ChatUserAdapter(Context context, List<ModelClassPost> modelClassPostListUser) {
        this.context = context;
        this.modelClassPostList = modelClassPostListUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_user_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClassPost postModelClass  = modelClassPostList.get(position);

        Picasso.get().load(postModelClass.getImageUrl()).placeholder(R.drawable.logo).fit().into(holder.circleImageViewSampleUserChat);
        holder.textViewSampleUserNameChat.setText("Posted by "+postModelClass.getUserName());
        holder.textViewSampleProductTitle.setText(postModelClass.getTitle());

    }

    @Override
    public int getItemCount() {
        return modelClassPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageViewSampleUserChat;
        TextView textViewSampleUserNameChat;
        TextView textViewSampleLastMessageChat;
        TextView textViewSampleProductTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewSampleUserChat = itemView.findViewById(R.id.circularImageViewSampleChatUser);
            textViewSampleUserNameChat = itemView.findViewById(R.id.textViewSampleUsernameChat);
            textViewSampleLastMessageChat = itemView.findViewById(R.id.textViewSampleLastMessageChat);
            textViewSampleProductTitle = itemView.findViewById(R.id.textViewUserTitle);

            //for use onItemClickListener from MainActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(modelClassPostList.get(position));
                    }
                }
            });
        }
    }
    //for use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(ModelClassPost post);
    }

    public void setOnItemClickListener(PostAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
