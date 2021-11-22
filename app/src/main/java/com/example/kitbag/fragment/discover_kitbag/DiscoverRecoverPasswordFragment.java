package com.example.kitbag.fragment.discover_kitbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.kitbag.R;
import com.example.kitbag.databinding.FragmentDiscoverRecoverPasswordBinding;

public class DiscoverRecoverPasswordFragment extends Fragment {

    private FragmentDiscoverRecoverPasswordBinding binding;

    public DiscoverRecoverPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDiscoverRecoverPasswordBinding.inflate(inflater, container, false);

        // Change AppBar Title
        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.nav_discover_kitbag);

        binding.buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return binding.getRoot();
    }
}