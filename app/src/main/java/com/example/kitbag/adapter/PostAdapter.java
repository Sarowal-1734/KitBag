package com.example.kitbag.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    //for use onItemClickListener from MainActivity
    private OnItemClickListener listener;
    int position;

    private Context context;
    private ArrayList<ModelClassPost> postList;

    public PostAdapter(Context context, ArrayList<ModelClassPost> journals) {
        this.context = context;
        this.postList = journals;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.row_post_items_recycler_view, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelClassPost post = postList.get(position);
        String imageUrl = post.getImageUrl();
        // Picasso library for download & show image
        Picasso.get().load(imageUrl).placeholder(R.drawable.logo).fit().centerCrop().into(holder.imageView);
        holder.titleTV.setText(post.getTitle());
        String destination = post.getFromDistrict() + " - " + post.getToDistrict();
        holder.destinationTV.setText(destination);
        // Adding time ago format
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(post.getTimeAdded().getSeconds() * 1000);
        holder.timeAddedTV.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView destinationTV, titleTV, timeAddedTV;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postedImages);
            titleTV = itemView.findViewById(R.id.postTitle);
            destinationTV = itemView.findViewById(R.id.from_to);
            timeAddedTV = itemView.findViewById(R.id.postDate);

            //for use onItemClickListener from MainActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(postList.get(position));
                    }
                }
            });

        }
    }

    //for use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(ModelClassPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}