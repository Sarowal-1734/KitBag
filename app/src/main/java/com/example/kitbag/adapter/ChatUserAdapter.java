package com.example.kitbag.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ModelClassMessageUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    //for use onItemClickListener from MainActivity
    private ChatUserAdapter.OnItemClickListener listener;
    private int position;

    private Context context;
    private List<ModelClassMessageUser> modelClassMessageUser = new ArrayList<>();
    private String postId;

    public ChatUserAdapter(Context context, List<ModelClassMessageUser> modelClassMessageUser) {
        this.context = context;
        this.modelClassMessageUser = modelClassMessageUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_user_chat,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClassMessageUser messageUsers = modelClassMessageUser.get(position);
        Picasso.get().load(messageUsers.getPostImageUrl()).placeholder(R.drawable.logo).fit().into(holder.circleImageViewSampleUserChat);
        FirebaseFirestore.getInstance().collection("Users").document(messageUsers.getPostedById())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.textViewSampleUserNameChat.setText("Posted by " + documentSnapshot.getString("userName"));
                    }
                });
        FirebaseFirestore.getInstance().collection("Users").document(messageUsers.getChildKeyUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.textViewChatWith.setText("Chat with " + documentSnapshot.getString("userName"));
                    }
                });
        holder.textViewSampleProductTitle.setText(messageUsers.getPostTitle());

    }

    @Override
    public int getItemCount() {
        return modelClassMessageUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageViewSampleUserChat;
        TextView textViewSampleUserNameChat;
        TextView textViewChatWith;
        TextView textViewSampleProductTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewSampleUserChat = itemView.findViewById(R.id.circularImageViewSampleChatUser);
            textViewSampleUserNameChat = itemView.findViewById(R.id.textViewSampleUsernameChat);
            textViewChatWith = itemView.findViewById(R.id.textViewChatWith);
            textViewSampleProductTitle = itemView.findViewById(R.id.textViewUserTitle);

            // For use onItemClickListener from MainActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(modelClassMessageUser.get(position));
                    }
                }
            });
        }
    }
    // For use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(ModelClassMessageUser list);
    }

    public void setOnItemClickListener(ChatUserAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
