package com.example.kitbag;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.kitbag.databinding.ActivityOtpVerificationBinding;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTP_Verification extends AppCompatActivity {

    private ActivityOtpVerificationBinding binding;

    private String otpId, phoneNumber, userName, email, password, pinViewOTP;

    PhoneAuthProvider.ForceResendingToken mResendToken;

    // For Authentication
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

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

        // Picking value which send from signUp activity
        phoneNumber = getIntent().getStringExtra("mobile");
        userName = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        // Set the phone number in UI
        binding.textViewPhoneNumber.setText("" + phoneNumber);

        // Initialize phone auth callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(OTP_Verification.this, "An OTP has been sent", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(OTP_Verification.this, "Failed to sent OTP!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Toast.makeText(OTP_Verification.this, "An OTP has been sent", Toast.LENGTH_SHORT).show();
                otpId = s;
                mResendToken = forceResendingToken;
            }
        };

        // Initialize phone number verification
        manageOTP();

        // On resend OTP button clicked
        binding.textViewResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
            }
        });

        // Get the OTP from input field
        binding.pinview.setTextColor(Color.BLACK);
        binding.pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                pinViewOTP = pinview.getValue();
            }
        });
    }

    // Initialize phone number verification
    public void manageOTP() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // On resend OTP button clicked
    private void resendVerificationCode() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .setForceResendingToken(mResendToken)     // ForceResendingToken from callbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // On Sign in button clicked
    public void onSignInButtonClicked(View view) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpId, pinViewOTP);
        signInWithPhoneAuthCredential(credential);
    }

    // sign_in_with_phone
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //FirebaseUser user = task.getResult().getUser();
                            // Link phone number as fake email for login
                            String subPhone = phoneNumber.substring(1, 14);
                            AuthCredential authCredential = EmailAuthProvider.getCredential(subPhone + "@gmail.com", password);
                            mAuth.getCurrentUser().linkWithCredential(authCredential);
                            Toast.makeText(OTP_Verification.this, "Registration Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(OTP_Verification.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(OTP_Verification.this, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}