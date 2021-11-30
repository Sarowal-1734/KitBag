package com.example.kitbag.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ModelClassNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    //for use onItemClickListener from MainActivity
    private NotificationAdapter.OnItemClickListener listener;
    private int position;

    private Context context;
    private ArrayList<ModelClassNotification> notificationArrayList;

    public NotificationAdapter(Context context, ArrayList<ModelClassNotification> notificationArrayList) {
        this.context = context;
        this.notificationArrayList = notificationArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClassNotification notifications = notificationArrayList.get(position);
        holder.textViewTitle.setText(notifications.getTitle());
        holder.textViewMessage.setText(notifications.getMessage());
        // Adding time ago format
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(notifications.getTime().getSeconds() * 1000);
        holder.textViewTime.setText(timeAgo);
        FirebaseFirestore.getInstance().collection("All_Post")
                .whereEqualTo("postReference", notifications.getPostReference())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.logo).fit().into(holder.circleImageViewPostImage);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return notificationArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageViewPostImage;
        TextView textViewTitle;
        TextView textViewMessage;
        TextView textViewTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewPostImage = itemView.findViewById(R.id.circleImageViewPostImage);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            //for use onItemClickListener from MainActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(notificationArrayList.get(position));
                    }
                }
            });
        }
    }

    // For use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(ModelClassNotification notification);
    }

    public void setOnItemClickListener(NotificationAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}
