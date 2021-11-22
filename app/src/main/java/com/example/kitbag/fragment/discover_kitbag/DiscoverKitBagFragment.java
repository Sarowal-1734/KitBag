package com.example.kitbag.fragment.discover_kitbag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kitbag.R;
import com.example.kitbag.adapter.DiscoverKitBagAdapter;
import com.example.kitbag.databinding.FragmentDiscoverKitBagBinding;

import java.util.ArrayList;

public class DiscoverKitBagFragment extends Fragment {

    private FragmentDiscoverKitBagBinding binding;

    public DiscoverKitBagFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDiscoverKitBagBinding.inflate(inflater, container, false);

        // Change AppBar Title
        TextView t = getActivity().findViewById(R.id.custom_app_bar).findViewById(R.id.appbar_title);
        t.setText(R.string.nav_discover_kitbag);

        // Setup Adapter
        ArrayList<String> discoverItems = new ArrayList<>();
        discoverItems.add("1. How to create an account?");
        discoverItems.add("2. How do I edit my profile?");
        discoverItems.add("3. How to recover password?");
        discoverItems.add("4. How to change your password?");
        discoverItems.add("5. How to post an item?");
        discoverItems.add("6. How to edit my post?");
        discoverItems.add("7. How to delete my post?");
        discoverItems.add("8. How to become a deliveryman?");
        discoverItems.add("9. How to handover product?");
        binding.recyclerViewDiscoverKitBag.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerViewDiscoverKitBag.setHasFixedSize(true);
        DiscoverKitBagAdapter adapter = new DiscoverKitBagAdapter(getActivity(), discoverItems);
        binding.recyclerViewDiscoverKitBag.setAdapter(adapter);

        // On Item Click
        adapter.setOnItemClickListener(new DiscoverKitBagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        openNestedFragment(new DiscoverCreateAccountFragment());
                        break;
                    case 1:
                        openNestedFragment(new DiscoverEditProfileFragment());
                        break;
                    case 2:
                        openNestedFragment(new DiscoverRecoverPasswordFragment());
                        break;
                    case 3:
                        openNestedFragment(new DiscoverChangePasswordFragment());
                        break;
                    case 4:
                        openNestedFragment(new DiscoverPostItemFragment());
                        break;
                    case 5:
                        openNestedFragment(new DiscoverEditPostFragment());
                        break;
                    case 6:
                        openNestedFragment(new DiscoverDeletePostFragment());
                        break;
                    case 7:
                        openNestedFragment(new DiscoverBecomeDeliverymanFragment());
                        break;
                    case 8:
                        openNestedFragment(new DiscoverHandoverProductFragment());
                        break;
                }
            }
        });

        return binding.getRoot();
    }

    private void openNestedFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragmentContainer, fragment).commit();
    }
}