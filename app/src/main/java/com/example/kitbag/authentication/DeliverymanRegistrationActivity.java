package com.example.kitbag.authentication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.kitbag.R;
import com.example.kitbag.data.SharedPreference;
import com.example.kitbag.databinding.ActivityDeliverymanRegistrationBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class DeliverymanRegistrationActivity extends AppCompatActivity {

    // Binding our activity
    private ActivityDeliverymanRegistrationBinding binding;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    private static final int REQUEST_CODE = 10;
    private boolean mPermissions = false;

    private int submitClicked = 0;
    private ImageCapture imageCapture;
    private Uri imageUri, imageUriFrontNID, imageUriBackNID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DarkMode Enable or Disable
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_Night);
        } else {
            setTheme(R.style.Theme_Day);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverymanRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Hide appBar icons
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.setVisibility(View.GONE);

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("Take NID Photo");

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Get image from database and set to the appBar
        if (currentUser != null) {
            // User is signed in
            collectionReference.document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.getString("imageUrl") != null) {
                                // Picasso library for download & show image
                                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.drawable.ic_profile).fit().centerCrop().into(binding.customAppBar.appbarImageviewProfile);
                            }
                        }
                    });
        }

        // Start Permissions checkup
        init();

        // On submit clicked
        binding.textViewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitClicked++;
                if (submitClicked == 1) {
                    binding.textViewNID.setText(R.string.back_of_nid);
                    imageUriFrontNID = imageUri;
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DeliverymanRegistrationActivity.this);
                    builder1.setTitle(R.string.now_capture_the_back_of_your_NID);
                    builder1.setCancelable(false);
                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    reStartCamera();
                } else {
                    imageUriBackNID = imageUri;
                    Intent intent = new Intent(DeliverymanRegistrationActivity.this, TakeFacePhotoActivity.class);
                    intent.putExtra("FrontNID", imageUriFrontNID.toString());
                    intent.putExtra("BackNID", imageUriBackNID.toString());
                    intent.putExtra("whatToDo", getIntent().getStringExtra("whatToDo"));
                    startActivity(intent);
                }
            }
        });

        // On reTake Clicked
        binding.textViewReTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reStartCamera();
            }
        });

        // On Capture Image
        binding.imageViewCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPermissions) {
                    takePicture();
                } else {
                    init();
                }
            }
        });

    } // Ending onCreate

    private void reStartCamera() {
        binding.imageViewPreviewImage.setImageURI(null);
        binding.cardViewPreviewImage.setVisibility(View.GONE);
        binding.cardViewPreviewView.setVisibility(View.VISIBLE);
        binding.imageViewCaptureImage.setEnabled(true);
        binding.imageViewCaptureImage.clearColorFilter();
        binding.textViewSubmit.setVisibility(View.GONE);
        binding.textViewReTake.setVisibility(View.GONE);
    }

    private void takePicture() {
        File photoFile;
        if (submitClicked == 0) {
            photoFile = new File(getCacheDir().getAbsolutePath(), "NIDFRONT.jpg");
        } else {
            photoFile = new File(getCacheDir().getAbsolutePath(), "NIDBACK.jpg");
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(DeliverymanRegistrationActivity.this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        imageUri = Uri.fromFile(photoFile);
                        binding.cardViewPreviewView.setVisibility(View.GONE);
                        binding.cardViewPreviewImage.setVisibility(View.VISIBLE);
                        binding.imageViewPreviewImage.setImageURI(imageUri);
                        binding.imageViewCaptureImage.setEnabled(false);
                        binding.imageViewCaptureImage.setColorFilter(Color.GRAY);
                        binding.textViewSubmit.setVisibility(View.VISIBLE);
                        binding.textViewReTake.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(DeliverymanRegistrationActivity.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void init() {
        if (mPermissions) {
            // Open the Camera Preview
            startCamera();
        } else {
            verifyPermissions();
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Camera provider is now guaranteed to be available
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    // Set up the view finder use case to display camera preview
                    Preview preview = new Preview.Builder().build();
                    // Set up the capture use case to allow users to take photos
                    imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                    // Choose the camera Front or Back
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    // Set image dimension according to the preview
                    imageCapture.setCropAspectRatio(new Rational(350, 200));
                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle((LifecycleOwner) DeliverymanRegistrationActivity.this, cameraSelector, preview, imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void verifyPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if ((ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED)) {
            mPermissions = true;
            init();
        } else {
            ActivityCompat.requestPermissions(DeliverymanRegistrationActivity.this, permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (mPermissions) {
                init();
            } else {
                verifyPermissions();
            }
        }
    }

}