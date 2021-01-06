package com.example.mescontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class DisplayQrActivity extends AppCompatActivity {
    private ContactsDbAdapter mDbHelper;
    private ImageView barcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr);

        Intent intent = getIntent();
        final long id_contact = intent.getLongExtra("id_contact",0);
        barcode = findViewById(R.id.qrView);
        GenerateQR(id_contact);
    }

    /**
     * Generer un code QR à partir des informations recuperer d'un contact grâce à son identifiant
     * @param id
     */
    private void GenerateQR(long id) {
        // Get all of the notes from the database and create the item list
        mDbHelper = new ContactsDbAdapter(this);
        mDbHelper.open();
        Cursor c = mDbHelper.fetchContact(id);
        startManagingCursor(c);
        c.moveToFirst();
        String Nom = "",Prenom = "",Tel = "",Email = "",Adresse = "",favoris ="";
        Nom=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_NOM));
        Prenom=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_PRENOM));
       Tel=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_TEL));
       Email=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_EMAIL));
       Adresse=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_ADRESSE));
       favoris=c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_FAVORY));
        //Recuperer les dimentions de l'écran
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ScreenHeight = metrics.heightPixels;
        float ScreenWidht = metrics.widthPixels;
        float Screen = Math.min(ScreenHeight, ScreenHeight);

        String data_in_code="Baba23"+"'%%"+Nom+"'%%"+Prenom+"'%%"+Tel+"'%%"+Email+"'%%"+Adresse+"'%%"+favoris;
        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(data_in_code, BarcodeFormat.QR_CODE,(int)(Screen*0.5),(int)(Screen*0.5));
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
            barcode.setImageBitmap(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
