package com.example.kitbag.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.kitbag.R;
import com.example.kitbag.databinding.FragmentAboutUsBinding;
import com.example.kitbag.effect.ShimmerEffect;

public class AboutUsFragment extends Fragment {

    private FragmentAboutUsBinding binding;

    public AboutUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAboutUsBinding.inflate(inflater, container, false);

        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.nav_about_us);

        String imageUrlDeveloperSarowal = "https://firebasestorage.googleapis.com/v0/b/kitbag-ca2b0.appspot.com/o/admin_photos%2Fdeveloper_sarowal.jpeg?alt=media&token=aa3d3a28-1a00-425b-8706-5e6cc9de613b";
        String imageUrlDeveloperMonir = "https://firebasestorage.googleapis.com/v0/b/kitbag-ca2b0.appspot.com/o/admin_photos%2Fdeveloper_monir.jpg?alt=media&token=4a5b9b47-1d3c-45e6-9573-93305463b79f";

        Glide.with(this).load(imageUrlDeveloperSarowal)
                .placeholder(ShimmerEffect.get())
                .into(binding.imageViewSarowal);

        Glide.with(this).load(imageUrlDeveloperMonir)
                .placeholder(ShimmerEffect.get())
                .into(binding.imageViewMonir);

        return binding.getRoot();
    }
}