package com.example.kitbag.fragment.container;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.authentication.LoginActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityFragmentContainerBinding;
import com.example.kitbag.fragment.AboutUsFragment;
import com.example.kitbag.fragment.ContactUsFragment;
import com.example.kitbag.fragment.TermsAndConditionsFragment;
import com.example.kitbag.fragment.discover_kitbag.DiscoverKitBagFragment;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.ui.EditProfileActivity;
import com.example.kitbag.ui.MyCartActivity;
import com.example.kitbag.ui.MyPostActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentContainerActivity extends AppCompatActivity {

    private ActivityFragmentContainerBinding binding;
    private String whatToDo;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserModel userModel;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show progressBar
    private ProgressDialog progressDialog;

    // For Changing Password
    private EditText editTextOldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (SharedPreference.getDarkModeEnableValue(this)) {
            setTheme(R.style.DarkMode);
        } else {
            setTheme(R.style.LightMode);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityFragmentContainerBinding.inflate(getLayoutInflater());
        loadLocale();
        setContentView(binding.getRoot());

        //get whatToDo value send from activity
        whatToDo = getIntent().getStringExtra("whatToDo");
        if (whatToDo.equals("discoverKitBag")) {
            binding.customAppBar.appbarTitle.setText(R.string.nav_discover_kitbag);
            loadFragment(new DiscoverKitBagFragment());
        } else if (whatToDo.equals("termsAndCondition")) {
            binding.customAppBar.appbarTitle.setText(R.string.terms_and_conditions);
            loadFragment(new TermsAndConditionsFragment());
        } else if (whatToDo.equals("contactUs")) {
            binding.customAppBar.appbarTitle.setText(R.string.nav_contact_us);
            loadFragment(new ContactUsFragment());
        } else {
            binding.customAppBar.appbarTitle.setText(R.string.nav_about_us);
            loadFragment(new AboutUsFragment());
        }

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Hide notify and search
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            // Get userName and image from database and set to the drawer
            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            userModel = documentSnapshot.toObject(UserModel.class);
                            if (userModel.getUserType().equals("Deliveryman") || userModel.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_deliveryman).setVisible(false);
                            }
                            View view = binding.navigationView.getHeaderView(0);
                            TextView userName = (TextView) view.findViewById(R.id.nav_user_name);
                            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.nav_user_photo);
                            userName.setText(userModel.getUserName());
                            if (userModel.getImageUrl() != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        } else {
            // No user is signed in
            binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
        }

        // Click profile to open drawer
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FragmentContainerActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        finish();
                        break;
                    case R.id.nav_login:
                        startActivity(new Intent(FragmentContainerActivity.this, LoginActivity.class));
                        break;
                    case R.id.nav_deliveryman:
                        registerAsDeliveryman();
                        break;
                    case R.id.nav_language:
                        showChangeLanguageDialog();
                        break;
                    case R.id.nav_discover_kitbag:
                        binding.customAppBar.appbarTitle.setText(R.string.nav_discover_kitbag);
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        loadFragment(new DiscoverKitBagFragment());
                        break;
                    case R.id.nav_terms_conditions:
                        binding.customAppBar.appbarTitle.setText(R.string.nav_terms_conditions);
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        loadFragment(new TermsAndConditionsFragment());
                        break;
                    case R.id.nav_contact:
                        binding.customAppBar.appbarTitle.setText(R.string.nav_contact_us);
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        loadFragment(new ContactUsFragment());
                        break;
                    case R.id.nav_about:
                        binding.customAppBar.appbarTitle.setText(R.string.nav_about_us);
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        loadFragment(new AboutUsFragment());
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(FragmentContainerActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(FragmentContainerActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(FragmentContainerActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(FragmentContainerActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        // smoothly reload activity
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }//ending onCreate

    // showing language alert Dialog to pick one language
    private void showChangeLanguageDialog() {
        // inflate custom layout
        View view = LayoutInflater.from(FragmentContainerActivity.this).inflate(R.layout.dialog_select_language, null);
        // Getting view form custom dialog layout
        RadioButton radioButtonBangla = view.findViewById(R.id.rdLanguageBangla);
        RadioButton radioButtonEnglish = view.findViewById(R.id.rdLanguageEnglish);
        if (SharedPreference.getLanguageValue(this).equals("bn")) {
            radioButtonBangla.setChecked(true);
        } else {
            radioButtonEnglish.setChecked(true);
        }
        Button buttonNext = view.findViewById(R.id.buttonNextLanguage);
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButtonBangla.isChecked()) {
                    dialog.dismiss();
                    setLocale("bn");
                    recreate();
                } else {
                    dialog.dismiss();
                    setLocale("en");
                    recreate();
                }
            }
        });
    }

    // setting chosen language to system
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        // Save data to SharedPreference
        SharedPreference.setLanguageValue(this, lang);
    }

    // get save value from sharedPreference and set It to as local language
    public void loadLocale(){
        setLocale(SharedPreference.getLanguageValue(this));
    }


    // Dynamically Replace Fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragmentContainer, fragment).commit();
    }

    // Registration as deliveryman
    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(FragmentContainerActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
        // Getting view form custom dialog layout
        ImageView imageViewNode1 = view.findViewById(R.id.imageViewNode1);
        ImageView imageViewNode2 = view.findViewById(R.id.imageViewNode2);
        ImageView imageViewNode3 = view.findViewById(R.id.imageViewNode3);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonProceed = view.findViewById(R.id.buttonProceed);
        imageViewNode1.setColorFilter(Color.parseColor("#1754B6")); // app_bar color
        imageViewNode2.setColorFilter(Color.parseColor("#1754B6"));
        imageViewNode3.setColorFilter(Color.parseColor("#1754B6"));
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FragmentContainerActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(FragmentContainerActivity.this).inflate(R.layout.dialog_change_password, null);
        // Getting view form custom dialog layout
        editTextOldPassword = view.findViewById(R.id.editTextOldPassword);
        EditText editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
        EditText editTextConfirmNewPassword = view.findViewById(R.id.editTextConfirmNewPassword);
        Button buttonUpdatePassword = view.findViewById(R.id.button_update_password);

        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getting value from user edit text
                String oldPassword = editTextOldPassword.getText().toString().trim();
                String newPassword = editTextNewPassword.getText().toString().trim();
                String conformNewPassword = editTextConfirmNewPassword.getText().toString().toString();

                if (TextUtils.isEmpty(oldPassword)) {
                    editTextOldPassword.setError("Required!");
                    editTextOldPassword.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(newPassword)) {
                    editTextNewPassword.setError("Required!");
                    editTextNewPassword.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(conformNewPassword)) {
                    editTextConfirmNewPassword.setError("Required!");
                    editTextConfirmNewPassword.requestFocus();
                    return;
                }
                if (newPassword.length() < 6) {
                    editTextNewPassword.setError("Length 6 or more");
                    editTextNewPassword.requestFocus();
                    return;
                }
                if (conformNewPassword.length() < 6) {
                    editTextConfirmNewPassword.setError("Length 6 or more");
                    editTextConfirmNewPassword.requestFocus();
                    return;
                }
                if (!newPassword.equals(conformNewPassword)) {
                    editTextConfirmNewPassword.setError("Confirm Password Didn't Match");
                    editTextConfirmNewPassword.requestFocus();
                    return;
                }
                // Show progressBar
                showProgressDialog();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    // Show progress Dialog
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(FragmentContainerActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    // Update Password
    private void updatePassword(String oldPassword, String newPassword) {
        // before updating password we have to re-authenticate our user
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
        currentUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // re-authentication successful
                currentUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Update the password in RealTime Database for ForgotPassword
                        FirebaseDatabase.getInstance().getReference().child("Passwords")
                                .child(userModel.getPhoneNumber().substring(1, 14)).setValue(newPassword);
                        dialog.dismiss();
                        progressDialog.dismiss();
                        Toast.makeText(FragmentContainerActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(FragmentContainerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // re-authentication failed
                progressDialog.dismiss();
                editTextOldPassword.setError("Wrong Password!");
                editTextOldPassword.requestFocus();
            }
        });
    }

    // Close drawer on back pressed
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
            } else {
                getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

}