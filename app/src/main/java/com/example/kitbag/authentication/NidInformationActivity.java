package com.example.kitbag.authentication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kitbag.R;
import com.example.kitbag.databinding.ActivityNidInformationBinding;
import com.example.kitbag.ui.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

public class NidInformationActivity extends AppCompatActivity {

    private ActivityNidInformationBinding binding;

    // Swipe to back
    private SlidrInterface slidrInterface;

    // For Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNidInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Hide appBar icons
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Change appBar title
        binding.customAppBar.appbarTitle.setText("NID Information");

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
            // Get image from database and set to the appBar
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

        /* Get Intent Data
        String UserFace = getIntent().getStringExtra("UserFace");
        String FrontNID = getIntent().getStringExtra("FrontNID");
        String BackNID = getIntent().getStringExtra("BackNID");
        // String to Uri (Images)
        Uri imageUriUserFace = Uri.parse(UserFace);
        Uri imageUriFrontNID = Uri.parse(FrontNID);
        Uri imageUriBackNID = Uri.parse(BackNID);*/

        // On Submit Button Clicked
        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inflate custom layout
                View view = LayoutInflater.from(NidInformationActivity.this).inflate(R.layout.dialog_nid_submission, null);
                Button buttonConfirm = view.findViewById(R.id.buttonConfirm);
                AlertDialog.Builder builder = new AlertDialog.Builder(NidInformationActivity.this);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.show();
                buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        //TODO Store Deliveryman Info To The Database and Show a dialog which includes further instructions
                        startActivity(new Intent(NidInformationActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        });
    } // Ending onCreate
}