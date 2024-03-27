package com.example.loginscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class localpolice extends AppCompatActivity {
    Button btnPolice, btnAmbulance, btnFirePolice, btnWomenSafety, btnChildSafety, btnBookHospital;
    //    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_localpolice);

        btnPolice = findViewById(R.id.btnPolice);
        btnAmbulance = findViewById(R.id.btnAmbulance);
        btnFirePolice = findViewById(R.id.btnFirePolice);
        btnWomenSafety = findViewById(R.id.btnWomenSafety);
        btnChildSafety = findViewById(R.id.btnChildSafety);
        btnBookHospital = findViewById(R.id.btnBookHospital);
//        Button btnPolice= findViewById(R.id.btnPolice);
        // In localpolice.java
        String message = getIntent().getStringExtra("message");
// Use the message here


        btnPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Calling Police", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91100"));
                startActivity(intent);
            }
        });

        btnAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Calling Ambulance", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91108"));
                startActivity(intent);
            }
        });
        btnFirePolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Calling Fire Police", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91101"));
                startActivity(intent);
            }
        });
        btnWomenSafety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Calling Women Safety Helpline", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:4856135435"));
                startActivity(intent);
            }
        });

        btnChildSafety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Calling Child Helpline", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"+Uri.encode("+911098")));
                intent.putExtra("sms_body","hello how are you");

                startActivity(intent);
            }
        });

        btnBookHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(localpolice.this, "Hospital Booked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"+Uri.encode("+9158641635486")));
                intent.putExtra("sms_body","hello how are you");

                startActivity(intent);
            }
        });


    }
}