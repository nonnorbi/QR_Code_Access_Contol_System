package com.nonnorbi.qrcs;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Registration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button      createBtn;

    TextView    signin;

    EditText    lname;
    EditText    fname;
    EditText    emailaddress;
    EditText    pnumber;

    Spinner     gender;
    Spinner     type;

    private final static String KEY_FIRSTNAME = "key_firstname";
    private final static String KEY_LASTNAME  = "key_lastname";
    private final static String KEY_EMAIL     = "key_email";
    private final static String KEY_PHONE     = "key_phone";

    String        name  = "";
    String        ID    = "";
    SecretKeySpec key   = null;


    private FirebaseFirestore   db       = FirebaseFirestore.getInstance();
    private CollectionReference userRef  = db.collection("user");

    static final String TAG = "Read Data Activity";


    //Encryption
    public String encryptMsg(String message, SecretKeySpec secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterException, IllegalBlockSizeException, BadPaddingException,
            UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

    //Decyption
    public String decryptMsg(String cipherText, SecretKeySpec secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        byte[] decode = Base64.decode(cipherText, Base64.NO_WRAP);
        String decryptString = new String(cipher.doFinal(decode), "UTF-8");
        return decryptString;
    }

    //Key generator
    public static SecretKeySpec generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int keySize = 128;
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(keySize);
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(raw, "AES");
        return key;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        createBtn    = findViewById(R.id.create);
        signin       = findViewById(R.id.signin);
        lname        = findViewById(R.id.lname);
        fname        = findViewById(R.id.fname);
        emailaddress = findViewById(R.id.emailaddress);
        pnumber      = findViewById(R.id.pnumber);
        gender       = findViewById(R.id.genderS);
        type         = findViewById(R.id.typeS);

        gender.setOnItemSelectedListener(this);
        List<String> genderS = new ArrayList<>();
        genderS.add("Male");
        genderS.add("Female");

        ArrayAdapter<String> dataAdapterg =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderS);
        dataAdapterg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapterg);

        type.setOnItemSelectedListener(this);
        List<String> typeS = new ArrayList<>();
        typeS.add("Daily");
        typeS.add("Weeky");
        typeS.add("Mounthly");
        typeS.add("Quarterly");
        typeS.add("Annual");

        ArrayAdapter<String> dataAdaptert =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeS);
        dataAdaptert.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(dataAdaptert);


        createBtn.setOnClickListener(v -> {

            String fName   = fname.getText().toString();
            String lName   = lname.getText().toString();
            String eMail   = emailaddress.getText().toString();
            String phone   = pnumber.getText().toString();
            String gender_ = gender.getSelectedItem().toString();
            String type_   = type.getSelectedItem().toString();

            name = lName + " " + fName;

            //ID GEN
            int           i         = (int) (new Date().getTime());
            int           random    = i * (new Random().nextInt(10000000));
            String        string    = phone + name + i
                    + random + eMail + type_ + random + random + random + gender_;

            try {
                key = generateKey();
                System.out.println("Titkosított: " + encryptMsg(string, key)
                        + ", key: " + key);
                System.out.println(name);
                ID = encryptMsg(string, key);
                System.out.println("Visszafejtett: " + decryptMsg(encryptMsg(string, key), key));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                    IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException |
                    InvalidAlgorithmParameterException | InvalidParameterSpecException
                    | InvalidKeySpecException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), Photo.class);
            intent.putExtra("KEY_NAME", name);
            intent.putExtra("KEY_ID", ID);
            intent.putExtra("KEY_FNAME", fName);
            intent.putExtra("KEY_LNAME", lName);
            intent.putExtra("KEY_EMAIL", eMail);
            intent.putExtra("KEY_PHONE", phone);
            intent.putExtra("KEY_GENDER", gender_);
            intent.putExtra("KEY_TYPE", type_);

            startActivity(intent);
        });

        signin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                Login.class)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_FIRSTNAME, fname.getText().toString());
        outState.putString(KEY_LASTNAME, lname.getText().toString());
        outState.putString(KEY_EMAIL, emailaddress.getText().toString());
        outState.putString(KEY_PHONE, pnumber.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lname.setText( savedInstanceState.getString(KEY_LASTNAME) );
        fname.setText( savedInstanceState.getString(KEY_FIRSTNAME) );
        emailaddress.setText( savedInstanceState.getString(KEY_EMAIL) );
        pnumber.setText( savedInstanceState.getString(KEY_PHONE) );
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}