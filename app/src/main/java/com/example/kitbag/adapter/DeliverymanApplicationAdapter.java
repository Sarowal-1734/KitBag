package com.example.kitbag.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kitbag.R;
import com.example.kitbag.model.ModelClassDeliveryman;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeliverymanApplicationAdapter extends RecyclerView.Adapter<DeliverymanApplicationAdapter.ViewHolder> {

    // For use onItemClickListener from MainActivity
    private OnItemClickListener listener;
    private OnItemClickListener listenerCall;
    private int position;

    private Context context;
    private ArrayList<ModelClassDeliveryman> applicationList;

    public DeliverymanApplicationAdapter(Context context, ArrayList<ModelClassDeliveryman> applicationList) {
        this.context = context;
        this.applicationList = applicationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_application_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelClassDeliveryman application = applicationList.get(position);
        holder.textViewUsername.setText(application.getNameEnglish());
        holder.textViewPhoneNumber.setText(application.getPhoneNumber());
        holder.timeStampAppliedOn.setText("Applied On " + getSince(application.getTimeApplied()));

        Picasso.get().load(application.getImageUrlUserFace()).fit().placeholder(R.drawable.logo).into(holder.circleImageViewApplicantImageUrl);


    }

    public String getSince(Timestamp since) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(since.getSeconds() * 1000);
        return DateFormat.format("dd MMM yyyy", cal).toString();
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageViewApplicantImageUrl;
        TextView textViewUsername;
        TextView textViewPhoneNumber;
        TextView timeStampAppliedOn;
        ImageView imageViewCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewApplicantImageUrl = itemView.findViewById(R.id.circularImageViewUserPhoto);
            textViewUsername = itemView.findViewById(R.id.textViewUserName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewUserphone);
            imageViewCall = itemView.findViewById(R.id.imageViewCall);
            timeStampAppliedOn = itemView.findViewById(R.id.textViewUserSince);

            // For use onItemClickListener from MainActivity
            imageViewCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listenerCall != null && position != -1) {
                        listenerCall.onItemClick(applicationList.get(position));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (listener != null && position != -1) {
                        listener.onItemClick(applicationList.get(position));
                    }
                }
            });


        }

    }

    // For use onItemClickListener from MainActivity
    public interface OnItemClickListener {
        void onItemClick(ModelClassDeliveryman application);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnCallIconClickListener(OnItemClickListener listenerCall) {
        this.listenerCall = listenerCall;
    }

}
