package com.example.loginscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.loginscreen.MainActivity2;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize EditText and Button
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        // Set OnClickListener for loginButton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get entered username and password
                String enteredUsername = username.getText().toString();
                String enteredPassword = password.getText().toString();

                // Check if entered username and password match the predefined values
                if (enteredUsername.equals("user") && enteredPassword.equals("1234")) {
                    // Display a toast message indicating successful login
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity2.java
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    startActivity(intent);
                } else {
                    // Display a toast message indicating failed login
                    Toast.makeText(MainActivity.this, "Login Failed! Please check your username and password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set OnClickListener for signupText Button
        Button signupText = findViewById(R.id.signupText);
        Intent iNext = new Intent(MainActivity.this, createaccActivity.class);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(iNext);
            }
        });
    }
}
