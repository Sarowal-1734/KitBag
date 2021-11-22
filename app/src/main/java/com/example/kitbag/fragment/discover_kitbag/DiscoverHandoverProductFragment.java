package com.example.kitbag.fragment.discover_kitbag;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kitbag.R;
import com.example.kitbag.databinding.FragmentDiscoverHandoverProductBinding;

public class DiscoverHandoverProductFragment extends Fragment {

    private FragmentDiscoverHandoverProductBinding binding;

    public DiscoverHandoverProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDiscoverHandoverProductBinding.inflate(inflater, container, false);

        // Change AppBar Title
        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.nav_discover_kitbag);

        return binding.getRoot();
    }
}