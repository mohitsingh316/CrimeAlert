package com.example.loginscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class createaccActivity extends AppCompatActivity {

    EditText signupUsername, signupPassword, signupConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createacc);

        // Initialize EditText fields
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirm_password);

        // Set OnClickListener for signupButton
        Button signupButton = findViewById(R.id.signupButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    // Method to create the account
    private void createAccount() {
        String username = signupUsername.getText().toString();
        String password = signupPassword.getText().toString();
        String confirmPassword = signupConfirmPassword.getText().toString();

        // Check if username, password, and confirm password are not empty
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password matches confirm password
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Your logic for creating the account goes here

        // Display a success message
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

        // Start MainActivity2
        Intent intent = new Intent(createaccActivity.this, MainActivity2.class);
        startActivity(intent);
    }
}
