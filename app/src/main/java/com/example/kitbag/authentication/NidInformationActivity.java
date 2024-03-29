package com.example.kitbag.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityNidInformationBinding;
import com.example.kitbag.model.ModelClassDeliveryman;
import com.example.kitbag.model.UserModel;
import com.example.kitbag.notification.FcmNotificationsSender;
import com.example.kitbag.ui.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

public class NidInformationActivity extends AppCompatActivity {

    private ActivityNidInformationBinding binding;

    // Show progressBar
    private ProgressDialog progressDialog;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private UserModel userModel;

    private ArrayList<Uri> imageUriLists = new ArrayList<>();
    private int i = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityNidInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Hide appBar icons
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("NID Information");

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
            // Get image from database and set to the appBar
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            userModel = documentSnapshot.toObject(UserModel.class);
                            if (documentSnapshot.getString("imageUrl") != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        }

        // Get Intent Data
        String UserFace = getIntent().getStringExtra("UserFace");
        String FrontNID = getIntent().getStringExtra("FrontNID");
        String BackNID = getIntent().getStringExtra("BackNID");
        // String to Uri (Images)
        imageUriLists.add(Uri.parse(UserFace));
        imageUriLists.add(Uri.parse(FrontNID));
        imageUriLists.add(Uri.parse(BackNID));

        // On Submit Button Clicked
        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
                    if (isConnected()) {
                        // inflate custom layout
                        View view = LayoutInflater.from(NidInformationActivity.this).inflate(R.layout.dialog_nid_submission, null);
                        if (getIntent().getStringExtra("whatToDo").equals("AgentRegistration")) {
                            TextView textView = view.findViewById(R.id.textViewStep1);
                            textView.setText(R.string.information_provided_for_nkitbag_agent_account_nregistration_is_correct_ndeliberate_and_authentic);
                        }
                        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
                        AlertDialog.Builder builder = new AlertDialog.Builder(NidInformationActivity.this);
                        builder.setView(view);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        buttonConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                showProgressBar();
                                if (getIntent().getStringExtra("whatToDo").equals("AgentRegistration")) {
                                    storeAgentInfo();
                                    storeAgentImages(imageUriLists);
                                } else {
                                    storeDeliverymanInfo();
                                    storeDeliverymanImages(imageUriLists);
                                }
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        // Show that no connection
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
            }
        });
    } // Ending onCreate

    private void storeAgentImages(ArrayList<Uri> imageUriLists) {
        ProgressDialog progressBarDialog = new ProgressDialog(NidInformationActivity.this);
        progressBarDialog.show();
        progressBarDialog.setContentView(R.layout.progress_image_uploading);
        progressBarDialog.setCancelable(false);
        TextView textView = progressBarDialog.findViewById(R.id.progressTextViewImage);
        TextView textViewCount = progressBarDialog.findViewById(R.id.progressTextCount);
        ProgressBar progressBarmage = progressBarDialog.findViewById(R.id.progressImage);
        progressBarDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // Store image to firebase
        int imageUriCount;
        for (imageUriCount = 0; imageUriCount < imageUriLists.size(); imageUriCount++) {
            StorageReference filepath = storageReference.child("agent_images").child(currentUser.getUid() + new Timestamp(new Date()));
            filepath.putFile(imageUriLists.get(imageUriCount))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    i++;
                                    // Update user info in Database
                                    if (i == 0) {
                                        db.collection("Agent").document(currentUser.getUid()).update("imageUrlUserFace", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                    }
                                    if (i == 1) {
                                        db.collection("Agent").document(currentUser.getUid()).update("imageUrlFrontNID", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                    }
                                    if (i == 2) {
                                        db.collection("Agent").document(currentUser.getUid()).update("imageUrlBackNID", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                        progressBarDialog.dismiss();
                                        progressDialog.dismiss();
                                        showDialog();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(NidInformationActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            int count = i + 2;
                            double progress = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBarmage.setProgress((int) progress);
                            textViewCount.setText(count + "/3");
                            textView.setText("Uploading images: " + progress + "%");
                        }
                    });
        }
    }

    private void storeAgentInfo() {
        ModelClassDeliveryman modelClassDeliveryman = new ModelClassDeliveryman();
        modelClassDeliveryman.setUserId(currentUser.getUid());
        modelClassDeliveryman.setUserType(userModel.getUserType());
        modelClassDeliveryman.setPhoneNumber(userModel.getPhoneNumber());
        modelClassDeliveryman.setNameBangla(binding.editTextNameBangla.getText().toString());
        modelClassDeliveryman.setNameEnglish(binding.editTextNameEnglish.getText().toString());
        modelClassDeliveryman.setFatherHusbandName(binding.editTextFatherHusbandName.getText().toString());
        modelClassDeliveryman.setMotherName(binding.editTextMotherName.getText().toString());
        modelClassDeliveryman.setDateOfBirth(binding.editTextDateOfBirth.getText().toString());
        modelClassDeliveryman.setNidNumber(binding.editTextNidNumber.getText().toString());
        modelClassDeliveryman.setPresentAddress(binding.editTextPresentAddress.getText().toString());
        modelClassDeliveryman.setPostCode(binding.editTextPostCode.getText().toString());
        modelClassDeliveryman.setPostOffice(binding.editTextPostOffice.getText().toString());
        modelClassDeliveryman.setThana(binding.editTextThana.getText().toString());
        modelClassDeliveryman.setDistrict(binding.editTextDistrict.getText().toString());
        modelClassDeliveryman.setDivision(binding.editTextDivision.getText().toString());
        modelClassDeliveryman.setGender(binding.editTextGender.getText().toString());
        modelClassDeliveryman.setOccupation(binding.editTextOccupation.getText().toString());
        modelClassDeliveryman.setApplicationStatus("Pending");
        modelClassDeliveryman.setTimeApplied(new Timestamp(new Date()));
        modelClassDeliveryman.setApprovedByAgent(null);
        modelClassDeliveryman.setImageUrlUserFace(null);
        modelClassDeliveryman.setImageUrlFrontNID(null);
        modelClassDeliveryman.setImageUrlBackNID(null);
        db.collection("Agent").document(currentUser.getUid()).set(modelClassDeliveryman)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(NidInformationActivity.this, "Please wait uploading images...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeDeliverymanInfo() {
        ModelClassDeliveryman modelClassDeliveryman = new ModelClassDeliveryman();
        modelClassDeliveryman.setUserId(currentUser.getUid());
        modelClassDeliveryman.setUserType(userModel.getUserType());
        modelClassDeliveryman.setPhoneNumber(userModel.getPhoneNumber());
        modelClassDeliveryman.setNameBangla(binding.editTextNameBangla.getText().toString());
        modelClassDeliveryman.setNameEnglish(binding.editTextNameEnglish.getText().toString());
        modelClassDeliveryman.setFatherHusbandName(binding.editTextFatherHusbandName.getText().toString());
        modelClassDeliveryman.setMotherName(binding.editTextMotherName.getText().toString());
        modelClassDeliveryman.setDateOfBirth(binding.editTextDateOfBirth.getText().toString());
        modelClassDeliveryman.setNidNumber(binding.editTextNidNumber.getText().toString());
        modelClassDeliveryman.setPresentAddress(binding.editTextPresentAddress.getText().toString());
        modelClassDeliveryman.setPostCode(binding.editTextPostCode.getText().toString());
        modelClassDeliveryman.setPostOffice(binding.editTextPostOffice.getText().toString());
        modelClassDeliveryman.setThana(binding.editTextThana.getText().toString());
        modelClassDeliveryman.setDistrict(binding.editTextDistrict.getText().toString());
        modelClassDeliveryman.setDivision(binding.editTextDivision.getText().toString());
        modelClassDeliveryman.setGender(binding.editTextGender.getText().toString());
        modelClassDeliveryman.setOccupation(binding.editTextOccupation.getText().toString());
        modelClassDeliveryman.setApplicationStatus("Pending");
        modelClassDeliveryman.setTimeApplied(new Timestamp(new Date()));
        modelClassDeliveryman.setApprovedByAgent(null);
        modelClassDeliveryman.setImageUrlUserFace(null);
        modelClassDeliveryman.setImageUrlFrontNID(null);
        modelClassDeliveryman.setImageUrlBackNID(null);
        db.collection("Deliveryman").document(currentUser.getUid()).set(modelClassDeliveryman)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(NidInformationActivity.this, "Please wait uploading images...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeDeliverymanImages(ArrayList<Uri> imageUriLists) {
        ProgressDialog progressBarDialog = new ProgressDialog(NidInformationActivity.this);
        progressBarDialog.show();
        progressBarDialog.setCancelable(false);
        progressBarDialog.setContentView(R.layout.progress_image_uploading);
        TextView textView = progressBarDialog.findViewById(R.id.progressTextViewImage);
        TextView textViewCount = progressBarDialog.findViewById(R.id.progressTextCount);
        ProgressBar progressBarmage = progressBarDialog.findViewById(R.id.progressImage);
        progressBarDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // Store image to firebase
        int imageUriCount;
        for (imageUriCount = 0; imageUriCount < imageUriLists.size(); imageUriCount++) {
            StorageReference filepath = storageReference.child("deliveryman_images").child(currentUser.getUid() + new Timestamp(new Date()));
            filepath.putFile(imageUriLists.get(imageUriCount))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    i++;
                                    // Update user info in Database
                                    if (i == 0) {
                                        db.collection("Deliveryman").document(currentUser.getUid()).update("imageUrlUserFace", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                    }
                                    if (i == 1) {
                                        db.collection("Deliveryman").document(currentUser.getUid()).update("imageUrlFrontNID", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                    }
                                    if (i == 2) {
                                        db.collection("Deliveryman").document(currentUser.getUid()).update("imageUrlBackNID", uri.toString());
                                        textView.setText("Uploading images: " + "100%");
                                        progressBarDialog.dismiss();
                                        progressDialog.dismiss();
                                        showDialog();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(NidInformationActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            int count = i + 2;
                            double progress = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBarmage.setProgress((int) progress);
                            textViewCount.setText(count + "/3");
                            textView.setText("Uploading images: " + progress + "%");
                        }
                    });
        }
    }

    private void showDialog() {
        String title = "Deliveryman Application";
        String message = "Your application has been successfully submitted.";
        if (getIntent().getStringExtra("whatToDo").equals("AgentRegistration")) {
            // Send a message to the user phone number
            String textMessage = getString(R.string.agent_application_successfully_submitted);
            sendMessage(textMessage);
            sendNotification("Agent Application", message);
        } else {
            // Send a message to the user phone number
            String textMessage = getString(R.string.deliveryman_application_successfully_submitted);
            sendMessage(textMessage);
            sendNotification(title, message);
        }
        // Show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NidInformationActivity.this);
        builder.setTitle("Submitted");
        builder.setMessage(R.string.confirmation_of_deliveryman_registration);
        builder.setCancelable(false);
        builder.setPositiveButton(
                R.string.got_it,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startActivity(new Intent(NidInformationActivity.this, MainActivity.class));
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Send Notification
    private void sendNotification(String title, String message) {
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userModel.getUserToken(),
                currentUser.getUid(), title, message, getApplicationContext(), NidInformationActivity.this);
        notificationsSender.SendNotifications();
    }

    private void sendMessage(String textMessage) {
        String number = userModel.getPhoneNumber().substring(3, 14);
        // Create a background thread to send OTP
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String apiKey = "api_key=" + "jWWu9013if833V1c503DYJs3k61VMDYT3yXy76J9";
                    String message = "&msg=" + textMessage;
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

    // Check the internet connection
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean valid() {
        if (TextUtils.isEmpty(binding.editTextNameBangla.getText().toString())) {
            binding.editTextNameBangla.setError("Required");
            binding.editTextNameBangla.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextNameEnglish.getText().toString())) {
            binding.editTextNameEnglish.setError("Required");
            binding.editTextNameEnglish.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextFatherHusbandName.getText().toString())) {
            binding.editTextFatherHusbandName.setError("Required");
            binding.editTextFatherHusbandName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextMotherName.getText().toString())) {
            binding.editTextMotherName.setError("Required");
            binding.editTextMotherName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextDateOfBirth.getText().toString())) {
            binding.editTextDateOfBirth.setError("Required");
            binding.editTextDateOfBirth.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextNidNumber.getText().toString())) {
            binding.editTextNidNumber.setError("Required");
            binding.editTextNidNumber.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextPresentAddress.getText().toString())) {
            binding.editTextPresentAddress.setError("Required");
            binding.editTextPresentAddress.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextPostCode.getText().toString())) {
            binding.editTextPostCode.setError("Required");
            binding.editTextPostCode.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextPostOffice.getText().toString())) {
            binding.editTextPostOffice.setError("Required");
            binding.editTextPostOffice.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextThana.getText().toString())) {
            binding.editTextThana.setError("Required");
            binding.editTextThana.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextDistrict.getText().toString())) {
            binding.editTextDistrict.setError("Required");
            binding.editTextDistrict.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextDivision.getText().toString())) {
            binding.editTextDivision.setError("Required");
            binding.editTextDivision.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextGender.getText().toString())) {
            binding.editTextGender.setError("Required");
            binding.editTextGender.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextOccupation.getText().toString())) {
            binding.editTextOccupation.setError("Required");
            binding.editTextOccupation.requestFocus();
            return false;
        }
        return true;
    }

    // ProgressBar Setup
    private void showProgressBar() {
        // Show progressBar
        progressDialog = new ProgressDialog(NidInformationActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
    }
}