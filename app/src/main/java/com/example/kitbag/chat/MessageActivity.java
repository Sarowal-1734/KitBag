package com.example.kitbag.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kitbag.EditProfileActivity;
import com.example.kitbag.MyCartActivity;
import com.example.kitbag.MyPostActivity;
import com.example.kitbag.NotificationsActivity;
import com.example.kitbag.R;
import com.example.kitbag.adapter.ChatUserAdapter;
import com.example.kitbag.adapter.PostAdapter;
import com.example.kitbag.authentication.LoginActivity;
import com.example.kitbag.databinding.ActivityMessageBinding;
import com.example.kitbag.model.ChatModel;
import com.example.kitbag.model.ModelClassPost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;

    private FirebaseFirestore db;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private String postReference;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // For Changing Password
    private EditText editTextOldPassword;

    // Show progressBar
    private ProgressDialog progressDialog;

    private ModelClassPost modelClassPost;

    private List<ModelClassPost> modelClassPostUserList = new ArrayList<>();
    private ChatUserAdapter adapter;

    // For Pagination
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private int limit = 15;
    private DocumentSnapshot lastVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Authenticate Firebase and Firebase User
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Swipe to back
        slidrInterface = Slidr.attach(this);

        // Customize Toolbar Dynamically
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Chats");

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

        // Set drawer menu based on Login/Logout
        // User will always signed in here
        binding.navigationView.getMenu().clear();
        binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
        // Get userName and image from database and set to the drawer
        db.collection("Users").document(currentUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setText
                        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                        View view = navigationView.getHeaderView(0);
                        TextView userName = (TextView) view.findViewById(R.id.nav_user_name);
                        CircleImageView imageView = (CircleImageView) view.findViewById(R.id.nav_user_photo);
                        userName.setText(documentSnapshot.getString("userName"));
                        if (documentSnapshot.getString("imageUrl") != null) {
                            // Picasso library for download & show image
                            Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                            Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                        }
                    }
                });


        // Swipe from up to bottom to refresh the recyclerView
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeRefreshLayout.setRefreshing(true);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        // setting up adapter
        binding.recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUser.setHasFixedSize(true);
        adapter = new ChatUserAdapter(MessageActivity.this, modelClassPostUserList);
        binding.recyclerViewUser.setAdapter(adapter);

        // Show progressBar
        progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);

        // Get my chat list from Firebase and fireStore and set to the recyclerView
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelClassPostUserList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                        for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                            ChatModel chatModel = snapshot2.getValue(ChatModel.class);
                            if (chatModel.getSender().equals(currentUser.getUid()) || chatModel.getReceiver().equals(currentUser.getUid())) {
                                postReference = dataSnapshot.getKey();
                                db.collection("All_Post")
                                        .whereEqualTo("postReference", postReference)
                                        .limit(8)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                        modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                                                        modelClassPostUserList.add(modelClassPost);
                                                    }
                                                    progressDialog.dismiss();
                                                    adapter.notifyDataSetChanged();
                                                    if (task.getResult().size() > 0) {
                                                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                                    }
                                                    // On recycler item click listener
                                                    adapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(ModelClassPost post) {
                                                            Intent intent = new Intent(MessageActivity.this, ChatDetailsActivity.class);
                                                            intent.putExtra("postReference", post.getPostReference());
                                                            intent.putExtra("userId", post.getUserId());
                                                            intent.putExtra("childKeyUserId", snapshot1.getKey());
                                                            startActivity(intent);
                                                        }
                                                    });

                                                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                                                        @Override
                                                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                                            super.onScrollStateChanged(recyclerView, newState);
                                                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                                                isScrolling = true;
                                                            }
                                                        }

                                                        @Override
                                                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                                            super.onScrolled(recyclerView, dx, dy);

                                                            LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                                                            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                                                            int visibleItemCount = layoutManager.getChildCount();
                                                            int totalItemCount = layoutManager.getItemCount();

                                                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                                                isScrolling = false;
                                                                binding.progressBar.setVisibility(View.VISIBLE);
                                                                Query nextQuery = db.collection("All_Post")
                                                                        .orderBy("timeAdded", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
                                                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                                                        if (t.isSuccessful()) {
                                                                            for (DocumentSnapshot d : t.getResult()) {
                                                                                ModelClassPost modelClassPost = d.toObject(ModelClassPost.class);
                                                                                modelClassPostUserList.add(modelClassPost);
                                                                            }
                                                                            binding.progressBar.setVisibility(View.GONE);
                                                                            adapter.notifyDataSetChanged();
                                                                            if (t.getResult().size() > 0) {
                                                                                lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                                                            }

                                                                            if (t.getResult().size() < limit) {
                                                                                isLastItemReached = true;
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    };
                                                    binding.recyclerViewUser.addOnScrollListener(onScrollListener);
                                                }
                                            }
                                        });
                                break;
                            }
                        }
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        // Open notifications Activity
        findViewById(R.id.appbar_notification_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, NotificationsActivity.class));
            }
        });

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, EditProfileActivity.class));
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_login:
                        startActivity(new Intent(MessageActivity.this, LoginActivity.class));
                        break;
                    case R.id.nav_language:
                        Toast.makeText(MessageActivity.this, "Language", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_discover_kitbag:
                        Toast.makeText(MessageActivity.this, "Discover KitBag", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_terms_conditions:
                        Toast.makeText(MessageActivity.this, "Terms And Conditions", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_contact:
                        Toast.makeText(MessageActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_about:
                        Toast.makeText(MessageActivity.this, "About Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_chat:
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(MessageActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(MessageActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(MessageActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
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


    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(MessageActivity.this).inflate(R.layout.dialog_change_password, null);
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
                progressDialog = new ProgressDialog(MessageActivity.this);
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
                        // Password update successfully
                        dialog.dismiss();
                        progressDialog.dismiss();
                        Toast.makeText(MessageActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MessageActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

}