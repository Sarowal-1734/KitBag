package com.example.kitbag.ui;

import static android.Manifest.permission.CALL_PHONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.chat.ChatDetailsActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.databinding.ActivityApplicationDeliverymanDetailsBinding;
import com.example.kitbag.effect.ShimmerEffect;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.model.ModelClassDeliveryman;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.notification.FcmNotificationsSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicationDeliverymanDetailsActivity extends AppCompatActivity {

    private ActivityApplicationDeliverymanDetailsBinding binding;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show Progress Diaglog
    private ProgressDialog progressDialog;

    // For Changing Password
    private EditText editTextOldPassword;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private UserModel userModel;

    private String userId;
    private ModelClassDeliveryman user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityApplicationDeliverymanDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initially Check Internet Connection
        if (!isConnected()) {
            displayNoConnection();
        }

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("Application List");

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // remove search icon from appBar
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
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            userModel = documentSnapshot.toObject(UserModel.class);
                            if (userModel.getUserType().equals("Deliveryman") || userModel.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_deliveryman).setVisible(false);
                            }
                            if (userModel.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_agent).setVisible(false);
                            }
                            if (!userModel.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_agent_control).setVisible(false);
                            }
                            if (userModel.getUserType().equals("GENERAL_USER")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_inprogress).setVisible(false);
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
            binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
        }

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ApplicationDeliverymanDetailsActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // Get data from fireStore and set to the recyclerView
        userId = getIntent().getStringExtra("userId");
        displayDetailsInfo();

        binding.buttonApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                approveApplication();
            }
        });

        // Click profile to open drawer
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Open notifications Activity
        binding.customAppBar.appbarNotificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, NotificationsActivity.class));
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(ApplicationDeliverymanDetailsActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, MainActivity.class));
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
                        intentFragment.putExtra("whatToDo", "contactUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_inprogress:
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, InprogressActivity.class));
                        break;
                    case R.id.nav_agent_control:
                        onBackPressed();
                        break;
                    case R.id.nav_about:
                        intentFragment.putExtra("whatToDo", "aboutUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(ApplicationDeliverymanDetailsActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });
    } // Ending onCreate

    private void approveApplication() {
        // Show progressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        updateStatusInUser();
        updateStatusInAgentDeliveryman();
        binding.textViewApplicationStatus.setText("Approved");
        binding.buttonApprove.setEnabled(false);
        sendMessage();
        showConfirmDialog();
        progressDialog.dismiss();
    }

    private void sendMessage() {
        String text = "Congratulations!\n Your account has been successfully updated from General_User to Deliveryman.";
        // Create a background thread to send OTP
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String apiKey = "api_key=" + "jWWu9013if833V1c503DYJs3k61VMDYT3yXy76J9";
                    String message = "&msg=" + text + "\n";
                    String numbers = "&to=" + user.getPhoneNumber();
                    String data = apiKey + message + numbers;
                    HttpURLConnection conn = (HttpURLConnection) new URL("https://api.sms.net.bd/sendsms?").openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                    conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
                    final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    final StringBuilder stringBuffer = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("The user is now a deliveryman")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this,
                                MainActivity.class));
                        finish();
                    }
                })
                .show();
    }

    private void updateStatusInAgentDeliveryman() {
        FirebaseFirestore.getInstance().collection("Deliveryman")
                .document(userId)
                .update(
                        "applicationStatus", "Approved",
                        "userType", "Deliveryman",
                        "approvedByAgent", user.getPhoneNumber());
    }

    private void updateStatusInUser() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .update("userType", "Deliveryman");
    }

    private void displayDetailsInfo() {
        // Show progressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        FirebaseFirestore.getInstance().collection("Deliveryman")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            user = snapshot.toObject(ModelClassDeliveryman.class);
                            binding.textViewUserName.setText(user.getNameEnglish());
                            binding.textViewUserNameBangla.setText(user.getNameBangla());
                            binding.textViewFatherName.setText(user.getFatherHusbandName());
                            binding.textViewMotherName.setText(user.getMotherName());
                            binding.textViewDateOfBirth.setText(user.getDateOfBirth());
                            binding.textViewNIDNumber.setText(user.getNidNumber());
                            binding.textViewPresentAddress.setText(user.getPresentAddress());
                            binding.textViewPostCode.setText(user.getPostCode());
                            binding.textViewPostOffice.setText(user.getPostOffice());
                            binding.textViewUserType.setText(user.getUserType());
                            binding.textViewPhoneNumber.setText(user.getPhoneNumber());
                            binding.textViewThana.setText(user.getThana());
                            binding.textViewDistrict.setText(user.getDistrict());
                            binding.textViewDivision.setText(user.getDivision());
                            binding.textViewGender.setText(user.getGender());
                            binding.textViewUserId.setText(user.getUserId());
                            binding.textViewOccupation.setText(user.getOccupation());
                            if (TextUtils.isEmpty(user.getApprovedByAgent())) {
                                binding.approvedByAgent.setVisibility(View.GONE);
                            }
                            binding.textViewApprovedAgent.setText(user.getApprovedByAgent());
                            binding.textViewApplicationStatus.setText(user.getApplicationStatus());
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(user.getTimeApplied().getSeconds() * 1000);
                            String date = DateFormat.format("hh:mm aa, dd MMM yyyy", cal).toString();
                            binding.textViewJoiningDate.setText(date);
                            progressDialog.dismiss();
                            Glide.with(ApplicationDeliverymanDetailsActivity.this).load(user.getImageUrlUserFace())
                                    .placeholder(ShimmerEffect.get())
                                    .centerCrop()
                                    .into(binding.ImageViewUserPhoto);
                            Glide.with(ApplicationDeliverymanDetailsActivity.this).load(user.getImageUrlFrontNID())
                                    .placeholder(ShimmerEffect.get())
                                    .centerCrop()
                                    .into(binding.frontNID);
                            Glide.with(ApplicationDeliverymanDetailsActivity.this).load(user.getImageUrlBackNID())
                                    .placeholder(ShimmerEffect.get())
                                    .centerCrop()
                                    .into(binding.backNID);
                        }
                    }
                });
    }

    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(ApplicationDeliverymanDetailsActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                startActivity(new Intent(ApplicationDeliverymanDetailsActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(ApplicationDeliverymanDetailsActivity.this).inflate(R.layout.dialog_change_password, null);
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
                progressDialog = new ProgressDialog(ApplicationDeliverymanDetailsActivity.this);
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
                        Toast.makeText(ApplicationDeliverymanDetailsActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ApplicationDeliverymanDetailsActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void displayNoConnection() {
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