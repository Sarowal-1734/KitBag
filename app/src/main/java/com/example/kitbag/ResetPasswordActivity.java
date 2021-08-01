package com.example.kitbag;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.kitbag.databinding.ActivityResetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;

    // For Authentication
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // remove search icon and notification icon from appBar
        binding.customAppBar.appbarImageviewSearch.setVisibility(View.GONE);
        binding.customAppBar.appbarNotificationIcon.notificationIcon.setVisibility(View.GONE);

        // Adding back arrow in the appBar
        binding.customAppBar.appbarLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        binding.customAppBar.appbarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Change the title of the appBar
        binding.customAppBar.appbarTitle.setText("Reset Password");

        // Open Drawer Layout
        binding.customAppBar.appbarImageviewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
            }
        });

    }

    public void onResetPasswordButtonClicked(View view) {
        boolean valid = validation();
        if (valid) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String newPassword = binding.editTextPassword.getText().toString();
            currentUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validation() {
        if (TextUtils.isEmpty(binding.editTextPassword.getText().toString())) {
            binding.editTextPassword.setError("Required");
            return false;
        } else if (binding.editTextPassword.getText().toString().length() < 8) {
            binding.editTextPassword.setError("Minimum 8 characters");
            binding.editTextPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(binding.editTextConfirmPassword.getText().toString())) {
            binding.editTextConfirmPassword.setError("Required");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        } else if (binding.editTextConfirmPassword.getText().toString().length() < 8) {
            binding.editTextConfirmPassword.setError("Minimum 8 characters");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        }
        if (!binding.editTextPassword.getText().toString().equals(binding.editTextConfirmPassword.getText().toString())){
            binding.editTextConfirmPassword.setError("Password doesn't match");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }
}