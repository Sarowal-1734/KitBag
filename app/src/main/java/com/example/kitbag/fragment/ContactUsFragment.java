package com.example.kitbag.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.kitbag.R;

public class ContactUsFragment extends Fragment {

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.nav_contact_us);

        view.findViewById(R.id.LiveChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Chat Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.Email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "sm.dynamic.host@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "KitBag Courier Service"));
            }
        });

        return view;
    }

}