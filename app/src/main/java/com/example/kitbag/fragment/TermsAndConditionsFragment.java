package com.example.kitbag.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.kitbag.R;

public class TermsAndConditionsFragment extends Fragment {

    public TermsAndConditionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.terms_and_conditions);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terms_and_conditions, container, false);
    }
}