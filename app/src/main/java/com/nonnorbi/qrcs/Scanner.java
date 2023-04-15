package com.nonnorbi.qrcs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scanner extends AppCompatActivity {

    CodeScanner         codeScanner;
    CodeScannerView     scannerView;
    TextView            resultData;
    ImageView           allow;
    ImageView           denied;


    private FirebaseFirestore   db       = FirebaseFirestore.getInstance();
    private CollectionReference userRef  = db.collection("user");

    private FirebaseAuth mAuth;
    private final static String TAGF = "Sign In: ";

    static final String TAG = "Read Data Activity";
    String value = "";
    String ID    = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        mAuth = FirebaseAuth.getInstance();

        scannerView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, scannerView);
        resultData  = findViewById(R.id.resultOfQr);
        allow       = findViewById(R.id.allow);
        denied      = findViewById(R.id.denied);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 123);
        }else {

            codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
                resultData.setText(result.getText());
                value = result.getText().trim();

                for (int i = 0; i < value.length(); i++) {
                    ID += (char) ((int) (value.charAt(i)) + 1);
                }

                checkID(ID);

                Handler handler = new Handler();
                handler.postDelayed(() ->
                                startActivity(new Intent(getApplicationContext(), Login_Registration.class)),
                        2500);
            }));
        }
    }

    protected void checkID(String value) {

       /* db.collectionGroup("pass").whereEqualTo("ID", value).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        if (doc.exists()) {
                            if (doc.getTimestamp("Expiration_date").compareTo(Timestamp.now()) == 1) {
                                Toast.makeText(getApplicationContext(), "ALLOW", Toast.LENGTH_SHORT).show();
                                allow.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(), "DENIED, PASS HAS EXPIRED", Toast.LENGTH_LONG).show();
                                denied.setVisibility(View.VISIBLE);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), "DENIED", Toast.LENGTH_SHORT).show();
                            denied.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });*/
        db.collectionGroup("pass").whereEqualTo("ID", value).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()){
                    Toast.makeText(getApplicationContext(), "DENIED", Toast.LENGTH_SHORT).show();
                    denied.setVisibility(View.VISIBLE);
                } else {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        if ( doc.getTimestamp("Expiration_date").compareTo( Timestamp.now() ) == 1 ){
                            Toast.makeText(getApplicationContext(), "ALLOW", Toast.LENGTH_SHORT).show();
                            allow.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getApplicationContext(), "DENIED, PASS HAS EXPIRED", Toast.LENGTH_LONG).show();
                            denied.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onStart() {
        String email = "kis.norbert.g.95@gmail.com";
        String password = "KnG9581";
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            return;
        }else{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(TAGF, "signInWithEmail: success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    }else{
                        Log.w(TAGF, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}