package com.example.kitbag.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import com.example.kitbag.R;
import com.example.kitbag.adapter.DistrictUpazillaAdapter;
import com.example.kitbag.authentication.DeliverymanRegistrationActivity;
import com.example.kitbag.chat.MessageActivity;
import com.example.kitbag.databinding.ActivityPostBinding;
import com.example.kitbag.fragment.container.FragmentContainerActivity;
import com.example.kitbag.model.ModelClassPost;
import com.example.kitbag.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private ModelClassPost modelClassPost;

    private String receiverPhoneNumber, postOwnersPhoneNumber;

    private ActivityPostBinding binding;

    // Show progressBar
    private ProgressDialog progressDialog;

    // Dialog Declaration
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    // For Changing Password
    private EditText editTextOldPassword;

    // Get image from gallery and set to the imageView
    private static final int PICK_IMAGE = 100;
    private Uri imageUri = null;

    // For camera
    private ActivityResultLauncher<Intent> activityResultLauncher;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        // setting chosen language as system language
        setContentView(binding.getRoot());

        // Initially Check Internet Connection
        if (!isConnected()) {
            showMessageNoConnection();
        }

        // remove search icon icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Attach full number from edittext with cpp
        binding.cppReceiverPhoneNumber.registerCarrierNumberEditText(binding.EditTextReceiverPhoneNumber);
        binding.cppPreferredDeliveryman.registerCarrierNumberEditText(binding.EditTextPreferredDeliveryman);

        // Invisible the preferred deliveryman editText if get opened from create post
        if (getIntent().getStringExtra("whatToDo").equals("CreatePost")) {
            binding.layoutPreferredDeliveryman.setVisibility(View.GONE);
        }

        // Disable the views if currentPostStatus is Primary_Agent
        if (getIntent().getStringExtra("statusCurrent") != null) {
            disableViewsBasedOnStatusCurrent(getIntent().getStringExtra("statusCurrent"));
        }

        // Change the title of the appBar according to Edit or Create post
        if (getIntent().getStringExtra("whatToDo").equals("EditPost")) {
            binding.customAppBar.appbarTitle.setText("Edit Post");
            binding.buttonPostItem.setText("Update Post");
            db.collection("All_Post")
                    .whereEqualTo("postReference", getIntent().getStringExtra("postReference"))
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        modelClassPost = documentSnapshot.toObject(ModelClassPost.class);
                        binding.EditTextPostTitle.setText(modelClassPost.getTitle());
                        binding.EditTextPostWeight.setText(modelClassPost.getWeight());
                        binding.EditTextPostDescription.setText(modelClassPost.getDescription());
                        binding.EditTextFromDistrict.setText(modelClassPost.getFromDistrict());
                        binding.EditTextFromUpazila.setText(modelClassPost.getFromUpazilla());
                        binding.EditTextToDistrict.setText(modelClassPost.getToDistrict());
                        binding.EditTextToUpazila.setText(modelClassPost.getToUpazilla());
                        postOwnersPhoneNumber = modelClassPost.getPhoneNumber();
                        String receiverPhone = modelClassPost.getReceiverPhoneNumber().substring(4);
                        binding.EditTextReceiverPhoneNumber.setText(receiverPhone);
                        String deliverymanPhone = modelClassPost.getPreferredDeliverymanContact();
                        if (deliverymanPhone != null) {
                            deliverymanPhone = modelClassPost.getPreferredDeliverymanContact().substring(4);
                            binding.EditTextPreferredDeliveryman.setText(deliverymanPhone);
                        }
                        Picasso.get().load(modelClassPost.getImageUrl()).placeholder(R.drawable.logo).fit().into(binding.imageViewAddPhoto);
                    }
                }
            });
        } else {
            binding.customAppBar.appbarTitle.setText("Create Post");
        }

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
                            // set userName to the drawer
                            userName.setText(userModel.getUserName());
                            if (userModel.getImageUrl() != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(userModel.getImageUrl()).placeholder(R.drawable.logo).fit().centerCrop().into(imageView);
                                Picasso.get().load(userModel.getImageUrl()).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
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
        }

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //setAdapter on District and Upazila
        setDistrictUpazilaOnEditText();

        // Open Drawer Layout
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
                Intent intent = new Intent(PostActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                startActivity(intent);
            }
        });

        // On drawer menu item clicked
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intentFragment = new Intent(PostActivity.this, FragmentContainerActivity.class);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
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
                        startActivity(new Intent(PostActivity.this, MessageActivity.class));
                        break;
                    case R.id.nav_my_post:
                        startActivity(new Intent(PostActivity.this, MyPostActivity.class));
                        break;
                    case R.id.nav_my_cart:
                        startActivity(new Intent(PostActivity.this, MyCartActivity.class));
                        break;
                    case R.id.nav_change_password:
                        validationUpdatePassword();
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
                        Toast.makeText(PostActivity.this, "Logout Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });
        // Get image from camera and set to the imageView
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "val", null);
                            setImageInImageView(path); // Reason: imageUri becomes null
                        }
                    }
                });

        // Set on button click lister on imageView of post
        binding.imageViewAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request for camera and storage permissions
                if (checkPermissions()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activityResultLauncher.launch(intent);
                } else {
                    requestPermission();
                }
            }
        });

        // On Post Button Clicked
        binding.buttonPostItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    if (currentUser != null) {
                        if (valid()) {
                            receiverPhoneNumber = binding.cppReceiverPhoneNumber.getFullNumberWithPlus().trim();
                            if (getIntent().getStringExtra("whatToDo").equals("EditPost")) {
                                // Edit The Post
                                verifyReceiverPhone(binding.cppReceiverPhoneNumber.getFullNumber().trim());
                            } else {
                                // Post The Item
                                if (imageUri != null) {
                                    verifyRecieverPhoneNumber(binding.cppReceiverPhoneNumber.getFullNumber().trim());
                                } else {
                                    Toast.makeText(PostActivity.this, "Please add a photo", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(PostActivity.this, "Please login first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show that no connection
                    showMessageNoConnection();
                }
            }
        });

    } // Ending onCreate



    private void disableViewsBasedOnStatusCurrent(String statusCurrent) {
        if (statusCurrent.equals("Primary_Agent")) {
            binding.imageViewAddPhoto.setEnabled(false);
            binding.EditTextPostTitle.setEnabled(false);
            binding.EditTextPostWeight.setEnabled(false);
            binding.EditTextPostDescription.setEnabled(false);
            binding.EditTextFromDistrict.setEnabled(false);
            binding.EditTextFromUpazila.setEnabled(false);
            binding.EditTextToDistrict.setEnabled(false);
            binding.EditTextToUpazila.setEnabled(false);
        }
    }

    // Verify Receiver Phone Number is a User or Not and Continue to create post
    private void verifyRecieverPhoneNumber(String phone) {
        showProgressBar();
        String fakeEmail = phone + "@gmail.com";
        mAuth.fetchSignInMethodsForEmail(fakeEmail)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                binding.EditTextReceiverPhoneNumber.setError("User Not Found");
                                binding.EditTextReceiverPhoneNumber.requestFocus();
                                progressDialog.dismiss();
                            } else {
                                continuePostItem();
                            }
                        }
                    }
                });
    }

    // Post a new item
    private void continuePostItem() {
        // Get user input data
        String title = binding.EditTextPostTitle.getText().toString().trim();
        String weight = binding.EditTextPostWeight.getText().toString().trim();
        String description = binding.EditTextPostDescription.getText().toString().trim();
        String fromDistrict = binding.EditTextFromDistrict.getText().toString().trim();
        String fromUpazilla = binding.EditTextFromUpazila.getText().toString().trim();
        String toDistrict = binding.EditTextToDistrict.getText().toString().trim();
        String toUpazilla = binding.EditTextToUpazila.getText().toString().trim();
        // Set data to the Model Class
        ModelClassPost post = new ModelClassPost();
        post.setTitle(title);
        post.setWeight(weight);
        post.setDescription(description);
        post.setFromDistrict(fromDistrict);
        post.setFromUpazilla(fromUpazilla);
        post.setToDistrict(toDistrict);
        post.setToUpazilla(toUpazilla);
        post.setReceiverPhoneNumber(receiverPhoneNumber);
        post.setTimeAdded(new Timestamp(new Date()));
        post.setUserId(currentUser.getUid());
        post.setStatusCurrent("N/A");
        post.setPostReference(null);
        // Get userPhoneNumber from database for storing with the post
        collectionReference.document(currentUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel model = documentSnapshot.toObject(UserModel.class);
                        post.setPhoneNumber(model.getPhoneNumber());
                    }
                });
        // Store image to Firebase Storage
        StorageReference filepath = storageReference.child("all_post_images").child(currentUser.getUid() + new Timestamp(new Date()));
        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                post.setImageUrl(uri.toString());
                                // Storing the PostInfo into All_Post Collection
                                db.collection("All_Post").add(post)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Storing the document reference for easy to delete later
                                                db.collection("All_Post").document(documentReference.getId())
                                                        .update("postReference", documentReference.getId());
                                                // Hide progressBar
                                                progressDialog.dismiss();
                                                Toast.makeText(PostActivity.this, "Your post is now visible to everyone", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(PostActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setImageInImageView(String path) {
        imageUri = Uri.parse(path);
        binding.imageViewAddPhoto.setImageURI(imageUri);
    }

    // Deliveryman Registration Dialog
    private void registerAsDeliveryman() {
        // inflate custom layout
        View view = LayoutInflater.from(PostActivity.this).inflate(R.layout.dialog_deliveryman_requirements, null);
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
                startActivity(new Intent(PostActivity.this, DeliverymanRegistrationActivity.class));
            }
        });
    }

    // Method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request camera and storage permission
    private void requestPermission() {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            },PICK_IMAGE);
        }

    // Validation for update password and create popup dialog
    private void validationUpdatePassword() {
        // inflate custom layout
        View view = LayoutInflater.from(PostActivity.this).inflate(R.layout.dialog_change_password, null);
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
                progressDialog = new ProgressDialog(PostActivity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                progressDialog.setCancelable(false);
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    // Update user password
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
                        Toast.makeText(PostActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
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

    // Here only the ReceiverPhoneNumber will verify and continue to update post
    private void verifyReceiverPhone(String phone) {
        showProgressBar();
        // Verify Receiver Phone Number is a User or Not
        String fakeEmail = phone + "@gmail.com";
        mAuth.fetchSignInMethodsForEmail(fakeEmail)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if (isNewUser) {
                                binding.EditTextReceiverPhoneNumber.setError("User Not Found");
                                binding.EditTextReceiverPhoneNumber.requestFocus();
                                progressDialog.dismiss();
                            } else {
                                verifyDeliverymanPhone();
                            }
                        }
                    }
                });
    }

    // Here only the DeliverymanPhoneNumber will verify and continue to update post
    private void verifyDeliverymanPhone() {
        String preferredDeliverymanContact = binding.cppPreferredDeliveryman.getFullNumberWithPlus().trim();
        if (binding.EditTextPreferredDeliveryman.getText().toString().isEmpty()) {
            continueUpdatePost(null);
        } else {
            // Checking the given number is a Deliveryman or not
            db.collection("Users")
                    .whereEqualTo("phoneNumber", preferredDeliverymanContact)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().getDocuments().isEmpty()) {
                                    DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                                    if (snapshot.getString("userType").equals("Deliveryman") || snapshot.getString("userType").equals("Agent")) {
                                        // Deliveryman
                                        continueUpdatePost(snapshot.getString("phoneNumber"));
                                    } else {
                                        // Not an Deliveryman
                                        binding.EditTextPreferredDeliveryman.setError("Deliveryman Not Found");
                                        binding.EditTextPreferredDeliveryman.requestFocus();
                                        progressDialog.dismiss();
                                    }
                                } else {
                                    // Not a User
                                    binding.EditTextPreferredDeliveryman.setError("Deliveryman Not Found");
                                    binding.EditTextPreferredDeliveryman.requestFocus();
                                    progressDialog.dismiss();
                                }
                            }
                        }
                    });
        }
    }

    // New image Captured or not and continue to update post
    private void continueUpdatePost(String preferredDeliverymanContact) {
        String imageUrl;
        if (imageUri != null) {
            // Delete old image and store update image with with this post
            FirebaseStorage.getInstance().getReferenceFromUrl(modelClassPost.getImageUrl()).delete();
            storeImageUpdatePost(preferredDeliverymanContact);
        } else {
            imageUrl = modelClassPost.getImageUrl();
            updatePostInfo(imageUrl, preferredDeliverymanContact);
        }
    }

    // Store the new captured image and update post
    private void storeImageUpdatePost(String preferredDeliverymanContact) {
        StorageReference filepath = storageReference.child("all_post_images").child(currentUser.getUid() + new Timestamp(new Date()));
        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                updatePostInfo(uri.toString(), preferredDeliverymanContact);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Finally Update the post
    private void updatePostInfo(String imageUrl, String preferredDeliverymanContact) {
        // Update post info in Database
        db.collection("All_Post").document(getIntent().getStringExtra("postReference"))
                .update(
                        "imageUrl", imageUrl,
                        "title", binding.EditTextPostTitle.getText().toString(),
                        "weight", binding.EditTextPostWeight.getText().toString(),
                        "description", binding.EditTextPostDescription.getText().toString(),
                        "fromDistrict", binding.EditTextFromDistrict.getText().toString(),
                        "fromUpazilla", binding.EditTextFromUpazila.getText().toString(),
                        "toDistrict", binding.EditTextToDistrict.getText().toString(),
                        "toUpazilla", binding.EditTextToUpazila.getText().toString(),
                        "receiverPhoneNumber", receiverPhoneNumber,
                        "preferredDeliverymanContact", preferredDeliverymanContact
                );
        progressDialog.dismiss();
        Toast.makeText(PostActivity.this, "Post successfully updated", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(PostActivity.this, MainActivity.class));
        finish();
    }

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // EditText Validation
    private boolean valid() {
        if (postOwnersPhoneNumber == null) {
            postOwnersPhoneNumber = userModel.getPhoneNumber();
        }
        if (TextUtils.isEmpty(binding.EditTextPostTitle.getText().toString())) {
            binding.EditTextPostTitle.setError("Required");
            binding.EditTextPostTitle.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextPostWeight.getText().toString())) {
            binding.EditTextPostWeight.setError("Required");
            binding.EditTextPostWeight.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextPostDescription.getText().toString())) {
            binding.EditTextPostDescription.setError("Required");
            binding.EditTextPostDescription.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextFromDistrict.getText().toString())) {
            binding.EditTextFromDistrict.setError("Required");
            binding.EditTextFromDistrict.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextFromUpazila.getText().toString())) {
            binding.EditTextFromUpazila.setError("Required");
            binding.EditTextFromUpazila.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextToDistrict.getText().toString())) {
            binding.EditTextToDistrict.setError("Required");
            binding.EditTextToDistrict.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextToUpazila.getText().toString())) {
            binding.EditTextToUpazila.setError("Required");
            binding.EditTextToUpazila.requestFocus();
            progressDialog.dismiss();
            return false;
        }
        if (TextUtils.isEmpty(binding.EditTextReceiverPhoneNumber.getText().toString())) {
            binding.EditTextReceiverPhoneNumber.setError("Required");
            binding.EditTextReceiverPhoneNumber.requestFocus();
            return false;
        }
        if (binding.cppReceiverPhoneNumber.getFullNumberWithPlus().trim()
                .equals(postOwnersPhoneNumber)) {
            binding.EditTextReceiverPhoneNumber.setError("It's your number");
            binding.EditTextReceiverPhoneNumber.requestFocus();
            return false;
        }
        return true;
    }

    // ProgressBar Setup
    private void showProgressBar() {
        // Show progressBar
        progressDialog = new ProgressDialog(PostActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }

    // Display no internet
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(PostActivity.this, android.R.layout.simple_list_item_1, districts);
        binding.EditTextFromDistrict.setAdapter(adapter);  // District
        binding.EditTextToDistrict.setAdapter(adapter);    // District

        // UpazilaFrom Recommendation
        binding.EditTextFromDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazillas = DistrictUpazillaAdapter.getUpazillas(PostActivity.this, district);
                if (upazillas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(PostActivity.this, android.R.layout.simple_list_item_1, upazillas);
                    binding.EditTextFromUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });

        // UpazilaTo Recommendation
        binding.EditTextToDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = adapter.getItem(position);
                String[] upazillas = DistrictUpazillaAdapter.getUpazillas(PostActivity.this, district);
                if (upazillas != null) {
                    ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(PostActivity.this, android.R.layout.simple_list_item_1, upazillas);
                    binding.EditTextToUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                }
            }
        });
    }

    // Clicking Event on termsAndCondition textView
    public void termsAndCondition(View view) {
        Intent intent = new Intent(PostActivity.this,FragmentContainerActivity.class);
        intent.putExtra("whatToDo","termsAndCondition");
        startActivity(intent);
    }
}