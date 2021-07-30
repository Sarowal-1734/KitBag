package com.example.kitbag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.kitbag.databinding.ActivityOtpVerificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OTP_Verification extends AppCompatActivity {
    private ActivityOtpVerificationBinding binding;

    // For Authentication
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;
    private FirebaseUser currentUser;

    private String otpID;

    // For database Database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();

        // Picking value which send from signUp activity
        String mobile = getIntent().getStringExtra("mobile");
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        // mange OTP
        manageOTP(mobile);

        binding.buttonVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(binding.editTextOTP.getText().toString().trim())){
                    Toast.makeText(OTP_Verification.this, "Please Enter 6 digit code", Toast.LENGTH_SHORT).show();
                }else if(binding.editTextOTP.getText().toString().length() !=6){
                    Toast.makeText(OTP_Verification.this, "Please Enter 6 digit code", Toast.LENGTH_SHORT).show();
                }else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpID,binding.editTextOTP.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void manageOTP(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            otpID = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull @org.jetbrains.annotations.NotNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull @org.jetbrains.annotations.NotNull FirebaseException e) {
            Toast.makeText(OTP_Verification.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // We take user to add journalActivity
                    currentUser = auth.getCurrentUser();
                    if(currentUser != null);
                    String currentUserId = currentUser.getUid();

                    // Picking value which send from signUp activity
                    String mobile = getIntent().getStringExtra("mobile");
                    String username = getIntent().getStringExtra("username");
                    String email = getIntent().getStringExtra("email");
                    String password = getIntent().getStringExtra("password");

                    // Create a user map so that we can create and add a user in the database user collection

                    Map<String,String> userObj = new HashMap<>();
                    userObj.put("userId",currentUserId);
                    userObj.put("username",username);
                    userObj.put("mobile",mobile);
                    userObj.put("email",email);
                    userObj.put("password",password);

                    collectionReference.add(userObj);

                    startActivity(new Intent(OTP_Verification.this,MainActivity.class));
                }else {
                    Toast.makeText(OTP_Verification.this, "OTP Doesn't Match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}