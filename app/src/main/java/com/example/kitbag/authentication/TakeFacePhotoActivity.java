package com.example.kitbag.authentication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityTakeFacePhotoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class TakeFacePhotoActivity extends AppCompatActivity {

    private ActivityTakeFacePhotoBinding binding;


    // Swipe to back
    private SlidrInterface slidrInterface;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    private ImageCapture imageCapture;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakeFacePhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Hide appBar icons
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("Take Face Photo");

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

        // Open the Camera Preview
        startCamera();

        // On submit clicked
        binding.textViewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TakeFacePhotoActivity.this, NidInformationActivity.class);
                intent.putExtra("UserFace", imageUri.toString());
                intent.putExtra("FrontNID", getIntent().getStringExtra("FrontNID"));
                intent.putExtra("BackNID", getIntent().getStringExtra("BackNID"));
                startActivity(intent);
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
                takePicture();
            }
        });

    } // Ending onCreate

    private void reStartCamera() {
        binding.imageViewPreviewImage.setImageURI(null);
        binding.cardViewPreviewImage.setVisibility(View.GONE);
        binding.cardViewPreviewView.setVisibility(View.VISIBLE);
        binding.imageViewCaptureImage.setEnabled(true);
        binding.imageViewCaptureImage.clearColorFilter();
        binding.textViewSubmit.setEnabled(false);
        binding.textViewSubmit.setTextColor(Color.GRAY);
        binding.textViewReTake.setEnabled(false);
        binding.textViewReTake.setTextColor(Color.GRAY);
    }

    private void takePicture() {
        File photoFile = new File(getCacheDir().getAbsolutePath(), "USERFACE.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(TakeFacePhotoActivity.this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        imageUri = Uri.fromFile(photoFile);
                        binding.cardViewPreviewView.setVisibility(View.GONE);
                        binding.cardViewPreviewImage.setVisibility(View.VISIBLE);
                        binding.imageViewPreviewImage.setImageURI(imageUri);
                        binding.imageViewCaptureImage.setEnabled(false);
                        binding.imageViewCaptureImage.setColorFilter(Color.GRAY);
                        binding.textViewSubmit.setEnabled(true);
                        binding.textViewSubmit.setTextColor(Color.BLACK);
                        binding.textViewReTake.setEnabled(true);
                        binding.textViewReTake.setTextColor(Color.BLACK);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(TakeFacePhotoActivity.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
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
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
                    // Set image dimension according to the preview
                    imageCapture.setCropAspectRatio(new Rational(200, 200));
                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle((LifecycleOwner) TakeFacePhotoActivity.this, cameraSelector, preview, imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

}