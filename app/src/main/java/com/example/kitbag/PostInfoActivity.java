package com.example.kitbag;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.kitbag.databinding.ActivityPostInfoBinding;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
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

        // Display all the info to the activity
        db.collection("All_Post")
                .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            String postedUser, source, destination, chatWith;
                            postedUser = "Posted by " + modelClassPost.getUserName();
                            source = modelClassPost.getFromUpazilla() + ", " + modelClassPost.getFromDistrict();
                            destination = modelClassPost.getToUpazilla() + ", " + modelClassPost.getToDistrict();
                            chatWith = "Chat (" + modelClassPost.getUserName() + ")";
                            binding.textViewTitle.setText(modelClassPost.getTitle());
                            binding.textViewUserTime.setText(postedUser);
                            binding.TextViewDescription.setText(modelClassPost.getDescription());
                            binding.TextViewWeight.setText(modelClassPost.getWeight());
                            binding.TextViewStatus.setText(modelClassPost.getStatus());
                            binding.TextViewSource.setText(source);
                            binding.TextViewDestination.setText(destination);
                            binding.TextViewUserType.setText(modelClassPost.getUserType());
                            binding.TextViewChat.setText(chatWith);
                            binding.TextViewCall.setText(modelClassPost.getPhoneNumber());
                            binding.TextViewMail.setText(modelClassPost.getEmail());
                            // Stop the shimmer effect and display data
                            binding.shimmerContainer.stopShimmer();
                            binding.shimmerContainer.setVisibility(View.GONE);
                            binding.view.setVisibility(View.GONE);
                            // Picasso library for download & show image
                            Picasso.get().load(modelClassPost.getImageUrl()).placeholder(R.drawable.logo).fit().centerInside().into(binding.photoView);
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
            if (getIntent().getStringExtra("fromActivity") != null
                    && getIntent().getStringExtra("fromActivity").equals("MainActivity")
                    && getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                binding.buttonAddToCart.setEnabled(false);
                return;
            }
            if (getIntent().getStringExtra("fromActivity") != null
                    && getIntent().getStringExtra("fromActivity").equals("MainActivity")) {
                db.collection("My_Cart").document(currentUser.getUid())
                        .collection("Cart_Lists")
                        .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (documentSnapshot != null) {
                                        binding.buttonAddToCart.setEnabled(false);
                                    }
                                }
                            }
                        });
                return;
            }
            if (getIntent().getStringExtra("userId").equals(currentUser.getUid())) {
                binding.buttonAddToCart.setText("Edit Post");
                binding.buttonDeleteItem.setVisibility(View.VISIBLE);
                binding.TextViewChat.setEnabled(false);
                binding.TextViewMail.setEnabled(false);
                binding.TextViewCall.setEnabled(false);
            } else {
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