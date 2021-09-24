package com.example.kitbag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.kitbag.adapter.PostAdapter;
import com.example.kitbag.databinding.ActivityMyPostBinding;
import com.example.kitbag.model.ModelClassPost;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {

    private ActivityMyPostBinding binding;
    private AutoCompleteTextView editTextFromDistrict, editTextFromUpazila, editTextToDistrict, editTextToUpazila;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("My Posts");

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
                    Toast.makeText(MyPostActivity.this, "Dark Mode Enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyPostActivity.this, "Dark Mode Disabled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // get data from fireStore and set to the recyclerView
        ArrayList<ModelClassPost> postList = new ArrayList<>();
        db.collection("All_Post")
                .whereEqualTo("userId", currentUser.getUid())  //to get postList of current user
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ModelClassPost modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                            postList.add(modelClassPost);
                        }
                        PostAdapter postAdapter = new PostAdapter(MyPostActivity.this, postList);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(MyPostActivity.this, 2, GridLayoutManager.VERTICAL, false);
                        binding.recyclerViewPostLists.setLayoutManager(gridLayoutManager);
                        binding.recyclerViewPostLists.setAdapter(postAdapter);
                        // On recycler item click listener
                        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(ModelClassPost post) {
                                Intent intent = new Intent(MyPostActivity.this, PostInfoActivity.class);
                                intent.putExtra("title", post.getTitle());
                                intent.putExtra("postedBy", post.getUserName());
                                intent.putExtra("imageUrl", post.getImageUrl());
                                intent.putExtra("timeAdded", post.getTimeAdded());
                                intent.putExtra("description", post.getDescription());
                                intent.putExtra("weight", post.getWeight());
                                intent.putExtra("status", "N/A");
                                intent.putExtra("fromUpazilla", post.getFromUpazilla());
                                intent.putExtra("fromDistrict", post.getFromDistrict());
                                intent.putExtra("toUpazilla", post.getToUpazilla());
                                intent.putExtra("toDistrict", post.getToDistrict());
                                intent.putExtra("userType", post.getUserType());
                                intent.putExtra("userPhone", post.getPhoneNumber());
                                intent.putExtra("userEmail", post.getEmail());
                                intent.putExtra("userId", post.getUserId());
                                intent.putExtra("postRef", post.getPostReference());
                                startActivity(intent);
                            }
                        });
                    }
                });

        // Open post Activity
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPostActivity.this, PostActivity.class);
                intent.putExtra("whatToDo", "CreatePost");
                startActivity(intent);
            }
        });

        // Open notifications Activity
        findViewById(R.id.appbar_notification_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyPostActivity.this, NotificationsActivity.class));
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
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyPostActivity.this, EditProfileActivity.class));
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(MyPostActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MyPostActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });

        // On search button click
        binding.customAppBar.appbarImageviewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Inflate Custom layout for searching
                LayoutInflater inflater = MyPostActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_search_dialog, null);

                // Create Dialog Builder
                AlertDialog.Builder ab = new AlertDialog.Builder(MyPostActivity.this);

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

        // Show or Hide Floating Action Button
        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                binding.fab.hide();
                slidrInterface.lock();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                binding.fab.show();
                slidrInterface.unlock();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

    }

    // Exit app on back pressed
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MyPostActivity.this, android.R.layout.simple_list_item_1, districts);
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
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MyPostActivity.this, android.R.layout.simple_list_item_1, upazilas);
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
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MyPostActivity.this, android.R.layout.simple_list_item_1, upazilas);
                    editTextToUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });
    }
}