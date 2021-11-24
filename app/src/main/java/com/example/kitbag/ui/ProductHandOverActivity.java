package com.example.kitbag.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;

import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.authentication.OtpVerificationActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityProductHandOverBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductHandOverActivity extends AppCompatActivity {

    private ActivityProductHandOverBinding binding;

    // Show Progress Dialog
    private ProgressDialog progressDialog;

    // For Changing Password
    private EditText editTextOldPassword;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserModel userModel;

    private String receiverContact;
    private String preferredDeliverymanContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (SharedPreference.getDarkModeEnableValue(this)) {
            setTheme(R.style.DarkMode);
        } else {
            setTheme(R.style.LightMode);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityProductHandOverBinding.inflate(getLayoutInflater());
        //loading chosen language as system language
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("Handover Product");

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
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
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
        }

        // Get postStatus info from database and Set Hint On EditText
        init();

        // Initial view of dark mode button in drawer menu
        SwitchCompat switchDarkMode = MenuItemCompat.getActionView(binding.navigationView.getMenu().findItem(R.id.nav_dark_mode)).findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(true);
        // Toggle dark mode button in drawer menu
        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDarkMode.isChecked()) {
                    Toast.makeText(ProductHandOverActivity.this, "Dark Mode Enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductHandOverActivity.this, "Dark Mode Disabled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductHandOverActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // Click profile to open drawer
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // On Send OTP Button Clicked
        binding.buttonSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    if (valid()) {
                        String phoneNumber = binding.cpp.getFullNumberWithPlus().trim();
                        if (binding.SenderToPrimary.isChecked()) {
                            SenderToPrimary(phoneNumber);
                        } else if (binding.PrimaryToDeliveryman.isChecked()) {
                            PrimaryToDeliveryman(phoneNumber);
                        } else if (binding.DeliverymanToFinal.isChecked()) {
                            DeliverymanToFinal(phoneNumber);
                        } else if (binding.FinalToReceiver.isChecked()) {
                            FinalToReceiver(phoneNumber);
                        }
                    }
                } else {
                    showMessageNoConnection();
                }
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(ProductHandOverActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ProductHandOverActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_deliveryman:
                        registerAsDeliveryman();
                        break;
                    case R.id.nav_discover_kitbag:
                        intentFragment.putExtra("whatToDo", "discoverKitBag");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_terms_conditions:
                        intentFragment.putExtra("whatToDo", "termsAndCondition");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_contact:
                        intentFragment.putExtra("whatToDo","contactUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_about:
                        intentFragment.putExtra("whatToDo", "aboutUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(ProductHandOverActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(ProductHandOverActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(ProductHandOverActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProductHandOverActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });

        // Attach full number from edittext with cpp
        binding.cpp.registerCarrierNumberEditText(binding.EditTextContact);

    } // Ending onCreate

    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(ProductHandOverActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                startActivity(new Intent(ProductHandOverActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    private void FinalToReceiver(String phoneNumber) {
        // Check receiver number
        if (phoneNumber.equals(receiverContact)) {
            // Send an OTP to the agent number and verify
            Intent intent = new Intent(ProductHandOverActivity.this, OtpVerificationActivity.class);
            intent.putExtra("whatToDo", "verifyReceiver");
            intent.putExtra("phoneNumber", phoneNumber);
            intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
            startActivity(intent);
            finish();
        } else {
            binding.EditTextContact.setError("Receiver Contact Doesn't Match");
            binding.EditTextContact.requestFocus();
        }
    }

    private void DeliverymanToFinal(String phoneNumber) {
        showProgressBar();
        // Checking the given number is an Agent or not
        db.collection("Users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                if (snapshot.getString("userType").equals("Agent")) {
                                    // Send an OTP to the agent number and verify
                                    Intent intent = new Intent(ProductHandOverActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("whatToDo", "verifyFinalAgent");
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
                                    progressDialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Not an Agent
                                    binding.EditTextContact.setError("Agent Not Found");
                                    binding.EditTextContact.requestFocus();
                                    progressDialog.dismiss();
                                }
                            } else {
                                // Not a User
                                binding.EditTextContact.setError("Agent Not Found");
                                binding.EditTextContact.requestFocus();
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }

    private void PrimaryToDeliveryman(String phoneNumber) {
        if (TextUtils.isEmpty(preferredDeliverymanContact)) {
            ContinuePrimaryToDeliveryman(phoneNumber);
        } else {
            if (phoneNumber.equals(preferredDeliverymanContact)) {
                ContinuePrimaryToDeliveryman(phoneNumber);
            } else {
                binding.EditTextContact.setError("Deliveryman Contact Doesn't Match");
                binding.EditTextContact.requestFocus();
                showDialog();
            }
        }
    }

    private void ContinuePrimaryToDeliveryman(String phoneNumber) {
        showProgressBar();
        // Checking the given number is an Agent or not
        db.collection("Users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                if (snapshot.getString("userType").equals("Deliveryman") || snapshot.getString("userType").equals("Agent")) {
                                    // Send an OTP to the deliveryman number and verify
                                    Intent intent = new Intent(ProductHandOverActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("whatToDo", "verifyDeliveryman");
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
                                    progressDialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Not an Deliveryman
                                    binding.EditTextContact.setError("Deliveryman Not Found");
                                    binding.EditTextContact.requestFocus();
                                    progressDialog.dismiss();
                                }
                            } else {
                                // Not a User
                                binding.EditTextContact.setError("Deliveryman Not Found");
                                binding.EditTextContact.requestFocus();
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }

    private void SenderToPrimary(String phoneNumber) {
        showProgressBar();
        // Checking the given number is an Agent or not
        db.collection("Users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                if (snapshot.getString("userType").equals("Agent")) {
                                    // Send an OTP to the agent number and verify
                                    Intent intent = new Intent(ProductHandOverActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("whatToDo", "verifyPrimaryAgent");
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
                                    progressDialog.dismiss();
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Not an Agent
                                    binding.EditTextContact.setError("Agent Not Found");
                                    binding.EditTextContact.requestFocus();
                                    progressDialog.dismiss();
                                }
                            } else {
                                // Not a User
                                binding.EditTextContact.setError("Agent Not Found");
                                binding.EditTextContact.requestFocus();
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }

    private void showDialog() {
        // Show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductHandOverActivity.this);
        builder.setTitle("Verification Failed!");
        builder.setMessage("A deliveryman has been preferred by the post owner. Now if you want to deliver this product please request the owner to remove preferred deliveryman from this post or replace preferred deliveryman by you.\nThank you");
        builder.setCancelable(false);
        builder.setPositiveButton(
                "Got it",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean valid() {
        if (TextUtils.isEmpty(binding.EditTextContact.getText().toString())) {
            return false;
        }
        return true;
    }

    private void init() {
        db.collection("All_Post")
                .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ModelClassPost modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            receiverContact = modelClassPost.getReceiverPhoneNumber();
                            if (modelClassPost.getStatusCurrent().equals("N/A")) {
                                binding.SenderToPrimary.setEnabled(true);
                                binding.SenderToPrimary.setChecked(true);

                                // setting hint in bangla or english
                                SharedPreferences preferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                                String lang = preferences.getString("my_lang", "bn");
                                if(lang.equals("en")){
                                    binding.EditTextContact.setHint("Agent's Contact");
                                }else {
                                    binding.EditTextContact.setHint("এজেন্টের নাম্বার");
                                }
                            } else if (modelClassPost.getStatusCurrent().equals("Primary_Agent")) {
                                binding.PrimaryToDeliveryman.setEnabled(true);
                                binding.PrimaryToDeliveryman.setChecked(true);
                                // setting hint in bangla or english
                                SharedPreferences preferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                                String lang = preferences.getString("my_lang", "bn");
                                if(lang.equals("en")){
                                    binding.EditTextContact.setHint("Deliveryman's Contact");
                                }else {
                                    binding.EditTextContact.setHint("ডেলিভারিম্যানের নাম্বার");
                                }
                                preferredDeliverymanContact = modelClassPost.getPreferredDeliverymanContact();
                                if (!TextUtils.isEmpty(preferredDeliverymanContact)) {
                                    String phone = preferredDeliverymanContact.substring(4, 14);
                                    binding.EditTextContact.setText(phone);
                                }
                            } else if (modelClassPost.getStatusCurrent().equals("Deliveryman")) {
                                binding.DeliverymanToFinal.setEnabled(true);
                                binding.DeliverymanToFinal.setChecked(true);
                                // setting hint in bangla or english
                                SharedPreferences preferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                                String lang = preferences.getString("my_lang", "bn");
                                if(lang.equals("en")){
                                    binding.EditTextContact.setHint("Agent's Contact");
                                }else {
                                    binding.EditTextContact.setHint("এজেন্টের নাম্বার");
                                }
                            } else if (modelClassPost.getStatusCurrent().equals("Final_Agent")) {
                                binding.FinalToReceiver.setEnabled(true);
                                binding.FinalToReceiver.setChecked(true);
                                String phone = receiverContact.substring(4, 14);
                                binding.EditTextContact.setText(phone);
                                // setting hint in bangla or english
                                SharedPreferences preferences = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                                String lang = preferences.getString("my_lang", "bn");
                                if(lang.equals("en")){
                                    binding.EditTextContact.setHint("Receiver Contact");
                                }else {
                                    binding.EditTextContact.setHint("গ্রহিতার নাম্বার");
                                }
                            }
                        }
                    }
                });
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(ProductHandOverActivity.this).inflate(R.layout.dialog_change_password, null);
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
                progressDialog = new ProgressDialog(ProductHandOverActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                updatePassword(oldPassword, newPassword);
            }
        });
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
                        Toast.makeText(ProductHandOverActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProductHandOverActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // On back pressed
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(ProductHandOverActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Message no connection
    private void showMessageNoConnection() {
        View parentLayout = findViewById(R.id.snackBarContainer);
        // create an instance of the snackBar
        final Snackbar snackbar = Snackbar.make(parentLayout, "", Snackbar.LENGTH_LONG);
        // inflate the custom_snackBar_view created previously
        View customSnackView = getLayoutInflater().inflate(R.layout.snackbar_disconnected, null);
        // set the background of the default snackBar as transparent
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        // now change the layout of the snackBar
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        // set padding of the all corners as 0
        snackbarLayout.setPadding(0, 0, 0, 0);
        // add the custom snack bar layout to snackbar layout
        snackbarLayout.addView(customSnackView, 0);
        snackbar.show();
    }
}