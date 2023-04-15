package com.nonnorbi.qrcs;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Login_Registration extends AppCompatActivity {

    Button loginBtn;
    Button regBtn;

    private FirebaseFirestore   db       = FirebaseFirestore.getInstance();
    private CollectionReference userRef  = db.collection("user");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        loginBtn = findViewById(R.id.login);
        regBtn   = findViewById(R.id.registration);

        loginBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                Login.class)));

        regBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                Registration.class)));
    }
}