package com.nonnorbi.qrcs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    Button   scannBtn;
    TextView signup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        scannBtn = findViewById(R.id.scannQRcode);
        signup   = findViewById(R.id.signup);

        scannBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                Scanner.class)));

        signup.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                Registration.class)));
    }

}