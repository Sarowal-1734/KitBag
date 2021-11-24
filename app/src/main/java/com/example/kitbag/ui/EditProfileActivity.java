package com.example.kitbag.ui;

import static android.Manifest.permission.CALL_PHONE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityEditProfileBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.model.UserModel;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    // Show progressBar
    private ProgressDialog progressDialog;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private UserModel currentUserModel;
    private UserModel userModelInfo;

    // For Changing Password
    private EditText editTextOldPassword;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    // Get image from gallery and set to the imageView
    private static final int PICK_IMAGE = 1;
    private Uri imageUri = null;

    // For Image Picker
    private ActivityResultLauncher<String> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        loadLocale();
        setContentView(binding.getRoot());

        //setAdapter on District and Upazila
        setDistrictUpazilaOnEditText();

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Edit Profile");

        // Dynamic Activity View (EditProfile/ViewProfile)
        if (!currentUser.getUid().equals(getIntent().getStringExtra("userId"))) {
            binding.customAppBar.appbarTitle.setText("User Info");
            binding.buttonUpdateProfile.setVisibility(View.GONE);
            binding.textViewBecomeDeliveryman.setVisibility(View.GONE);
            binding.customEditProfileImage.cardViewAddProfilePic.setVisibility(View.GONE);
            binding.autoCompleteUserName.setEnabled(false);
            binding.autoCompleteEmail.setEnabled(false);
            binding.autoCompleteDistrict.setEnabled(false);
            binding.autoCompleteUpazilla.setEnabled(false);
            binding.imageViewCall.setVisibility(View.VISIBLE);
            binding.imageViewCall.setColorFilter(Color.parseColor("#43AA0C"));
            binding.imageViewMail.setVisibility(View.VISIBLE);
            binding.imageViewMail.setColorFilter(Color.parseColor("#DC6363"));
        }
        // on Call icon clicked
        binding.imageViewCall.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                makeCall();
            }
        });
        // on Mail icon clicked
        binding.imageViewMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(currentUserModel.getEmail())) {
                    String subject = "Providing Delivery Service In KitBag";
                    String body = "Hi! I am a deliveryman. I found that you are going to deliver a product. Here as deliveryman," +
                            " I can deliver your product. If you are interested then please check the inbox in KitBag Chat option.";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + userModelInfo.getEmail()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(emailIntent, "KitBag Courier Service"));
                } else {
                    Toast.makeText(EditProfileActivity.this, "Please add an email to your profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set drawer menu based on Login/Logout
        if (currentUser != null) {
            // User is signed in
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
            binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
            // Hide DarkMode button in drawer in MainActivity
            binding.navigationView.getMenu().findItem(R.id.nav_dark_mode).setVisible(false);
            // Get userName and image from database and set to the drawer
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            currentUserModel = documentSnapshot.toObject(UserModel.class);
                            // Hide or visible the deliveryman text
                            if (currentUserModel.getUserType().equals("Deliveryman") || currentUserModel.getUserType().equals("Agent")) {
                                binding.textViewBecomeDeliveryman.setVisibility(View.GONE);
                            }
                            if (currentUserModel.getUserType().equals("Deliveryman") || currentUserModel.getUserType().equals("Agent")) {
                                binding.navigationView.getMenu().findItem(R.id.nav_deliveryman).setVisible(false);
                            }
                            View view = binding.navigationView.getHeaderView(0);
                            TextView userName = (TextView) view.findViewById(R.id.nav_user_name);
                            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.nav_user_photo);
                            userName.setText(currentUserModel.getUserName());
                            if (currentUserModel.getImageUrl() != null) {
                                // Picasso library for download & show image in drawer and appBar
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        }

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // On Edit profile icon clicked
        View view = binding.navigationView.getHeaderView(0);
        ImageView imageView = view.findViewById(R.id.nav_edit_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(EditProfileActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                        finish();
                        break;
                    case R.id.nav_language:
                        showChangeLanguageDialog();
                        break;
                    case R.id.nav_deliveryman:
                        registerAsDeliveryman();
                        break;
                    case R.id.nav_discover_kitbag:
                        intentFragment.putExtra("whatToDo","discoverKitBag");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_terms_conditions:
                        intentFragment.putExtra("whatToDo","termsAndCondition");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_contact:
                        intentFragment.putExtra("whatToDo","contactUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_about:
                        intentFragment.putExtra("whatToDo","aboutUs");
                        startActivity(intentFragment);
                        break;
                    case R.id.nav_chat:
                        startActivity(new Intent(EditProfileActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(EditProfileActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(EditProfileActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(EditProfileActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });

        // User will be always signed in here
        binding.navigationView.getMenu().clear();
        binding.navigationView.inflateMenu(R.menu.drawer_menu_login);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_user_name).setVisibility(View.VISIBLE);
        binding.navigationView.getHeaderView(0).findViewById(R.id.nav_edit_profile).setVisibility(View.VISIBLE);
        // Get userName and image from database and set to the drawer and hide or visible the deliveryman text
        collectionReference.document(getIntent().getStringExtra("userId")).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userModelInfo = documentSnapshot.toObject(UserModel.class);
                        if (!currentUser.getUid().equals(getIntent().getStringExtra("userId")) && TextUtils.isEmpty(userModelInfo.getEmail())) {
                            binding.autoCompleteEmail.setVisibility(View.GONE);
                            binding.imageViewMail.setVisibility(View.GONE);
                        }
                        // get user info from database and set to fields
                        binding.editTextUsername.setText(userModelInfo.getUserName());
                        binding.editTextUsertype.setText(userModelInfo.getUserType());
                        binding.editTextContact.setText(userModelInfo.getPhoneNumber());
                        if (documentSnapshot.getString("email") != null) {
                            binding.editTextEmail.setText(userModelInfo.getEmail());
                        }
                        if (documentSnapshot.getString("district") != null) {
                            binding.EditTextDistrict.setText(userModelInfo.getDistrict());
                        }
                        if (documentSnapshot.getString("upazilla") != null) {
                            binding.EditTextUpazila.setText(userModelInfo.getUpazilla());
                        }
                        if (documentSnapshot.getString("imageUrl") != null) {
                            // set image to the imageView (activity)
                            Picasso.get().load(userModelInfo.getImageUrl()).placeholder(R.drawable.logo).fit().centerCrop().into(binding.customEditProfileImage.circularImageViewProfile);
                        }
                    }
                });

        // For Image Picker
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        imageUri = uri;
                        binding.customEditProfileImage.circularImageViewProfile.setImageURI(imageUri);
                    }
                });

        // Get image from gallery and set to the imageView
        binding.customEditProfileImage.cardViewAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityResultLauncher.launch("image/*");
            }
        });

        // On Become Deliveryman text clicked
        binding.textViewBecomeDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerAsDeliveryman();
            }
        });

        // On Update Profile Button Clicked
        binding.buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    if (validation()) {
                        showProgressBar();
                        if (imageUri == null) {
                            // Update user info in Database
                            updateUserInfo(userModelInfo.getImageUrl());
                        } else {
                            // Store image to firebase
                            StorageReference filepath = storageReference.child("users_profile_picture").child(currentUser.getUid());
                            filepath.putFile(imageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // Update user info in Database
                                                    updateUserInfo(uri.toString());
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                    }
                } else {
                    // Show that no connection
                    showMessageNoConnection();
                }
            }
        });
    }//ending onCreate

    // showing language alert Dialog to pick one language
    private void showChangeLanguageDialog() {
        final String[] multiLanguage = {"বাংলা","English"};
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Language..");
        builder.setSingleChoiceItems(multiLanguage, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    setLocale("bn");
                    recreate();
                }else {
                    setLocale("en");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    // setting chosen language to system
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        // Save data to SharedPreference
        SharedPreference.setLanguageValue(this, lang);
    }

    // get save value from sharedPreference and set It to as local language
    public void loadLocale(){
        setLocale(SharedPreference.getLanguageValue(this));
    }

    private void updateUserInfo(String imageUrl) {
        // Setting user value to model class
        UserModel userModelUpdate = new UserModel();
        userModelUpdate.setEmail(binding.editTextEmail.getText().toString());
        userModelUpdate.setUserName(binding.editTextUsername.getText().toString());
        userModelUpdate.setUpazilla(binding.EditTextUpazila.getText().toString());
        userModelUpdate.setDistrict(binding.EditTextDistrict.getText().toString());
        db.collection("Users").document(currentUser.getUid())
                .update(
                        "imageUrl", imageUrl,
                        "userName", userModelUpdate.getUserName(),
                        "email", userModelUpdate.getEmail(),
                        "district", userModelUpdate.getDistrict(),
                        "upazilla", userModelUpdate.getUpazilla()
                );
        progressDialog.dismiss();
        Toast.makeText(EditProfileActivity.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + userModelInfo.getPhoneNumber()));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            requestPermissions(new String[]{CALL_PHONE}, 1);
        }

    }

    // Registration as deliveryman
    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                startActivity(new Intent(EditProfileActivity.this, DeliverymanRegistrationActivity.class));
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

    // Validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.dialog_change_password,null);
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
                showProgressBar();
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
                        // Update the password in RealTime Database for ForgotPassword
                        FirebaseDatabase.getInstance().getReference().child("Passwords")
                                .child(currentUserModel.getPhoneNumber().substring(1, 14)).setValue(newPassword);
                        dialog.dismiss();
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // EditText Validation
    private boolean validation() {
        if (TextUtils.isEmpty(binding.editTextUsername.getText().toString())) {
            binding.editTextUsername.setError("Required");
            binding.editTextUsername.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextDistrict.getText().toString())) {
            binding.EditTextDistrict.setError("Required");
            binding.EditTextDistrict.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextUpazila.getText().toString())) {
            binding.EditTextUpazila.setError("Required");
            binding.EditTextUpazila.requestFocus();
            return false;
        }
        return true;
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
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

    // District and Upazila Recommendation
    private void setDistrictUpazilaOnEditText() {
        // District Recommendation
        String[] districts = getResources().getStringArray(R.array.Districts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_list_item_1, districts);
        binding.EditTextDistrict.setAdapter(adapter);

        // UpazilaFrom Recommendation
        binding.EditTextDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_list_item_1, upazilas);
                    binding.EditTextUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });
    }
}