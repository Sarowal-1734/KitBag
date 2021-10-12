package com.example.kitbag.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.kitbag.chat.fragments.ChatFragment;
import com.example.kitbag.chat.fragments.UserFragment;

public class FragmentAdapter extends FragmentStateAdapter {


    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new ChatFragment();
        }else {
           return new UserFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
