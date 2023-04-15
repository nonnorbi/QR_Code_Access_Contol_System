package com.nonnorbi.qrcs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.contentpager.content.Query;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class Mail extends AppCompatActivity {

    static final String sEmail      = "kis.norbert.g.95@gmail.com";
    static final String sPassword   = "xxcgwpfudnhppbwy";

    String fName;
    String lName;
    String type;
    String eMail;
    String fileName;
    String subject;
    String bodyText;
    String qrCodePath;
    String expDate;

    int price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        fileName   = getIntent().getStringExtra("KEY_FILENAME");
        eMail      = getIntent().getStringExtra("KEY_EMAIL");
        fName      = getIntent().getStringExtra("KEY_FNAME");
        lName      = getIntent().getStringExtra("KEY_LNAME");
        type       = getIntent().getStringExtra("KEY_TYPE");
        qrCodePath = getIntent().getStringExtra("KEY_ABSPATH");
        price      = getIntent().getIntExtra("KEY_PRICE", 0);
        expDate    = getIntent().getStringExtra("KEY_EXPDATE");
        subject  = lName + " " + fName + " Gym Pass";
        bodyText = "Hello " + fName + "!\n\n"
                   + "Welcome to the gym.\n\nYour pass: "
                   + type + ", Price: " + price + "HUF"
                   + "\nExpiration date: " + expDate
                   + "\nGood luck with your development.\nHave a good workout!";


        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.user", sEmail);
        properties.setProperty("mail.smtp.password", sPassword);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sEmail, sPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(eMail));
            message.setSubject(subject);

            Multipart emailContent = new MimeMultipart();
            //Text body part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(bodyText);
            //QRCode attach body part
            MimeBodyPart qrCodeBodyPart = new MimeBodyPart();
            qrCodeBodyPart.attachFile(qrCodePath);

            emailContent.addBodyPart(textBodyPart);
            emailContent.addBodyPart(qrCodeBodyPart);
            message.setContent(emailContent);

            new sendEmail().execute(message);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }

        startActivity(new Intent(getApplicationContext(), Login_Registration.class));
    }

    private class sendEmail extends AsyncTask<Message, String, String>{

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(Mail.this, "Please Wait",
                    "Sending Mail...", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send( messages[0] );
                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s.equals("Success")){
                AlertDialog.Builder builder = new AlertDialog.Builder(Mail.this);
                builder.setCancelable(false);
                builder.setTitle(Html.fromHtml("<font color = '#509324'>Success</font>"));
                builder.setMessage("Mail send successfully");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }else {
                Toast.makeText(getApplicationContext(), "Something went wrong",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}