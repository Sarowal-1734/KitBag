package com.example.kitbag.ui;

import static android.Manifest.permission.CALL_PHONE;
import static com.example.kitbag.ui.MainActivity.fromChatDetailsActivity;
import static com.example.kitbag.ui.MainActivity.fromMainActivity;
import static com.example.kitbag.ui.MainActivity.fromMyCartActivity;
import static com.example.kitbag.ui.MainActivity.fromMyPostActivity;
import static com.example.kitbag.ui.MainActivity.fromOtpVerificationActivity;
import static com.example.kitbag.ui.MainActivity.getOpenFromActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.authentication.LoginActivity;
import com.example.kitbag.authentication.OtpVerificationActivity;
import com.example.kitbag.chat.ChatDetailsActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.databinding.ActivityPostInfoBinding;
import com.example.kitbag.effect.ShimmerEffect;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.notification.FcmNotificationsSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostInfoActivity extends AppCompatActivity {

    private ActivityPostInfoBinding binding;

    private ModelClassPost modelClassPost;
    private UserModel userModel;

    // Show progressBar
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
    private final CollectionReference collectionReference = db.collection("Users");

    // Firebase database For storing message
    private DatabaseReference databaseReference;

    private boolean removeFromCart = false;

    // Status Details
    private View viewStatusDetails;
    // Time
    private TextView senderTime, primaryAgentTime, deliverymanTime, finalAgentTime, receiverTime;
    // Node
    private ImageView senderNode, primaryAgentNode, deliverymanNode, finalAgentNode, receiverNode;
    // TextView Status
    private TextView sender, primaryAgent, deliveryman, finalAgent, receiver;
    // TextView Description
    private TextView senderDescription, primaryAgentDescription, deliverymanDescription, finalAgentDescription, receiverDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityPostInfoBinding.inflate(getLayoutInflater());
        // loading chosen language from multiple language option
        setContentView(binding.getRoot());

        // Initially Check Internet Connection
        if (!isConnected()) {
            displayNoConnection();
        }

        // Remove search icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inflate Status Details Info
        viewStatusDetails = LayoutInflater.from(PostInfoActivity.this).inflate(R.layout.dialog_product_status_track, null);
        builder = new AlertDialog.Builder(PostInfoActivity.this);
        builder.setView(viewStatusDetails);
        dialog = builder.create();

        // Time
        senderTime = viewStatusDetails.findViewById(R.id.textViewSenderTime);
        primaryAgentTime = viewStatusDetails.findViewById(R.id.textViewPrimaryAgentTime);
        deliverymanTime = viewStatusDetails.findViewById(R.id.textViewDeliverymanTime);
        finalAgentTime = viewStatusDetails.findViewById(R.id.textViewFinalAgentTime);
        receiverTime = viewStatusDetails.findViewById(R.id.textViewDeliveredTime);
        // Node
        senderNode = viewStatusDetails.findViewById(R.id.imageViewNodeSender);
        primaryAgentNode = viewStatusDetails.findViewById(R.id.imageViewNodePrimaryAgent);
        deliverymanNode = viewStatusDetails.findViewById(R.id.imageViewNodeDeliveryman);
        finalAgentNode = viewStatusDetails.findViewById(R.id.imageViewNodeSecondaryAgent);
        receiverNode = viewStatusDetails.findViewById(R.id.imageViewNodeReceiver);
        // TextView Status
        sender = viewStatusDetails.findViewById(R.id.textViewSender);
        primaryAgent = viewStatusDetails.findViewById(R.id.textViewPrimaryAgent);
        deliveryman = viewStatusDetails.findViewById(R.id.textViewDeliveryman);
        finalAgent = viewStatusDetails.findViewById(R.id.textViewSecondaryAgent);
        receiver = viewStatusDetails.findViewById(R.id.textViewReceiver);
        // TextView Description
        senderDescription = viewStatusDetails.findViewById(R.id.textViewSenderDescription);
        primaryAgentDescription = viewStatusDetails.findViewById(R.id.textViewPrimaryAgentDescription);
        deliverymanDescription = viewStatusDetails.findViewById(R.id.textViewDeliverymanDescription);
        finalAgentDescription = viewStatusDetails.findViewById(R.id.textViewFinalAgentDescription);
        receiverDescription = viewStatusDetails.findViewById(R.id.textViewDeliveredDescription);

        // Set color on Call and Mail Icon
        binding.imageViewCall.setColorFilter(Color.parseColor("#43AA0C"));
        binding.imageViewMail.setColorFilter(Color.parseColor("#DC6363"));
        binding.imageViewChat.setColorFilter(Color.parseColor("#1754B6"));

        // On notification icon click
        binding.customAppBar.appbarNotificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostInfoActivity.this, NotificationsActivity.class));
            }
        });

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
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
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            // Inactive the delivery button if the user is not an Agent or Deliveryman
                            if (user.getUserType().equals("GENERAL_USER")) {
                                binding.buttonRequestDelivery.setEnabled(false);
                            }
                            if (user.getUserType().equals("Deliveryman") || user.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_deliveryman).setVisible(false);
                            }
                            View view = binding.navigationView.getHeaderView(0);
                            TextView userName = (TextView) view.findViewById(R.id.nav_user_name);
                            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.nav_user_photo);
                            // set userName to the drawer
                            userName.setText(user.getUserName());
                            if (user.getImageUrl() != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                                Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        } else {
            // No user is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
            binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            //hiding language option from drawer
            binding.navigationView.getMenu().findItem(R.id.nav_language).setVisible(false);
            // Inactive Delivery Request & Product Handover Button
            binding.buttonRequestDelivery.setEnabled(false);
        }

        // Inactive Delivery Request Button if My Post
        if (currentUser != null && getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
            binding.buttonRequestDelivery.setEnabled(false);
        }

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Post Informations");

        // Click profile to open drawer
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Dynamically visible request deliver button
        if (getIntent().getStringExtra("statusCurrent").equals("Deliveryman")
                || getIntent().getStringExtra("statusCurrent").equals("Final_Agent")
                || getIntent().getStringExtra("statusCurrent").equals("Delivered")) {
            binding.buttonRequestDelivery.setVisibility(View.GONE);
        }

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostInfoActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(PostInfoActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_login:
                        startActivity(new Intent(PostInfoActivity.this, LoginActivity.class));
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
                    case R.id.nav_about:
                        intentFragment.putExtra("whatToDo", "aboutUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(PostInfoActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(PostInfoActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(PostInfoActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(PostInfoActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });

        // Set button text AddToCart or DeletePost or inactive AddToCartButton
        if (currentUser != null) {
            if (getIntent().getStringExtra(getOpenFromActivity).equals(fromMainActivity)
                    || getIntent().getStringExtra(getOpenFromActivity).equals(fromChatDetailsActivity)
                    || getIntent().getStringExtra(getOpenFromActivity).equals(fromOtpVerificationActivity)) {
                // Checking this is my post or not
                if (getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                    // This is my post so disable addToCart button
                    binding.buttonAddToCart.setEnabled(false);
                } else {
                    // Checking already has this post in my cart or not
                    db.collection("My_Cart").document(currentUser.getUid())
                            .collection("Cart_Lists")
                            .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot != null) {
                                            // Already has this post in my cart
                                            binding.buttonAddToCart.setEnabled(false);
                                        }
                                    }
                                }
                            });
                }
            }
            // This is my post and here came from my_post only. Now edit or delete post
            if (getIntent().getStringExtra(getOpenFromActivity).equals(fromMyPostActivity)
                    && getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                binding.buttonAddToCart.setText("Edit Post");
                binding.buttonDeleteItem.setVisibility(View.VISIBLE);
                // Dynamic Edit Delete Button According to current postStatus
                if (getIntent().getStringExtra("statusCurrent").equals("Deliveryman")
                        || getIntent().getStringExtra("statusCurrent").equals("Final_Agent")
                        || getIntent().getStringExtra("statusCurrent").equals("Delivered")) {
                    // If status changed to deliveryman or later you can't edit or delete post
                    binding.buttonAddToCart.setEnabled(false);
                    binding.buttonDeleteItem.setEnabled(false);
                } else {
                    binding.buttonDeleteItem.setBackgroundColor(getResources().getColor(R.color.red));
                }
                if (getIntent().getStringExtra("statusCurrent").equals("Primary_Agent")) {
                    // If status 'Primary_Agent' you can cancel the delivery
                    binding.buttonDeleteItem.setText("Cancel Delivery");
                }
            }
            // Here came from my_cart. Now remove the item from my cart
            if (getIntent().getStringExtra(getOpenFromActivity).equals(fromMyCartActivity)) {
                db.collection("My_Cart").document(currentUser.getUid())
                        .collection("Cart_Lists")
                        .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (documentSnapshot != null) {
                                        binding.buttonAddToCart.setBackgroundColor(Color.RED);
                                        binding.buttonAddToCart.setText("Remove from My Cart");
                                        removeFromCart = true;
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostInfoActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

        // Display all the info to the activity
        displayPostInfo();

        // Adding onClickListener on Status text click
        binding.textViewStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                // Update delivery status to the dialogView
                updateStatusInfoInDialog();
                progressDialog.dismiss();
            }
        });


        // Adding onClickListener on Call text click
        binding.call.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Toast.makeText(PostInfoActivity.this, "Please Login To Call", Toast.LENGTH_SHORT).show();
                } else if (currentUser.getUid().equals(modelClassPost.getUserId())) {
                    Toast.makeText(PostInfoActivity.this, "Can't call with yourself", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + modelClassPost.getPhoneNumber()));
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    } else {
                        requestPermissions(new String[]{CALL_PHONE}, 1);
                    }
                }
            }
        });

        // Adding onClickListener on Chat text click
        binding.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Toast.makeText(PostInfoActivity.this, "Please Login To Chat", Toast.LENGTH_SHORT).show();
                } else if (currentUser.getUid().equals(modelClassPost.getUserId())) {
                    Toast.makeText(PostInfoActivity.this, "Can't Chat With Yourself", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(PostInfoActivity.this, ChatDetailsActivity.class);
                    // sending Post Id for Chatting
                    intent.putExtra("postReference", modelClassPost.getPostReference());
                    intent.putExtra("userId", modelClassPost.getUserId());
                    intent.putExtra("childKeyUserId", currentUser.getUid());
                    startActivity(intent);
                }
            }
        });

        // Adding onClickListener on Mail text click
        binding.imageIconMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Toast.makeText(PostInfoActivity.this, "Please Login To Send Mail", Toast.LENGTH_SHORT).show();
                } else if (currentUser.getUid().equals(modelClassPost.getUserId())) {
                    Toast.makeText(PostInfoActivity.this, "Can't Mail", Toast.LENGTH_SHORT).show();
                } else {
                    String subject = "Providing Delivery Service In KitBag";
                    String body = "Hi! I am a deliveryman. I found that you are going to deliver a product. Here as deliveryman," +
                            " I can deliver your product. If you are interested then please check the inbox in KitBag Chat option.";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + userModel.getEmail()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(emailIntent, "KitBag Courier Service"));
                }
            }
        });

    } // Ending onCreate

    // Display all the info to the activity
    private void displayPostInfo() {
        db.collection("All_Post")
                .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            String source, destination;
                            source = modelClassPost.getFromUpazilla() + ", " + modelClassPost.getFromDistrict();
                            destination = modelClassPost.getToUpazilla() + ", " + modelClassPost.getToDistrict();
                            binding.textViewTitle.setText(modelClassPost.getTitle());
                            binding.TextViewDescription.setText(modelClassPost.getDescription());
                            binding.TextViewWeight.setText(modelClassPost.getWeight());
                            binding.TextViewStatus.setText(modelClassPost.getStatusCurrent());
                            binding.TextViewSource.setText(source);
                            binding.TextViewDestination.setText(destination);
                            binding.TextViewCall.setText(modelClassPost.getPhoneNumber());
                            db.collection("Users").document(modelClassPost.getUserId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            userModel = documentSnapshot.toObject(UserModel.class);
                                            if (userModel.getUserType().equals("Agent")) {
                                                binding.navigationView.getMenu().findItem(R.id.nav_agent).setVisible(false);
                                            }
                                            if (currentUser != null && !userModel.getUserType().equals("Agent")) {
                                                binding.navigationView.getMenu().findItem(R.id.nav_agent_control).setVisible(false);
                                            }
                                            if (currentUser != null && userModel.getUserType().equals("GENERAL_USER")) {
                                                binding.navigationView.getMenu().findItem(R.id.nav_inprogress).setVisible(false);
                                            }
                                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                            cal.setTimeInMillis(modelClassPost.getTimeAdded().getSeconds() * 1000);
                                            String postedUserTime;
                                            postedUserTime = "Posted by " + userModel.getUserName() + " on ";
                                            postedUserTime += DateFormat.format("dd MMM hh:mm a", cal).toString();
                                            ;
                                            String chatWith = "Chat (" + userModel.getUserName() + ")";
                                            binding.TextViewChat.setText(chatWith);
                                            binding.textViewUserTime.setText(postedUserTime);
                                            binding.TextViewUserType.setText(userModel.getUserType());
                                            if (TextUtils.isEmpty(userModel.getEmail())) {
                                                binding.imageIconMail.setVisibility(View.GONE);
                                            } else {
                                                binding.TextViewMail.setText(userModel.getEmail());
                                            }
                                            // Inactive Product Delivery Process According to currentPostStatus
                                            if (currentUser != null) {
                                                enableDisableHandoverButton(modelClassPost);
                                            }
                                        }
                                    });
                            // Stop the shimmer effect and display data
                            binding.shimmerContainer.stopShimmer();
                            binding.shimmerContainer.setVisibility(View.GONE);
                            binding.view.setVisibility(View.GONE);

                            Glide.with(PostInfoActivity.this).load(modelClassPost.getImageUrl())
                                    .placeholder(ShimmerEffect.get())
                                    .into(binding.photoView);
                            return;
                        }
                        // Stop the shimmer effect and display data
                        binding.shimmerContainer.stopShimmer();
                        binding.shimmerContainer.setVisibility(View.GONE);
                        binding.view.setVisibility(View.GONE);
                        binding.textViewTitle.setText("This post has been deleted by the owner! Now please remove this post from your cart.");
                        binding.textViewTitle.setGravity(Gravity.CENTER);
                        binding.textViewTitle.setTextColor(Color.RED);
                        binding.textViewUserTime.setVisibility(View.GONE);
                        binding.cardViewPhoto.setVisibility(View.GONE);
                        binding.TextViewDescriptionLayout.setVisibility(View.GONE);
                        binding.weightStatus.setVisibility(View.GONE);
                        binding.fromTo.setVisibility(View.GONE);
                        binding.typeChat.setVisibility(View.GONE);
                        binding.callEmail.setVisibility(View.GONE);
                    }
                });
    }

    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(PostInfoActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                startActivity(new Intent(PostInfoActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    private void updateStatusInfoInDialog() {
        if (getIntent().getStringExtra("statusCurrent").equals("N/A")) {
            updateSenderInfo();
        } else if (getIntent().getStringExtra("statusCurrent").equals("Primary_Agent")) {
            updateSenderInfo();
            updatePrimaryAgentInfo();
        } else if (getIntent().getStringExtra("statusCurrent").equals("Deliveryman")) {
            updateSenderInfo();
            updatePrimaryAgentInfo();
            updateDeliverymanInfo();
        } else if (getIntent().getStringExtra("statusCurrent").equals("Final_Agent")) {
            updateSenderInfo();
            updatePrimaryAgentInfo();
            updateDeliverymanInfo();
            updateFinalAgentInfo();
        } else if (getIntent().getStringExtra("statusCurrent").equals("Delivered")) {
            updateSenderInfo();
            updatePrimaryAgentInfo();
            updateDeliverymanInfo();
            updateFinalAgentInfo();
            updateDeliveredInfo();
        }
        viewStatusDetails.findViewById(R.id.textViewDismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateDeliveredInfo() {
        viewStatusDetails.findViewById(R.id.viewTargetAgentToReceiver).setBackgroundColor(Color.parseColor("#1754B6"));
        receiverNode.setColorFilter(Color.parseColor("#1754B6")); // app_bar_color
        receiverTime.setText(getDateTimeFormat(modelClassPost.getStatusReceiverPhoneNumberTime()));
        setName(receiver, "Delivered", modelClassPost.getReceiverPhoneNumber());
        receiver.setTextColor(Color.parseColor("#1754B6"));
        receiverDescription.setTextColor(Color.parseColor("#1754B6"));
    }

    private void updateFinalAgentInfo() {
        viewStatusDetails.findViewById(R.id.viewDeliverymanToTargetAgent).setBackgroundColor(Color.parseColor("#1754B6"));
        finalAgentNode.setColorFilter(Color.parseColor("#1754B6")); // app_bar_color
        finalAgentTime.setText(getDateTimeFormat(modelClassPost.getStatusFinalAgentTime()));
        setName(finalAgent, "Final Agent", modelClassPost.getStatusFinalAgent());
        finalAgent.setTextColor(Color.parseColor("#1754B6"));
        finalAgentDescription.setTextColor(Color.parseColor("#1754B6"));
    }

    private void updateDeliverymanInfo() {
        viewStatusDetails.findViewById(R.id.viewPrimaryToDeliveryman).setBackgroundColor(Color.parseColor("#1754B6"));
        deliverymanNode.setColorFilter(Color.parseColor("#1754B6")); // app_bar_color
        deliverymanTime.setText(getDateTimeFormat(modelClassPost.getStatusDeliverymanTime()));
        setName(deliveryman, "Deliveryman", modelClassPost.getStatusDeliveryman());
        deliveryman.setTextColor(Color.parseColor("#1754B6"));
        deliverymanDescription.setTextColor(Color.parseColor("#1754B6"));
    }

    private void updatePrimaryAgentInfo() {
        viewStatusDetails.findViewById(R.id.viewSenderToPrimaryAgent).setBackgroundColor(Color.parseColor("#1754B6"));
        primaryAgentNode.setColorFilter(Color.parseColor("#1754B6")); // app_bar_color
        primaryAgentTime.setText(getDateTimeFormat(modelClassPost.getStatusPrimaryAgentTime()));
        setName(primaryAgent, "Primary Agent", modelClassPost.getStatusPrimaryAgent());
        primaryAgent.setTextColor(Color.parseColor("#1754B6"));
        primaryAgentDescription.setTextColor(Color.parseColor("#1754B6"));
    }

    private void updateSenderInfo() {
        senderTime.setText(getDateTimeFormat(modelClassPost.getTimeAdded()));
        senderNode.setColorFilter(Color.parseColor("#1754B6")); // app_bar_color
        setName(sender, "Sender", modelClassPost.getPhoneNumber());
        sender.setTextColor(Color.parseColor("#1754B6"));
        senderDescription.setTextColor(Color.parseColor("#1754B6"));
    }

    private void setName(TextView textViewName, String statusName, String statusNumber) {
        collectionReference.whereEqualTo("phoneNumber", statusNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            UserModel userModel = snapshot.toObject(UserModel.class);
                            textViewName.setText(statusName + " (" + userModel.getUserName() + ")");
                            textViewName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (currentUser != null) {
                                        Intent intent = new Intent(PostInfoActivity.this, EditProfileActivity.class);
                                        intent.putExtra("userId", userModel.getUserId());
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(PostInfoActivity.this, "Please login", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            return;
                        }
                    }
                });
    }

    private String getDateTimeFormat(Timestamp timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timeStamp.getSeconds() * 1000);
        return DateFormat.format("dd MMM\nhh:mm a", cal).toString();
    }

    private void enableDisableHandoverButton(ModelClassPost modelClassPost) {
        if (modelClassPost.getStatusCurrent().equals("N/A")) {
            if (currentUser.getUid().equals(modelClassPost.getUserId())) {
                binding.buttonProductHandover.setEnabled(true);
            }
        } else if (modelClassPost.getStatusCurrent().equals("Primary_Agent")) {
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user.getPhoneNumber().equals(modelClassPost.getStatusPrimaryAgent())) {
                                binding.buttonProductHandover.setEnabled(true);
                            }
                        }
                    });
        } else if (modelClassPost.getStatusCurrent().equals("Deliveryman")) {
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user.getPhoneNumber().equals(modelClassPost.getStatusDeliveryman())) {
                                binding.buttonProductHandover.setEnabled(true);
                            }
                        }
                    });
        } else if (modelClassPost.getStatusCurrent().equals("Final_Agent")) {
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user.getPhoneNumber().equals(modelClassPost.getStatusFinalAgent())) {
                                binding.buttonProductHandover.setEnabled(true);
                            }
                        }
                    });
        } else {
            binding.buttonProductHandover.setVisibility(View.GONE);
        }
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(PostInfoActivity.this).inflate(R.layout.dialog_change_password, null);
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
                progressDialog = new ProgressDialog(PostInfoActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                updatePassword(oldPassword, newPassword);
            }
        });
    }


    // Update password
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
                        Toast.makeText(PostInfoActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostInfoActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // Close Drawer on back pressed
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        if (getIntent().getStringExtra(getOpenFromActivity).equals(fromOtpVerificationActivity)) {
            startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
            return;
        }
        super.onBackPressed();
    }

    // On add to cart button click
    public void onAddToCartButtonClick(View view) {
        if (isConnected()) {
            if (currentUser != null) {
                if (getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                    Intent intent = new Intent(PostInfoActivity.this, PostActivity.class);
                    intent.putExtra("whatToDo", "EditPost");
                    intent.putExtra("postReference", getIntent().getStringExtra("postReference"));
                    intent.putExtra("statusCurrent", getIntent().getStringExtra("statusCurrent"));
                    startActivity(intent);
                    return;
                }
                if (removeFromCart) {
                    if (isConnected()) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(PostInfoActivity.this);
                        ab.setTitle("Are you sure to remove this item?");
                        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                showProgressBar();
                                db.collection("My_Cart").document(currentUser.getUid()).collection("Cart_Lists")
                                        .document(getIntent().getStringExtra("documentReference"))
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(PostInfoActivity.this, "Item successfully removed from your cart", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(PostInfoActivity.this, "Some error occurred!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ab.show();
                    } else {
                        displayNoConnection();
                    }
                    return;
                }
                showProgressBar();
                // Get all the Post_Info to store in Cart
                db.collection("All_Post")
                        .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    ModelClassPost modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                                    assert modelClassPost != null;
                                    db.collection("My_Cart").document(currentUser.getUid()).collection("Cart_Lists")
                                            .add(modelClassPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // Storing the document reference for easy to delete later from my cart
                                            db.collection("My_Cart").document(currentUser.getUid())
                                                    .collection("Cart_Lists").document(documentReference.getId())
                                                    .update("documentReference", documentReference.getId());
                                            progressDialog.dismiss();
                                            Toast.makeText(PostInfoActivity.this, "Successfully added to your cart", Toast.LENGTH_SHORT).show();
                                            binding.buttonAddToCart.setEnabled(false);
                                        }
                                    });
                                }
                            }
                        });
            } else {
                Toast.makeText(PostInfoActivity.this, "Please login first", Toast.LENGTH_SHORT).show();
            }
        } else {
            displayNoConnection();
        }
    }

    // On delete button clicked
    public void onDeletePostButtonClick(View view) {
        if (isConnected()) {
            if (getIntent().getStringExtra("statusCurrent").equals("Primary_Agent")) {
                // If status 'Primary_Agent' you can cancel the delivery
                cancelDelivery();
            } else {
                deletePost();
            }
        } else {
            displayNoConnection();
        }
    }

    private void cancelDelivery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostInfoActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("An OTP will be sent to the Agent and after successful verification you can take back your product from the Agent.");
        builder.setPositiveButton(
                "Got it",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        // Cancel the delivery (Status: Primary_Agent to N/A)
                        Intent intent = new Intent(PostInfoActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("whatToDo", "cancelDelivery");
                        intent.putExtra("phoneNumber", modelClassPost.getStatusPrimaryAgent());
                        intent.putExtra("postReference", modelClassPost.getPostReference());
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePost() {
        AlertDialog.Builder ab = new AlertDialog.Builder(PostInfoActivity.this);
        ab.setTitle("Are you sure you want to delete this post?");
        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showProgressBar();
                db.collection("All_Post").document(modelClassPost.getPostReference()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(modelClassPost.getImageUrl());
                                storageReference.delete();
                                progressDialog.dismiss();
                                Toast.makeText(PostInfoActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PostInfoActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(PostInfoActivity.this, "Some error occurred!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ab.show();
    }

    // On Request To delivery button clicked
    public void onRequestDeliveryButtonClick(View view) {
        if (isConnected()) {
            if (currentUser != null) {
                db.collection("Users").document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //model = documentSnapshot.toObject(UserModel.class);
                                String userType = documentSnapshot.getString("userType");
                                if (userType.equals("Deliveryman") || userType.equals("Agent")) {
                                    sendTextMessageWithNotification();
                                    binding.buttonRequestDelivery.setEnabled(false);
                                } else {
                                    becomeDeliveryMan();
                                }
                            }
                        });
            }
        } else {
            displayNoConnection();
        }
    }

    // On Request Delivery Button Clicked
    private void sendTextMessageWithNotification() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String title = "You have a new text message";
        String message = "Hi! I want to deliver your product";
        // Store message in Database
        ChatModel chatModel = new ChatModel();
        chatModel.setSender(currentUser.getUid());
        chatModel.setReceiver(modelClassPost.getUserId());
        chatModel.setMessage(message);
        chatModel.setStatus("Sent");
        databaseReference.child("Chats").child(modelClassPost.getPostReference()).child(currentUser.getUid()).push().setValue(chatModel);
        // Sent Notification
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userModel.getUserToken(),
                modelClassPost.getUserId(), title, message, getApplicationContext(), PostInfoActivity.this);
        notificationsSender.SendNotifications();
    }

    public void onProductHandoverButtonClick(View view) {
        Intent intent = new Intent(PostInfoActivity.this, ProductHandOverActivity.class);
        intent.putExtra("postReference", modelClassPost.getPostReference());
        startActivity(intent);
    }

    private void becomeDeliveryMan() {
        // inflate custom layout
        View view = LayoutInflater.from(PostInfoActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
        // Getting view form custom dialog layout
        ImageView imageViewNode1 = view.findViewById(R.id.imageViewNode1);
        ImageView imageViewNode2 = view.findViewById(R.id.imageViewNode2);
        ImageView imageViewNode3 = view.findViewById(R.id.imageViewNode3);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonProceed = view.findViewById(R.id.buttonProceed);
        imageViewNode1.setColorFilter(Color.parseColor("#1754B6")); // app_bar color
        imageViewNode2.setColorFilter(Color.parseColor("#1754B6"));
        imageViewNode3.setColorFilter(Color.parseColor("#1754B6"));
        builder = new AlertDialog.Builder(PostInfoActivity.this);
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
                startActivity(new Intent(PostInfoActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    // ProgressBar Setup
    private void showProgressBar() {
        // Show progressBar
        progressDialog = new ProgressDialog(PostInfoActivity.this);
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