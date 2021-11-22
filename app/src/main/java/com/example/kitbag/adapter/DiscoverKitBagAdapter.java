package com.example.kitbag.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;

import java.util.ArrayList;

public class DiscoverKitBagAdapter extends RecyclerView.Adapter<DiscoverKitBagAdapter.ViewHolder> {

    private DiscoverKitBagAdapter.OnItemClickListener listener;
    private int position;

    private Context context;
    private ArrayList<String> discoverKitBagItemList = new ArrayList<>();

    public DiscoverKitBagAdapter(Context context, ArrayList<String> discoverKitBagItemList) {
        this.context = context;
        this.discoverKitBagItemList = discoverKitBagItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_discover_kitbag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewDiscoverKitBagItems.setText(discoverKitBagItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return discoverKitBagItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDiscoverKitBagItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDiscoverKitBagItems = itemView.findViewById(R.id.textViewDiscoverKitBag);
            //for use onItemClickListener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    //for use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(DiscoverKitBagAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}
