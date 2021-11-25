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

        String imageUrlDeveloperSarowal = "https://firebasestorage.googleapis.com/v0/b/kitbag-ca2b0.appspot.com/o/admin_photos%2Fdeveloper_sarowal.jpg?alt=media&token=56b217db-8703-4b77-ad15-48612d52e06b";
        String imageUrlDeveloperMonir = "https://firebasestorage.googleapis.com/v0/b/kitbag-ca2b0.appspot.com/o/admin_photos%2Fdeveloper_monir.jpg?alt=media&token=819c0a97-cdb1-472a-b764-28c8517b14a8";

        Glide.with(this).load(imageUrlDeveloperSarowal)
                .placeholder(ShimmerEffect.get())
                .into(binding.imageViewSarowal);

        Glide.with(this).load(imageUrlDeveloperMonir)
                .placeholder(ShimmerEffect.get())
                .into(binding.imageViewMonir);

        return binding.getRoot();
    }
}