package com.example.kitbag.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kitbag.R;
import com.example.kitbag.adapter.DistrictUpazillaAdapter;
import com.example.kitbag.adapter.PostAdapter;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.authentication.LoginActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityMainBinding;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // Binding our activity
    private ActivityMainBinding binding;
    private TextView textViewDismissCustomSearch;
    private AutoCompleteTextView editTextFromDistrict, editTextFromUpazila, editTextToDistrict, editTextToUpazila;

    // Exit app on back pressed again
    private long backPressedTime;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    private UserModel userModel;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // Show progressBar
    private ProgressDialog progressDialog;

    // DarkMode Button
    private SwitchCompat switchDarkMode;

    // For Changing Password
    private EditText editTextOldPassword;

    // For Pagination
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private int limit = 8;
    private DocumentSnapshot lastVisible;

    public static final String getOpenFromActivity = "getOpenFromActivity";
    public static final String fromMainActivity = "MainActivity";
    public static final String fromMyPostActivity = "MyPostActivity";
    public static final String fromMyCartActivity = "MyCartActivity";
    public static final String fromChatDetailsActivity = "ChatDetailsActivity";
    public static final String fromOtpVerificationActivity = "OtpVerificationActivity";

    // Check is searching or not
    private boolean searching = false;

    // get data from fireStore and set to the recyclerView
    private PostAdapter postAdapter;

    private ArrayList<ModelClassPost> postList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setting chosen language as local language
        loadLocale();
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initially Check Internet Connection
        if (SharedPreference.getConnectionCheckupValue(this)) {
            SharedPreference.setConnectionCheckupValue(this, false);
            checkInternetConnection();
        }

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
            // Theme
            switchDarkMode = MenuItemCompat.getActionView(binding.navigationView.getMenu().findItem(R.id.nav_dark_mode)).findViewById(R.id.switch_dark_mode);
            if (SharedPreference.getDarkModeEnableValue(this)) {
                switchDarkMode.setChecked(true);
            } else {
                switchDarkMode.setChecked(false);
            }
            // Hide Home button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_home).setVisible(false);
            // Get userName and image from database and set to the drawer
            collectionReference.document(currentUser.getUid()).get()
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
            // Theme
            switchDarkMode = MenuItemCompat.getActionView(binding.navigationView.getMenu().findItem(R.id.nav_dark_mode)).findViewById(R.id.switch_dark_mode);
            if (SharedPreference.getDarkModeEnableValue(this)) {
                switchDarkMode.setChecked(true);
            } else {
                switchDarkMode.setChecked(false);
            }
            // Hide Home button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_home).setVisible(false);
        }

        // Swipe from up to bottom to refresh the recyclerView
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (searching) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                } else {
                    binding.swipeRefreshLayout.setRefreshing(true);
                    if (isConnected()) {
                        restartApp();
                    } else {
                        displayNoConnection();
                    }
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // Click the appBar logo to refresh the layout
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                if (isConnected()) {
                    restartApp();
                } else {
                    displayNoConnection();
                }
                progressDialog.dismiss();
            }
        });

        // Toggle dark mode button in drawer menu
        switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDarkMode.isChecked()) {
                    SharedPreference.setDarkModeEnableValue(MainActivity.this, true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    SharedPreference.setDarkModeEnableValue(MainActivity.this, false);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        // Get data from fireStore and set to the recyclerView
        displayAllPost();

        // Open post Activity
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                intent.putExtra("whatToDo", "CreatePost");
                startActivity(intent);
            }
        });

        // Open notifications Activity
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
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
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(MainActivity.this,FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        binding.drawerLayout.closeDrawer(GravityCompat.END);
                        break;
                    case R.id.nav_login:
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                    case R.id.nav_deliveryman:
                        registerAsDeliveryman();
                        break;
                    case R.id.nav_language:
                        showChangeLanguageDialog();
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
                        startActivity(new Intent(MainActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(MainActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(MainActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(MainActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        // smoothly reload activity
                        finish();
                        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                        startActivity(getIntent());
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });

        // On search Icon click from app bar
        binding.customAppBar.appbarImageviewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate custom layout
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_custom_search, null);
                // Getting view form custom dialog layout
                editTextFromDistrict = view.findViewById(R.id.EditTextFromDistrict);
                editTextFromUpazila = view.findViewById(R.id.EditTextFromUpazila);
                editTextToDistrict = view.findViewById(R.id.EditTextToDistrict);
                editTextToUpazila = view.findViewById(R.id.EditTextToUpazila);
                textViewDismissCustomSearch = view.findViewById(R.id.textViewDismissCustomSearch);
                Button buttonSearch = view.findViewById(R.id.buttonSearch);
                //setAdapter on District and Upazila
                setDistrictUpazilaOnEditText();
                // Dialog Builder
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();

                //textView dismissCustomSearch dialog
                textViewDismissCustomSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // On click the search button
                buttonSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Getting value from user edit text
                        String fromDistrict = editTextFromDistrict.getText().toString().trim();
                        String fromUpazila = editTextFromUpazila.getText().toString().trim();
                        String toDistrict = editTextToDistrict.getText().toString().trim();
                        String toUpazila = editTextToUpazila.getText().toString().trim();
                        // Check validation
                        if (TextUtils.isEmpty(fromDistrict)) {
                            editTextFromDistrict.setError("Required");
                            editTextFromDistrict.requestFocus();
                            return;
                        }
                        if (TextUtils.isEmpty(fromUpazila)) {
                            editTextFromUpazila.setError("Required");
                            editTextFromUpazila.requestFocus();
                            return;
                        }
                        if (TextUtils.isEmpty(toDistrict)) {
                            editTextToDistrict.setError("Required");
                            editTextToDistrict.requestFocus();
                            return;
                        }
                        if (TextUtils.isEmpty(toUpazila)) {
                            editTextToUpazila.setError("Required");
                            editTextToUpazila.requestFocus();
                            return;
                        }
                        dialog.dismiss();
                        postList.clear();
                        postAdapter.notifyDataSetChanged();
                        filterPostInAdapter(fromDistrict, fromUpazila, toDistrict, toUpazila);
                    }
                });
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
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                binding.fab.show();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

    } // Ending onCreate

    // Init Check Internet Connection
    private void checkInternetConnection() {
        if (isConnected()) {
            displayConnected();
        } else {
            displayNoConnection();
        }
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

    private void displayConnected() {
        View parentLayout = findViewById(R.id.snackBarContainer);
        // create an instance of the snackBar
        final Snackbar snackbar = Snackbar.make(parentLayout, "", Snackbar.LENGTH_LONG);
        // inflate the custom_snackBar_view created previously
        View customSnackView = getLayoutInflater().inflate(R.layout.snackbar_connected, null);
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

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void restartApp() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    // Get data from fireStore and set to the recyclerView
    private void displayAllPost() {
        postAdapter = new PostAdapter(MainActivity.this, postList);
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2, GridLayoutManager.VERTICAL, false);
        binding.recyclerViewPostLists.setLayoutManager(gridLayoutManager);
        binding.recyclerViewPostLists.setAdapter(postAdapter);
        // Show progressBar
        showProgressDialog();
        // get data from fireStore and set to the recyclerView
        db.collection("All_Post")
                .orderBy("timeAdded", Query.Direction.DESCENDING)
                .limit(limit)
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
                                    Intent intent = new Intent(MainActivity.this, PostInfoActivity.class);
                                    intent.putExtra("userId", post.getUserId());
                                    intent.putExtra("postReference", post.getPostReference());
                                    intent.putExtra("statusCurrent", post.getStatusCurrent());
                                    intent.putExtra(getOpenFromActivity, fromMainActivity);
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
                                        Query nextQuery = db.collection("All_Post")
                                                .orderBy("timeAdded", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
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

                                                    if (t.getResult().size() < limit) {
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
    }

    // showing language alert Dialog to pick one language
    private void showChangeLanguageDialog() {
        // inflate custom layout
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_select_language, null);
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
                    showProgressDialog();
                    dialog.dismiss();
                    setLocale("bn");
                    restartApp();
                    progressDialog.dismiss();
                } else {
                    showProgressDialog();
                    dialog.dismiss();
                    setLocale("en");
                    restartApp();
                    progressDialog.dismiss();
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
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        // Save data to SharedPreference
        SharedPreference.setLanguageValue(this, lang);
    }

    // get save value from sharedPreference and set It to as local language
    public void loadLocale(){
        setLocale(SharedPreference.getLanguageValue(this));
    }

    // Registration as deliveryman
    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                if (isConnected()) {
                    startActivity(new Intent(MainActivity.this, DeliverymanRegistrationActivity.class));
                } else {
                    displayNoConnection();
                }
            }
        });
    }

    // Show progress Dialog
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    // Get Searched data from fireStore and set to the recyclerView
    private void filterPostInAdapter(String fromDistrict, String fromUpazila, String toDistrict, String toUpazila) {
        binding.textViewSearchResult.setVisibility(View.VISIBLE);
        binding.textViewSearchResult.setText("Filter by: from " + fromUpazila + ", " + fromDistrict + " to " + toUpazila + ", " + toDistrict);
        searching = true;
        binding.customAppBar.appbarTitle.setText("Search");
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.fab.setVisibility(View.GONE);
        // Show progressBar
        showProgressDialog();
        db.collection("All_Post")
                .whereEqualTo("fromDistrict", fromDistrict)
                .whereEqualTo("fromUpazilla", fromUpazila)
                .whereEqualTo("toDistrict", toDistrict)
                .whereEqualTo("toUpazilla", toUpazila)
                .limit(limit)
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
                                    Intent intent = new Intent(MainActivity.this, PostInfoActivity.class);
                                    intent.putExtra("userId", post.getUserId());
                                    intent.putExtra("postReference", post.getPostReference());
                                    intent.putExtra("statusCurrent", post.getStatusCurrent());
                                    intent.putExtra(getOpenFromActivity, fromMainActivity);
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
                                        Query nextQuery = db.collection("All_Post")
                                                .orderBy("timeAdded", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
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

                                                    if (t.getResult().size() < limit) {
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
        // Later show data matched with district also
        db.collection("All_Post")
                .whereEqualTo("fromDistrict", fromDistrict)
                .whereEqualTo("toDistrict", toDistrict)
                .limit(limit)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                ModelClassPost modelClassPost = document.toObject(ModelClassPost.class);
                                // Don't repeat post
                                if (!modelClassPost.getFromUpazilla().equals(fromUpazila) && !modelClassPost.getToUpazilla().equals(toUpazila)) {
                                    postList.add(modelClassPost);
                                }
                            }
                            if (postList.isEmpty()) {
                                binding.textViewNotFoundMessage.setVisibility(View.VISIBLE);
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
                                    Intent intent = new Intent(MainActivity.this, PostInfoActivity.class);
                                    intent.putExtra("userId", post.getUserId());
                                    intent.putExtra("postReference", post.getPostReference());
                                    intent.putExtra("statusCurrent", post.getStatusCurrent());
                                    intent.putExtra(getOpenFromActivity, fromMainActivity);
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
                                        Query nextQuery = db.collection("All_Post")
                                                .orderBy("timeAdded", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
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

                                                    if (t.getResult().size() < limit) {
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
                        progressDialog.dismiss();
                    }
                });
    }

    // validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_change_password, null);
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
                        // Update the password in RealTime Database for ForgotPassword
                        FirebaseDatabase.getInstance().getReference().child("Passwords")
                                .child(userModel.getPhoneNumber().substring(1, 14)).setValue(newPassword);
                        dialog.dismiss();
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // Exit app on back pressed
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else if (searching) {
            showProgressDialog();
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            progressDialog.dismiss();
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                moveTaskToBack(true);
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    // District and Upazila Recommendation
    private void setDistrictUpazilaOnEditText() {
        // District Recommendation
        String[] districts = getResources().getStringArray(R.array.Districts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, districts);
        editTextFromDistrict.setAdapter(adapter);  // District
        editTextToDistrict.setAdapter(adapter);    // District

        // UpazilaFrom Recommendation
        editTextFromDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazillas = DistrictUpazillaAdapter.getUpazillas(MainActivity.this, district);
                if (upazillas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, upazillas);
                    editTextFromUpazila.setAdapter(adapterUpazila);  // Define Upazillas
                }
            }
        });

        // UpazillaTo Recommendation
        editTextToDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazillas = DistrictUpazillaAdapter.getUpazillas(MainActivity.this, district);
                if (upazillas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, upazillas);
                    editTextToUpazila.setAdapter(adapterUpazila);  // Define Upazillas
                }
            }
        });
    }
}