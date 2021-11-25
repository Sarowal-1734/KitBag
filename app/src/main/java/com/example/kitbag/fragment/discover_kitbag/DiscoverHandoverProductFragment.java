package com.example.kitbag.fragment.discover_kitbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
        t.setText(R.string.product_handover_process);

        binding.SenderToPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNestedFragment(new DiscoverSenderToPrimaryFragment(), "discoverKitBag");
            }
        });

        binding.PrimaryToDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNestedFragment(new DiscoverPrimaryToDeliverymanFragment(), "discoverKitBag");
            }
        });

        binding.DeliverymanToFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNestedFragment(new DiscoverDeliverymanToFinalFragment(), "discoverKitBag");
            }
        });

        binding.FinalToReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNestedFragment(new DiscoverFinalToReceiverFragment(), "discoverKitBag");
            }
        });

        return binding.getRoot();
    }

    private void openNestedFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragmentContainer, fragment, tag).commit();
    }

}