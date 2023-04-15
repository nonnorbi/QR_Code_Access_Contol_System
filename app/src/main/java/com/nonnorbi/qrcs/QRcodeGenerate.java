package com.nonnorbi.qrcs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;


public class QRcodeGenerate extends AppCompatActivity {

    private ImageView qrCodeIV;
    private EditText  dataEdt;
    private Button    saveQrBtn;
    Bitmap            bitmap;
    QRGEncoder        qrgEncoder;

    String            imagesDir;
    String            absPath;
    String            photoPath;
    String            ID;
    String            gender;
    String            type;

    Map<String, Object> user;
    Map<String, Object> pass;
    String   fileName;
    String   fname = "";
    String   lname = "";
    String   email = "";
    String   phone;
    int[]    price = {2000, 8000, 17500, 43500, 145000};
    int      date  = (int) (new Date().getTime());

    private FirebaseFirestore   db        = FirebaseFirestore.getInstance();
    private CollectionReference userRef   = db.collection("user");

    private FirebaseAuth mAuth;
    private final static String TAG = "Sign In: ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodegenerate);

        mAuth = FirebaseAuth.getInstance();

        qrCodeIV      = findViewById(R.id.idIVQRcode);
        dataEdt       = findViewById(R.id.idEdt);
        saveQrBtn     = findViewById(R.id.idBtnSaveQR);

        dataEdt.setText( getIntent().getStringExtra("KEY_ID") );
        photoPath = getIntent().getStringExtra("KEY_PHOTO");

        ID        = getIntent().getStringExtra("KEY_ID");
        fname     = getIntent().getStringExtra("KEY_FNAME");
        lname     = getIntent().getStringExtra("KEY_LNAME");
        email     = getIntent().getStringExtra("KEY_EMAIL");
        phone     = getIntent().getStringExtra("KEY_PHONE");
        gender    = getIntent().getStringExtra("KEY_GENDER");
        type      = getIntent().getStringExtra("KEY_TYPE");

        Matrix mat = new Matrix();
        mat.postRotate(90);
        Bitmap l = BitmapFactory.decodeFile(getIntent().getStringExtra("KEY_PHOTO"));
        Bitmap logo = Bitmap.createBitmap(l, 0, 0, l.getWidth(), l.getHeight(), mat, true);

        //Generate QR Code
        if (TextUtils.isEmpty(dataEdt.getText().toString())){
            Toast.makeText(QRcodeGenerate.this,
                    "Enter some text to Generate QR Code",
                    Toast.LENGTH_SHORT).show();
        } else {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);

            int width   = point.x;
            int height  = point.y;

            int dimen = width < height ? width : height;
            dimen = dimen * 3 / 4;

            qrgEncoder = new QRGEncoder(dataEdt.getText().toString(), null, QRGContents.Type.TEXT, dimen);

            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrCodeIV.setImageBitmap( mergeBitmap(bitmap, logo) );
            } catch (WriterException e) {
                Log.e("Tag", e.toString());
            }
        }

        //Save QR Code
        saveQrBtn.setOnClickListener(v -> {
            try {
                String result = saveImage( mergeBitmap(bitmap, logo) ) ? "Image Saved" : "Image Not Saved";
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                dataEdt.setText(null);

                absPath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString() + File.separator + "QR/" +
                        fname + date +".png";

                //user
                user = new HashMap<>();//user
                user.put("First_name", fname);
                user.put("Last_name", lname);
                user.put("E-mail", email);
                user.put("Phone", phone);
                user.put("Gender", gender);

                //pass
                pass = new HashMap<>();
                Calendar r_date = Calendar.getInstance();
                String mID = "";
                for (int i = 0; i < ID.length(); i++) {
                    mID += (char)( (int)( ID.charAt( i ) ) + 1 );
                }
                System.out.println("IDs:\n" + ID + "\n" + mID);;

                pass.put("ID", mID);
                pass.put("Release_date", r_date.getTime());
                pass.put("Type", type);
                if (type.equals("Daily")) {
                    pass.put("Price", price[0]);
                    r_date.add(Calendar.DAY_OF_MONTH, 1);
                    pass.put("Expiration_date",  r_date.getTime());
                } else if (type.equals("Weekly")){
                    pass.put("Price", price[1]);
                    r_date.add(Calendar.WEEK_OF_MONTH, 1);
                    pass.put("Expiration_date", r_date.getTime());
                } else if (type.equals("Mounthly")) {
                    pass.put("Price", price[2]);
                    r_date.add(Calendar.MONTH, 1);
                    pass.put("Expiration_date", r_date.getTime());
                } else if (type.equals("Quarterly")) {
                    pass.put("Price", price[3]);
                    r_date.add(Calendar.MONTH, 3);
                    pass.put("Expiration_date", r_date.getTime());
                } else if (type.equals("Annual")) {
                    pass.put("Price", price[4]);
                    r_date.add(Calendar.YEAR, 1);
                    pass.put("Expiration_date", r_date.getTime());
                }

                //Add user and pass to Database
                addToDatabase(user, pass);

                Intent intent = new Intent(getApplicationContext(), Mail.class);
                intent.putExtra("KEY_ABSPATH", absPath);
                intent.putExtra("KEY_EMAIL", email);
                intent.putExtra("KEY_FNAME", fname);
                intent.putExtra("KEY_LNAME", lname);
                intent.putExtra("KEY_PHONE", phone);
                intent.putExtra("KEY_TYPE", type);
                intent.putExtra("KEY_EXPDATE", r_date.getTime());
                if (type.equals("Daily")) intent.putExtra("KEY_PRICE", price[0]);
                else if (type.equals("Weekly")) intent.putExtra("KEY_PRICE", price[1]);
                else if (type.equals("Mounthly")) intent.putExtra("KEY_PRICE", price[2]);
                else if (type.equals("Annual")) intent.putExtra("KEY_PRICE", price[3]);
                fileName = fname + date + ".png";
                intent.putExtra("KEY_FILENAME", fileName);

                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    //Merge picture and qr code
    public Bitmap mergeBitmap(Bitmap qrCode, Bitmap logo){
        Bitmap combined = Bitmap.createBitmap(
                qrCode.getWidth(),
                qrCode.getHeight(),
                qrCode.getConfig());
        Canvas canvas= new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        canvas.drawBitmap(qrCode, new Matrix(), null);

        Bitmap resizeLogo = Bitmap.createScaledBitmap(
                logo,
                canvasWidth / 5,
                canvasHeight / 5,
                true);
        int centreX = (canvasWidth - resizeLogo.getWidth()) / 2;
        int centreY = (canvasHeight - resizeLogo.getHeight()) / 2;
        canvas.drawBitmap(resizeLogo, centreX, centreY, null);
        return combined;
    }
    //Save QR-code
    private boolean saveImage(Bitmap bitmap) throws IOException{
        boolean saved;
        OutputStream fileOutputStream = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,
                    getIntent().getStringExtra("KEY_FNAME") + date);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "QR");
            Uri imageUri =
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues);
            try {
                fileOutputStream = getContentResolver().openOutputStream(imageUri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }else {
            imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "QR";
            File file = new File(imagesDir);

            if (!file.exists())
                file.mkdir();

            File image = new File(imagesDir, getIntent().getStringExtra("KEY_FNAME")
                    + date + ".png");
            try {
                fileOutputStream = new FileOutputStream(image);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        assert fileOutputStream != null;
        fileOutputStream.flush();
        fileOutputStream.close();
        return saved;
    }
    //Add to Database
    private void addToDatabase(Map<String, Object> user, Map<String, Object> pass){
        String TAG = "Write Data Activity";

        db.collection("user")
                .add(user).addOnSuccessListener(documentReference
                -> {
            Log.d(TAG, "DocumentSnapshot written with ID: "
                    + documentReference.getId());
            db.collection("user").document(documentReference.getId())
                    .collection("pass").add(pass)
                    .addOnSuccessListener(documentReference1 ->
                            Log.d(TAG, "DocumentSnapshot written with ID: "
                                    + documentReference1.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document!", e));
        }).addOnFailureListener(e -> Log.w(TAG, "Error adding document!", e));
    }
    //Check database user
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
                        Log.d(TAG, "signInWithEmail: success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    }else{
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}