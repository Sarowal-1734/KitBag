package com.example.kitbag.fragment.container;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.kitbag.R;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.fragment.AboutUsFragment;
import com.example.kitbag.fragment.DiscoverKitBagFragment;
import com.example.kitbag.fragment.TermsAndConditionsFragment;

public class FragmentContainerActivity extends AppCompatActivity {
    private String whatToDo;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (SharedPreference.getDarkModeEnableValue(this)) {
            setTheme(R.style.DarkMode);
        } else {
            setTheme(R.style.LightMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //get whatToDo value send from activity
        whatToDo = getIntent().getStringExtra("whatToDo");
        if (whatToDo.equals("discoverKitBag")) {
            openDiscoverKitBag();
        } else if (whatToDo.equals("termsAndCondition")) {
            openTermsAndCondition();
        } else{
            openAboutUs();
        }
    }
    // Open About Us Fragment
    private void openAboutUs() {
        fragmentTransaction.replace(R.id.fragmentContainer, new AboutUsFragment())
                .commit();
    }

    // Open Terms and Condition Fragment
    private void openTermsAndCondition() {
        fragmentTransaction.replace(R.id.fragmentContainer, new TermsAndConditionsFragment())
                .commit();
    }

    // Open Discover KitBag Fragment
    private void openDiscoverKitBag() {
        fragmentTransaction.replace(R.id.fragmentContainer, new DiscoverKitBagFragment())
                .commit();
    }
}