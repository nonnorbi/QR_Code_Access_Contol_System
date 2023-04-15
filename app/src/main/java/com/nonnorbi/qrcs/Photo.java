package com.nonnorbi.qrcs;

import static android.os.Environment.getExternalStoragePublicDirectory;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PackageManagerCompat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Photo extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    ImageView       imageView;
    Button          captureImage;
    EditText        name;

    String          currentPhotoPath;
    String          key_name;
   /* String          fName;
    String          lName;
    String          eMail;
    String          phone;
    String          ID;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageView    = findViewById(R.id.mimageView);
        captureImage = findViewById(R.id.captureImage);
        name         = findViewById(R.id.name);

        key_name = getIntent().getStringExtra("KEY_NAME");
        name.setText(key_name);

        //Open camera
        captureImage.setOnClickListener(v -> verifyPermission());
    }

    private void verifyPermission() {
        String[] permission = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permission[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permission[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permission[2]) == PackageManager.PERMISSION_GRANTED){
            dispatchTakePictureIntent();
        }else {
            ActivityCompat.requestPermissions(this, permission, CAMERA_PERM_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE){
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this,
                        "Camera Permission is Required to Use camera.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                File file = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile( file ));
                Log.d("TAG", "Absolute Url of Image is " + Uri.fromFile(file));

                Intent mediaScanIntent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);


                Intent intent = new Intent(this, QRcodeGenerate.class);

                intent.putExtra("KEY_PHOTO", currentPhotoPath);
                intent.putExtra("KEY_ID", getIntent().getStringExtra("KEY_ID"));
                intent.putExtra("KEY_FNAME", getIntent().getStringExtra("KEY_FNAME"));
                intent.putExtra("KEY_LNAME", getIntent().getStringExtra("KEY_LNAME"));
                intent.putExtra("KEY_EMAIL", getIntent().getStringExtra("KEY_EMAIL"));
                intent.putExtra("KEY_PHONE", getIntent().getStringExtra("KEY_PHONE"));
                intent.putExtra("KEY_GENDER", getIntent().getStringExtra("KEY_GENDER"));
                intent.putExtra("KEY_TYPE", getIntent().getStringExtra("KEY_TYPE"));

                startActivity(intent);
            }
        }
    }

    private File createImageFile() throws IOException{
      //Create an image file name
      String timeStamp = new SimpleDateFormat("yyMMdd_HHmm").format(new Date());
      String imageFileName = "PNG_" + name.getText();
      File storageDir =
              Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
      File image = File.createTempFile(
              imageFileName, //prefix
              ".png",  //suffix
              storageDir    //directory
      );
      //Save a file: path for use with ACTION_VIEW intents
      currentPhotoPath = image.getAbsolutePath();
      return image;
    };

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            //Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Continue only if the File was successfully created
            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}