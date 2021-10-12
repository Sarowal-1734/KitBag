package com.example.kitbag.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.kitbag.R;
import com.example.kitbag.adapter.FragmentAdapter;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMessageBinding binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(),getLifecycle());
        binding.viewPager.setAdapter(fragmentAdapter);


        new TabLayoutMediator(
                binding.tabLayout,
                binding.viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        //Sets tabs names as mentioned in ViewPagerAdapter fragmentNames array, this can be implemented in many different ways.
                       if(position == 0){
                           tab.setText("CHATS");
                       }else {
                           tab.setText("USERS");
                       }
                    }
                }
        ).attach();

    }
}