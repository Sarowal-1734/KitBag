package com.example.kitbag.ui;

import static com.example.kitbag.ui.MainActivity.fromMyCartActivity;
import static com.example.kitbag.ui.MainActivity.getOpenFromActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kitbag.R;
import com.example.kitbag.adapter.PostAdapter;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.databinding.ActivityMyCartBinding;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyCartActivity extends AppCompatActivity {

    private ActivityMyCartBinding binding;
    private AutoCompleteTextView editTextFromDistrict, editTextFromUpazila, editTextToDistrict, editTextToUpazila;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show Progress Diaglog
    private ProgressDialog progressDialog;

    // For Changing Password
    private EditText editTextOldPassword;

    // For Pagination
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private DocumentSnapshot lastVisible;
    ArrayList<ModelClassPost> postList = new ArrayList<>();

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("My Cart");

        // Swipe to back
        slidrInterface = Slidr.attach(this);

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
            binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_logout);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.GONE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.GONE);
        }

        // Initial view of dark mode button in drawer menu
        SwitchCompat switchDarkMode = MenuItemCompat.getActionView(binding.navigationView.getMenu().findItem(R.id.nav_dark_mode)).findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(true);
        // Toggle dark mode button in drawer menu
        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDarkMode.isChecked()) {
                    Toast.makeText(MyCartActivity.this, "Dark Mode Enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyCartActivity.this, "Dark Mode Disabled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyCartActivity.this, EditProfileActivity.class));
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

        // Get data from fireStore and set to the recyclerView
        PostAdapter postAdapter = new PostAdapter(MyCartActivity.this, postList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MyCartActivity.this, 2, GridLayoutManager.VERTICAL, false);
        binding.recyclerViewPostLists.setLayoutManager(gridLayoutManager);
        binding.recyclerViewPostLists.setAdapter(postAdapter);

        // Show progressBar
        progressDialog = new ProgressDialog(MyCartActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);

        db.collection("My_Cart").document(currentUser.getUid())
                .collection("Cart_Lists")
                .orderBy("timeAdded", Query.Direction.DESCENDING)
                .limit(8)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                ModelClassPost modelClassPost = document.toObject(ModelClassPost.class);
                                postList.add(modelClassPost);
                            }
                            progressDialog.dismiss();
                            postAdapter.notifyDataSetChanged();
                            if (task.getResult().size() > 0) {
                                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            }
                            // On recycler item click listener
                            postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(ModelClassPost post) {
                                    Intent intent = new Intent(MyCartActivity.this, PostInfoActivity.class);
                                    intent.putExtra("userId", post.getUserId());
                                    intent.putExtra("postReference", post.getPostReference());
                                    intent.putExtra("documentReference", post.getDocumentReference());
                                    intent.putExtra(getOpenFromActivity, fromMyCartActivity);
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

                                    GridLayoutManager gridLayoutManager1 = ((GridLayoutManager) recyclerView.getLayoutManager());
                                    int firstVisibleItemPosition = gridLayoutManager1.findFirstVisibleItemPosition();
                                    int visibleItemCount = gridLayoutManager1.getChildCount();
                                    int totalItemCount = gridLayoutManager1.getItemCount();

                                    if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                        isScrolling = false;
                                        binding.progressBar.setVisibility(View.VISIBLE);
                                        Query nextQuery = db.collection("My_Cart").document(currentUser.getUid())
                                                .collection("Cart_Lists")
                                                .orderBy("timeAdded", Query.Direction.DESCENDING).startAfter(lastVisible).limit(8);
                                        nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                                if (t.isSuccessful()) {
                                                    for (DocumentSnapshot d : t.getResult()) {
                                                        ModelClassPost modelClassPost = d.toObject(ModelClassPost.class);
                                                        postList.add(modelClassPost);
                                                    }
                                                    binding.progressBar.setVisibility(View.GONE);
                                                    postAdapter.notifyDataSetChanged();
                                                    if (t.getResult().size() > 0) {
                                                        lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                                    }

                                                    if (t.getResult().size() < 8) {
                                                        isLastItemReached = true;
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            };
                            binding.recyclerViewPostLists.addOnScrollListener(onScrollListener);
                        }
                    }
                });

        // Open notifications Activity
        findViewById(R.id.appbar_notification_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyCartActivity.this, NotificationsActivity.class));
            }
        });

        // Click profile to open drawer
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // On Edit profile icon clicked
        View view1 = binding.navigationView.getHeaderView(0);
        ImageView imageView1 = view1.findViewById(R.id.nav_edit_profile);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyCartActivity.this, EditProfileActivity.class));
            }
        });

        // On search button click
        binding.customAppBar.appbarImageviewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Inflate Custom layout for searching
                LayoutInflater inflater = MyCartActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_search_dialog, null);

                // Create Dialog Builder
                AlertDialog.Builder ab = new AlertDialog.Builder(MyCartActivity.this);

                // Init the editText of the custom dialog box
                editTextFromDistrict = dialogView.findViewById(R.id.EditTextFromDistrict);
                editTextFromUpazila = dialogView.findViewById(R.id.EditTextFromUpazila);
                editTextToDistrict = dialogView.findViewById(R.id.EditTextToDistrict);
                editTextToUpazila = dialogView.findViewById(R.id.EditTextToUpazila);

                //setAdapter on District and Upazila
                setDistrictUpazilaOnEditText();

                //Setting positive "Ok" Button
                ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here...
                    }
                });
                //Setting Negative "Cancel" Button
                ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
                ab.setCancelable(false);
                ab.setView(dialogView);
                ab.show();
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_language:
                        Toast.makeText(MyCartActivity.this, "Language", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_discover_kitbag:
                        Toast.makeText(MyCartActivity.this, "Discover KitBag", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_terms_conditions:
                        Toast.makeText(MyCartActivity.this, "Terms And Conditions", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_contact:
                        Toast.makeText(MyCartActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_about:
                        Toast.makeText(MyCartActivity.this, "About Us", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(MyCartActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(MyCartActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(MyCartActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MyCartActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(MyCartActivity.this).inflate(R.layout.dialog_change_password,null);
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
                progressDialog = new ProgressDialog(MyCartActivity.this);
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
                        Toast.makeText(MyCartActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyCartActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // District and Upazila Recommendation
    private void setDistrictUpazilaOnEditText() {
        // District Recommendation
        String[] districts = getResources().getStringArray(R.array.Districts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MyCartActivity.this, android.R.layout.simple_list_item_1, districts);
        editTextFromDistrict.setAdapter(adapter);  // District
        editTextToDistrict.setAdapter(adapter);    // District

        // UpazilaFrom Recommendation
        editTextFromDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazilas = null;
                if (district.equals("Bagerhat")) {
                    upazilas = getResources().getStringArray(R.array.Bagerhat);
                } else if (district.equals("Bandarban")) {
                    upazilas = getResources().getStringArray(R.array.Bandarban);
                } else if (district.equals("Barguna")) {
                    upazilas = getResources().getStringArray(R.array.Barguna);
                } else if (district.equals("Barisal")) {
                    upazilas = getResources().getStringArray(R.array.Barisal);
                } else if (district.equals("Bhola")) {
                    upazilas = getResources().getStringArray(R.array.Bhola);
                } else if (district.equals("Bogra")) {
                    upazilas = getResources().getStringArray(R.array.Bogra);
                } else if (district.equals("Brahmanbaria")) {
                    upazilas = getResources().getStringArray(R.array.Brahmanbaria);
                } else if (district.equals("Chandpur")) {
                    upazilas = getResources().getStringArray(R.array.Chandpur);
                } else if (district.equals("Chapainawabganj")) {
                    upazilas = getResources().getStringArray(R.array.Chapainawabganj);
                } else if (district.equals("Chittagong")) {
                    upazilas = getResources().getStringArray(R.array.Chittagong);
                } else if (district.equals("Chuadanga")) {
                    upazilas = getResources().getStringArray(R.array.Chuadanga);
                } else if (district.equals("Cox's Bazar")) {
                    upazilas = getResources().getStringArray(R.array.CoxBazar);
                } else if (district.equals("Comilla")) {
                    upazilas = getResources().getStringArray(R.array.Comilla);
                } else if (district.equals("Dhaka")) {
                    upazilas = getResources().getStringArray(R.array.Dhaka);
                } else if (district.equals("Dinajpur")) {
                    upazilas = getResources().getStringArray(R.array.Dinajpur);
                } else if (district.equals("Faridpur")) {
                    upazilas = getResources().getStringArray(R.array.Faridpur);
                } else if (district.equals("Feni")) {
                    upazilas = getResources().getStringArray(R.array.Feni);
                } else if (district.equals("Gaibandha")) {
                    upazilas = getResources().getStringArray(R.array.Gaibandha);
                } else if (district.equals("Gazipur")) {
                    upazilas = getResources().getStringArray(R.array.Gazipur);
                } else if (district.equals("Gopalganj")) {
                    upazilas = getResources().getStringArray(R.array.Gopalganj);
                } else if (district.equals("Habiganj")) {
                    upazilas = getResources().getStringArray(R.array.Habiganj);
                } else if (district.equals("Joypurhat")) {
                    upazilas = getResources().getStringArray(R.array.Joypurhat);
                } else if (district.equals("Jamalpur")) {
                    upazilas = getResources().getStringArray(R.array.Jamalpur);
                } else if (district.equals("Jessore")) {
                    upazilas = getResources().getStringArray(R.array.Jessore);
                } else if (district.equals("Jhalokati")) {
                    upazilas = getResources().getStringArray(R.array.Jhalokati);
                } else if (district.equals("Jhenaidah")) {
                    upazilas = getResources().getStringArray(R.array.Jhenaidah);
                } else if (district.equals("Khagrachari")) {
                    upazilas = getResources().getStringArray(R.array.Khagrachari);
                } else if (district.equals("Khulna")) {
                    upazilas = getResources().getStringArray(R.array.Khulna);
                } else if (district.equals("Kishoreganj")) {
                    upazilas = getResources().getStringArray(R.array.Kishoreganj);
                } else if (district.equals("Kurigram")) {
                    upazilas = getResources().getStringArray(R.array.Kurigram);
                } else if (district.equals("Kushtia")) {
                    upazilas = getResources().getStringArray(R.array.Kushtia);
                } else if (district.equals("Lakshmipur")) {
                    upazilas = getResources().getStringArray(R.array.Lakshmipur);
                } else if (district.equals("Lalmonirhat")) {
                    upazilas = getResources().getStringArray(R.array.Lalmonirhat);
                } else if (district.equals("Madaripur")) {
                    upazilas = getResources().getStringArray(R.array.Madaripur);
                } else if (district.equals("Magura")) {
                    upazilas = getResources().getStringArray(R.array.Magura);
                } else if (district.equals("Manikganj")) {
                    upazilas = getResources().getStringArray(R.array.Manikganj);
                } else if (district.equals("Meherpur")) {
                    upazilas = getResources().getStringArray(R.array.Meherpur);
                } else if (district.equals("Moulvibazar")) {
                    upazilas = getResources().getStringArray(R.array.Moulvibazar);
                } else if (district.equals("Munshiganj")) {
                    upazilas = getResources().getStringArray(R.array.Munshiganj);
                } else if (district.equals("Mymensingh")) {
                    upazilas = getResources().getStringArray(R.array.Mymensingh);
                } else if (district.equals("Naogaon")) {
                    upazilas = getResources().getStringArray(R.array.Naogaon);
                } else if (district.equals("Narail")) {
                    upazilas = getResources().getStringArray(R.array.Narail);
                } else if (district.equals("Narayanganj")) {
                    upazilas = getResources().getStringArray(R.array.Narayanganj);
                } else if (district.equals("Narsingdi")) {
                    upazilas = getResources().getStringArray(R.array.Narsingdi);
                } else if (district.equals("Natore")) {
                    upazilas = getResources().getStringArray(R.array.Natore);
                } else if (district.equals("Netrakona")) {
                    upazilas = getResources().getStringArray(R.array.Netrakona);
                } else if (district.equals("Nilphamari")) {
                    upazilas = getResources().getStringArray(R.array.Nilphamari);
                } else if (district.equals("Noakhali")) {
                    upazilas = getResources().getStringArray(R.array.Noakhali);
                } else if (district.equals("Pabna")) {
                    upazilas = getResources().getStringArray(R.array.Pabna);
                } else if (district.equals("Panchagarh")) {
                    upazilas = getResources().getStringArray(R.array.Panchagarh);
                } else if (district.equals("Patuakhali")) {
                    upazilas = getResources().getStringArray(R.array.Patuakhali);
                } else if (district.equals("Pirojpur")) {
                    upazilas = getResources().getStringArray(R.array.Pirojpur);
                } else if (district.equals("Rajbari")) {
                    upazilas = getResources().getStringArray(R.array.Rajbari);
                } else if (district.equals("Rajshahi")) {
                    upazilas = getResources().getStringArray(R.array.Rajshahi);
                } else if (district.equals("Rangpur")) {
                    upazilas = getResources().getStringArray(R.array.Rangpur);
                } else if (district.equals("Rangamati")) {
                    upazilas = getResources().getStringArray(R.array.Rangamati);
                } else if (district.equals("Satkhira")) {
                    upazilas = getResources().getStringArray(R.array.Satkhira);
                } else if (district.equals("Shariatpur")) {
                    upazilas = getResources().getStringArray(R.array.Shariatpur);
                } else if (district.equals("Sherpur")) {
                    upazilas = getResources().getStringArray(R.array.Sherpur);
                } else if (district.equals("Sirajganj")) {
                    upazilas = getResources().getStringArray(R.array.Sirajganj);
                } else if (district.equals("Sunamganj")) {
                    upazilas = getResources().getStringArray(R.array.Sunamganj);
                } else if (district.equals("Sylhet")) {
                    upazilas = getResources().getStringArray(R.array.Sylhet);
                } else if (district.equals("Tangail")) {
                    upazilas = getResources().getStringArray(R.array.Tangail);
                } else if (district.equals("Thakurgaon")) {
                    upazilas = getResources().getStringArray(R.array.Thakurgaon);
                }
                if (upazilas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MyCartActivity.this, android.R.layout.simple_list_item_1, upazilas);
                    editTextFromUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });

        // UpazilaTo Recommendation
        editTextToDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazilas = null;
                if (district.equals("Bagerhat")) {
                    upazilas = getResources().getStringArray(R.array.Bagerhat);
                } else if (district.equals("Bandarban")) {
                    upazilas = getResources().getStringArray(R.array.Bandarban);
                } else if (district.equals("Barguna")) {
                    upazilas = getResources().getStringArray(R.array.Barguna);
                } else if (district.equals("Barisal")) {
                    upazilas = getResources().getStringArray(R.array.Barisal);
                } else if (district.equals("Bhola")) {
                    upazilas = getResources().getStringArray(R.array.Bhola);
                } else if (district.equals("Bogra")) {
                    upazilas = getResources().getStringArray(R.array.Bogra);
                } else if (district.equals("Brahmanbaria")) {
                    upazilas = getResources().getStringArray(R.array.Brahmanbaria);
                } else if (district.equals("Chandpur")) {
                    upazilas = getResources().getStringArray(R.array.Chandpur);
                } else if (district.equals("Chapainawabganj")) {
                    upazilas = getResources().getStringArray(R.array.Chapainawabganj);
                } else if (district.equals("Chittagong")) {
                    upazilas = getResources().getStringArray(R.array.Chittagong);
                } else if (district.equals("Chuadanga")) {
                    upazilas = getResources().getStringArray(R.array.Chuadanga);
                } else if (district.equals("Cox's Bazar")) {
                    upazilas = getResources().getStringArray(R.array.CoxBazar);
                } else if (district.equals("Comilla")) {
                    upazilas = getResources().getStringArray(R.array.Comilla);
                } else if (district.equals("Dhaka")) {
                    upazilas = getResources().getStringArray(R.array.Dhaka);
                } else if (district.equals("Dinajpur")) {
                    upazilas = getResources().getStringArray(R.array.Dinajpur);
                } else if (district.equals("Faridpur")) {
                    upazilas = getResources().getStringArray(R.array.Faridpur);
                } else if (district.equals("Feni")) {
                    upazilas = getResources().getStringArray(R.array.Feni);
                } else if (district.equals("Gaibandha")) {
                    upazilas = getResources().getStringArray(R.array.Gaibandha);
                } else if (district.equals("Gazipur")) {
                    upazilas = getResources().getStringArray(R.array.Gazipur);
                } else if (district.equals("Gopalganj")) {
                    upazilas = getResources().getStringArray(R.array.Gopalganj);
                } else if (district.equals("Habiganj")) {
                    upazilas = getResources().getStringArray(R.array.Habiganj);
                } else if (district.equals("Joypurhat")) {
                    upazilas = getResources().getStringArray(R.array.Joypurhat);
                } else if (district.equals("Jamalpur")) {
                    upazilas = getResources().getStringArray(R.array.Jamalpur);
                } else if (district.equals("Jessore")) {
                    upazilas = getResources().getStringArray(R.array.Jessore);
                } else if (district.equals("Jhalokati")) {
                    upazilas = getResources().getStringArray(R.array.Jhalokati);
                } else if (district.equals("Jhenaidah")) {
                    upazilas = getResources().getStringArray(R.array.Jhenaidah);
                } else if (district.equals("Khagrachari")) {
                    upazilas = getResources().getStringArray(R.array.Khagrachari);
                } else if (district.equals("Khulna")) {
                    upazilas = getResources().getStringArray(R.array.Khulna);
                } else if (district.equals("Kishoreganj")) {
                    upazilas = getResources().getStringArray(R.array.Kishoreganj);
                } else if (district.equals("Kurigram")) {
                    upazilas = getResources().getStringArray(R.array.Kurigram);
                } else if (district.equals("Kushtia")) {
                    upazilas = getResources().getStringArray(R.array.Kushtia);
                } else if (district.equals("Lakshmipur")) {
                    upazilas = getResources().getStringArray(R.array.Lakshmipur);
                } else if (district.equals("Lalmonirhat")) {
                    upazilas = getResources().getStringArray(R.array.Lalmonirhat);
                } else if (district.equals("Madaripur")) {
                    upazilas = getResources().getStringArray(R.array.Madaripur);
                } else if (district.equals("Magura")) {
                    upazilas = getResources().getStringArray(R.array.Magura);
                } else if (district.equals("Manikganj")) {
                    upazilas = getResources().getStringArray(R.array.Manikganj);
                } else if (district.equals("Meherpur")) {
                    upazilas = getResources().getStringArray(R.array.Meherpur);
                } else if (district.equals("Moulvibazar")) {
                    upazilas = getResources().getStringArray(R.array.Moulvibazar);
                } else if (district.equals("Munshiganj")) {
                    upazilas = getResources().getStringArray(R.array.Munshiganj);
                } else if (district.equals("Mymensingh")) {
                    upazilas = getResources().getStringArray(R.array.Mymensingh);
                } else if (district.equals("Naogaon")) {
                    upazilas = getResources().getStringArray(R.array.Naogaon);
                } else if (district.equals("Narail")) {
                    upazilas = getResources().getStringArray(R.array.Narail);
                } else if (district.equals("Narayanganj")) {
                    upazilas = getResources().getStringArray(R.array.Narayanganj);
                } else if (district.equals("Narsingdi")) {
                    upazilas = getResources().getStringArray(R.array.Narsingdi);
                } else if (district.equals("Natore")) {
                    upazilas = getResources().getStringArray(R.array.Natore);
                } else if (district.equals("Netrakona")) {
                    upazilas = getResources().getStringArray(R.array.Netrakona);
                } else if (district.equals("Nilphamari")) {
                    upazilas = getResources().getStringArray(R.array.Nilphamari);
                } else if (district.equals("Noakhali")) {
                    upazilas = getResources().getStringArray(R.array.Noakhali);
                } else if (district.equals("Pabna")) {
                    upazilas = getResources().getStringArray(R.array.Pabna);
                } else if (district.equals("Panchagarh")) {
                    upazilas = getResources().getStringArray(R.array.Panchagarh);
                } else if (district.equals("Patuakhali")) {
                    upazilas = getResources().getStringArray(R.array.Patuakhali);
                } else if (district.equals("Pirojpur")) {
                    upazilas = getResources().getStringArray(R.array.Pirojpur);
                } else if (district.equals("Rajbari")) {
                    upazilas = getResources().getStringArray(R.array.Rajbari);
                } else if (district.equals("Rajshahi")) {
                    upazilas = getResources().getStringArray(R.array.Rajshahi);
                } else if (district.equals("Rangpur")) {
                    upazilas = getResources().getStringArray(R.array.Rangpur);
                } else if (district.equals("Rangamati")) {
                    upazilas = getResources().getStringArray(R.array.Rangamati);
                } else if (district.equals("Satkhira")) {
                    upazilas = getResources().getStringArray(R.array.Satkhira);
                } else if (district.equals("Shariatpur")) {
                    upazilas = getResources().getStringArray(R.array.Shariatpur);
                } else if (district.equals("Sherpur")) {
                    upazilas = getResources().getStringArray(R.array.Sherpur);
                } else if (district.equals("Sirajganj")) {
                    upazilas = getResources().getStringArray(R.array.Sirajganj);
                } else if (district.equals("Sunamganj")) {
                    upazilas = getResources().getStringArray(R.array.Sunamganj);
                } else if (district.equals("Sylhet")) {
                    upazilas = getResources().getStringArray(R.array.Sylhet);
                } else if (district.equals("Tangail")) {
                    upazilas = getResources().getStringArray(R.array.Tangail);
                } else if (district.equals("Thakurgaon")) {
                    upazilas = getResources().getStringArray(R.array.Thakurgaon);
                }
                if (upazilas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MyCartActivity.this, android.R.layout.simple_list_item_1, upazilas);
                    editTextToUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });
    }
}