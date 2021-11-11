package com.example.kitbag.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityOtpVerificationBinding;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.ui.MainActivity;
import com.example.kitbag.ui.PostInfoActivity;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtpVerificationActivity extends AppCompatActivity {

    private ActivityOtpVerificationBinding binding;

    private String pinViewOTP, whatToDo, phoneNumber;

    private int OtpID;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // Show progressBar
    private ProgressDialog progressDialog;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Swipe to back
        slidrInterface = Slidr.attach(this);

        // Picking value which send from signUp activity
        whatToDo = getIntent().getStringExtra("whatToDo");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        // Send OTP
        sendOTP();

        // Change the title of the appBar
        if (whatToDo.equals("resetPassword")) {
            binding.customAppBar.appbarTitle.setText("Forgot Password");
            binding.buttonVerify.setText("Verify");
        } else if (whatToDo.equals("verifyFinalAgent") || whatToDo.equals("verifyPrimaryAgent")) {
            binding.customAppBar.appbarTitle.setText("Verify Agent");
            binding.buttonVerify.setText("Verify");
        } else if (whatToDo.equals("verifyDeliveryman")) {
            binding.customAppBar.appbarTitle.setText("Verify Deliveryman");
            binding.buttonVerify.setText("Verify");
        } else if (whatToDo.equals("verifyReceiver")) {
            binding.customAppBar.appbarTitle.setText("Verify Receiver");
            binding.buttonVerify.setText("Verify");
        } else {
            binding.customAppBar.appbarTitle.setText("Sign Up");
        }

        // For Authentication
        mAuth = FirebaseAuth.getInstance();

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);


            // Get userName and image from database and set to the drawer
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setText
                            NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                            View view = navigationView.getHeaderView(0);
                            TextView userName = (TextView) view.findViewById(R.id.nav_user_name);
                            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.nav_user_photo);
                            // set userName to the drawer
                            userName.setText(documentSnapshot.getString("userName"));
                            if (documentSnapshot.getString("imageUrl") != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        } else {
            // No user is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
        }

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Active Inactive Slider to back based on drawer
        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                slidrInterface.lock();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                slidrInterface.unlock();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_login:
                        Toast.makeText(OtpVerificationActivity.this, "Process dismissed!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OtpVerificationActivity.this, LoginActivity.class));
                        finish();
                        break;
                    case R.id.nav_language:
                        Toast.makeText(OtpVerificationActivity.this, "Language", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_discover_kitbag:
                        Toast.makeText(OtpVerificationActivity.this, "Discover KitBag", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_terms_conditions:
                        Toast.makeText(OtpVerificationActivity.this, "Terms And Conditions", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_contact:
                        Toast.makeText(OtpVerificationActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_about:
                        Toast.makeText(OtpVerificationActivity.this, "About Us", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set the phone number in UI
        binding.textViewPhoneNumber.setText("" + phoneNumber);

        // On resend OTP button clicked
        binding.textViewResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });

        // Get the OTP from input field
        binding.pinview.setTextColor(Color.BLACK);
        binding.pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                pinViewOTP = pinview.getValue();
            }
        });
    } // Ending onCreate

    // On Sign in button clicked
    public void onSignInButtonClicked(View view) {
        if (isConnected()) {
            // Show progressBar
            progressDialog = new ProgressDialog(OtpVerificationActivity.this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(false);
            String OTPID = String.valueOf(OtpID);
            if (OTPID.equals(pinViewOTP)) {
                if (whatToDo.equals("registration")) {
                    registerUser();
                } else if (whatToDo.equals("resetPassword")) {
                    progressDialog.dismiss();
                    // Status to check that the password successfully resetted or not
                    //SharedPreference.setPasswordResettedValue(OtpVerificationActivity.this, false);
                    // TODO To Many Things
                    startActivity(new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class));
                    finish();
                } else if (whatToDo.equals("verifyPrimaryAgent")) {
                    // Update status
                    updatePostStatus("Primary_Agent", "statusPrimaryAgent");
                    String message = "Agent successfully verified. Now please handover your item to the Agent.";
                    showDialog(message);
                } else if (whatToDo.equals("verifyDeliveryman")) {
                    // Update status
                    updatePostStatus("Deliveryman", "statusDeliveryman");
                    String message = "Deliveryman successfully verified. Now please handover your item to the Deliveryman.";
                    showDialog(message);
                } else if (whatToDo.equals("verifyFinalAgent")) {
                    // Update status
                    updatePostStatus("Final_Agent", "statusFinalAgent");
                    String message = "Agent successfully verified. Now please handover your item to the Agent.";
                    showDialog(message);
                } else if (whatToDo.equals("verifyReceiver")) {
                    // Update status
                    updatePostStatus("Delivered", "receiverPhoneNumber");
                    String message = "Receiver successfully verified. Now please deliver item to the receiver.";
                    showDialog(message);
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(OtpVerificationActivity.this, "OTP doesn't match!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Hide progressBar
            progressDialog.dismiss();
            showMessageNoConnection();
        }
    }

    private void updatePostStatus(String currentStatus, String statusDeliverymanOrAgentOrReceiver) {
        db.collection("All_Post").document(getIntent().getStringExtra("postReference"))
                .update(
                        "statusCurrent", currentStatus,
                        statusDeliverymanOrAgentOrReceiver, phoneNumber
                );
    }

    private void showDialog(String message) {
        db.collection("All_Post").document(getIntent().getStringExtra("postReference"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();
                        ModelClassPost post = documentSnapshot.toObject(ModelClassPost.class);
                        AlertDialog.Builder builder = new AlertDialog.Builder(OtpVerificationActivity.this);
                        builder.setTitle("Verified!");
                        builder.setMessage(message);
                        builder.setCancelable(false);
                        builder.setPositiveButton(
                                "Got it",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(OtpVerificationActivity.this, PostInfoActivity.class);
                                        intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
                                        intent.putExtra("userId", post.getUserId());
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
    }

    private void sendOTP() {
        String number = phoneNumber.substring(3, 14);
        // Create a background thread to send OTP
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String apiKey = "api_key=" + "jWWu9013if833V1c503DYJs3k61VMDYT3yXy76J9";
                    Random random = new Random();
                    OtpID = random.nextInt(999999);
                    String message = "&msg=" + "Your KitBag OTP is: " + OtpID + "\n";
                    String numbers = "&to=" + number;
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

    private void registerUser() {
        String subPhone = OtpVerificationActivity.this.phoneNumber.substring(1, 14);
        String fakeEmail = subPhone + "@gmail.com";
        mAuth.createUserWithEmailAndPassword(fakeEmail, getIntent().getStringExtra("password"))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            // Store user info in Database
                            UserModel userModel = new UserModel();
                            userModel.setUserId(currentUser.getUid());
                            userModel.setUserName(getIntent().getStringExtra("userName"));
                            userModel.setPhoneNumber(phoneNumber);
                            userModel.setUserType("GENERAL_USER");
                            userModel.setEmail(null);
                            userModel.setDistrict(null);
                            userModel.setUpazilla(null);
                            userModel.setImageUrl(null);

                            collectionReference.document(currentUser.getUid()).set(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Hide progressBar
                                            progressDialog.dismiss();
                                            Toast.makeText(OtpVerificationActivity.this, "Registration Success!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(OtpVerificationActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    });
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    // Close Drawer on back pressed
    @Override
    public void onBackPressed() {
        // progressDialog.dismiss(); //produce bug
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}