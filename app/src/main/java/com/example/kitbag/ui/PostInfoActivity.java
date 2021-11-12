package com.example.kitbag.ui;

import static android.Manifest.permission.CALL_PHONE;
import static com.example.kitbag.ui.MainActivity.fromChatDetailsActivity;
import static com.example.kitbag.ui.MainActivity.fromMainActivity;
import static com.example.kitbag.ui.MainActivity.fromMyCartActivity;
import static com.example.kitbag.ui.MainActivity.fromMyPostActivity;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.authentication.LoginActivity;
import com.example.kitbag.chat.ChatDetailsActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.databinding.ActivityPostInfoBinding;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostInfoActivity extends AppCompatActivity {

    private ActivityPostInfoBinding binding;

    private ModelClassPost modelClassPost;
    private UserModel userModel;

    // Swipe to back
    private SlidrInterface slidrInterface;

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

    private boolean removeFromCart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // remove search icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Swipe to back
        slidrInterface = Slidr.attach(this);

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
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            //binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setText
                            // Inactive the delivery button if the user is not an Agent or Deliveryman
                            if (user.getUserType().equals("GENERAL_USER")) {
                                binding.buttonRequestDelivery.setEnabled(false);
                                binding.buttonRequestDelivery.setBackgroundColor(Color.GRAY);
                            }
                            NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                            View view = navigationView.getHeaderView(0);
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
            binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
            // Inactive Delivery Request & Product Handover Button
            binding.buttonRequestDelivery.setEnabled(false);
            binding.buttonRequestDelivery.setBackgroundColor(getResources().getColor(R.color.silver));
        }

        // Inactive Delivery Request Button if My Post
        if (currentUser != null && getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
            binding.buttonRequestDelivery.setEnabled(false);
            binding.buttonRequestDelivery.setBackgroundColor(getResources().getColor(R.color.silver));
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

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostInfoActivity.this, EditProfileActivity.class));
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_login:
                        startActivity(new Intent(PostInfoActivity.this, LoginActivity.class));
                        break;
                    case R.id.nav_language:
                        Toast.makeText(PostInfoActivity.this, "Language", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_discover_kitbag:
                        Toast.makeText(PostInfoActivity.this, "Discover KitBag", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_terms_conditions:
                        Toast.makeText(PostInfoActivity.this, "Terms And Conditions", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_contact:
                        Toast.makeText(PostInfoActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_about:
                        Toast.makeText(PostInfoActivity.this, "About Us", Toast.LENGTH_SHORT).show();
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

        // Display all the info to the activity
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
                                            String postedUser = "Posted by " + userModel.getUserName();
                                            String chatWith = "Chat (" + userModel.getUserName() + ")";
                                            binding.TextViewChat.setText(chatWith);
                                            binding.textViewUserTime.setText(postedUser);
                                            binding.TextViewUserType.setText(userModel.getUserType());
                                            if (userModel.getEmail().equals("")) {
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
                            // Initialize shimmer for loading the image
                            Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                                    .setBaseColor(Color.parseColor("#AEADAD"))
                                    .setBaseAlpha(1)
                                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                                    .setHighlightAlpha(1)
                                    .setDropoff(50)
                                    .build();
                            // Initialize shimmer drawable
                            ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
                            // Set shimmer
                            shimmerDrawable.setShimmer(shimmer);

                            Glide.with(PostInfoActivity.this).load(modelClassPost.getImageUrl())
                                    .placeholder(shimmerDrawable)
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
                        // Stop the shimmer effect and display data
                        binding.shimmerContainer.stopShimmer();
                        binding.shimmerContainer.setVisibility(View.GONE);
                        binding.view.setVisibility(View.GONE);
                    }
                });

        // Set button text AddToCart or DeletePost or inactive AddToCartButton
        if (currentUser != null) {
            if (getIntent().getStringExtra(getOpenFromActivity).equals(fromMainActivity)
                    || getIntent().getStringExtra(getOpenFromActivity).equals(fromChatDetailsActivity)) {
                // Opened from mainActivity
                // Checking this is my post or not
                if (getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                    // This is my post so disable addToCart and remove delete button
                    binding.buttonAddToCart.setEnabled(false);
                }
                // Checking already has this post in my cart or not
                if (getIntent().getStringExtra(getOpenFromActivity).equals(fromMainActivity)) {
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

        // Adding onClickListener on Status text click
        binding.textViewStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(PostInfoActivity.this).inflate(R.layout.product_status_track, null);
                ImageView senderNode = view.findViewById(R.id.imageViewNodeSender);
                ImageView primaryAgentNode = view.findViewById(R.id.imageViewNodePrimaryAgent);
                senderNode.setColorFilter(Color.GREEN);
                view.findViewById(R.id.viewSenderToPrimaryAgent).setBackgroundColor(Color.GREEN);
                primaryAgentNode.setColorFilter(Color.GREEN);
                view.findViewById(R.id.textViewDismiss).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                builder = new AlertDialog.Builder(PostInfoActivity.this);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();
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

    private void enableDisableHandoverButton(ModelClassPost modelClassPost) {
        if (modelClassPost.getStatusCurrent().equals("N/A")) {
            if (currentUser.getUid().equals(modelClassPost.getUserId())) {
                binding.buttonProductHandover.setEnabled(true);
                binding.buttonProductHandover.setBackgroundColor(Color.parseColor("#1754B6"));
            }
        } else if (modelClassPost.getStatusCurrent().equals("Primary_Agent")) {
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user.getPhoneNumber().equals(modelClassPost.getStatusPrimaryAgent())) {
                                binding.buttonProductHandover.setEnabled(true);
                                binding.buttonProductHandover.setBackgroundColor(Color.parseColor("#1754B6"));
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
                                binding.buttonProductHandover.setBackgroundColor(Color.parseColor("#1754B6"));
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
                                binding.buttonProductHandover.setBackgroundColor(Color.parseColor("#1754B6"));
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
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(),oldPassword);
        currentUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // re-authentication successful
                currentUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Password update successfully
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
        } else {
            displayNoConnection();
        }

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
                                    //todo send a notification to the post owner
                                    Toast.makeText(PostInfoActivity.this, "Delivery request sent to post owner", Toast.LENGTH_SHORT).show();
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