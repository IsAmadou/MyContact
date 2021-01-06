package com.example.mescontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;


public class DisplayContactActivity extends AppCompatActivity {
    private ImageView ImgProfil;
    private ScrollView ImgContainer, AttributContainer;
    private EditText Nom,Prenom,Tel,Email,Adresse;
    private Switch favoris;
    private ContactsDbAdapter mDbHelper;
    private FloatingActionButton btnMap,btnEmail,btnSms,btnCall,btnEdit;
    private long id_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact);

            // Instancier les attributs
        InstantiateAttribut();

             // Get the Intent that started this activity and extract the string
       Intent intent = getIntent();
       id_contact = intent.getLongExtra("id_contact",0);
            // Afficher les details du contacts
        DisplayContact(id_contact);


            //Clique sur le boutton de la recherche sur une Map
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapLocate();
            }
        });
            //Clique sur le boutton de l'appel
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Call();
            }
        });
            //Clique sur le boutton de l'envoi de sms
        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSms();
            }
        });
            //Clique sur le boutton d'envoie d'Email
        btnEmail.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 SendEmail();
              }
        });
            //Clique sur le boutton ramenant à la page de modification d'un contact
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Edit(id_contact);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayContact(id_contact);
    }

    /**
     * Methode pour l'nstanciation des attribut de la classe
     */
    public void InstantiateAttribut(){
        ImgProfil =  findViewById(R.id.Img_profil_display);
        Bitmap defaultImg = BitmapFactory.decodeResource(getResources(), R.drawable.profil);
        defaultImg=ResizeImg(defaultImg, 0.4f);
        ImgProfil.setImageBitmap(defaultImg);
        AttributContainer = findViewById(R.id.AttributContainer_display);
        ImgContainer = findViewById(R.id.ImgContainer_display);
        Nom = findViewById(R.id.nom_display);
        Prenom = findViewById(R.id.prenom_display);
        Tel= findViewById(R.id.tel_display);
        Email =findViewById(R.id.email_display);
        Adresse = findViewById(R.id.adresse_display);
        favoris = findViewById(R.id.favoris_display);
        favoris.setClickable(false);
        mDbHelper = new ContactsDbAdapter(this);
        mDbHelper.open();
        btnMap = findViewById(R.id.btnMaps);
        btnEmail = findViewById(R.id.btnEmail);
        btnSms = findViewById(R.id.btnSms);
        btnCall = findViewById(R.id.btnCall);
        btnEdit = findViewById(R.id.btnEdit);

        //Redimensionner les containers
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ScreenHeight = metrics.heightPixels;
        ImgContainer.setMinimumHeight((int)(ScreenHeight*0.4f));
        AttributContainer.setMinimumHeight((int)(ScreenHeight*0.4f));
    }

    /**
     * Redimentionner l'image par rapport à la taille de l'ecran
     * @param image
     * @param proportion
     * @return
     */
    public Bitmap ResizeImg( Bitmap image, float proportion){
        //Recuperer les dimention de l'ecran
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ScreenHeight = metrics.heightPixels*proportion;
        float ScreenWidht = metrics.widthPixels*proportion;
        //Recuperer les diffrences de dimentions entre l'ecran et l'image
        float ratioHeight = ScreenHeight/image.getHeight();
        float ratioWidht = ScreenWidht/image.getWidth();
        //Redimension de l'image par rapport à la plus petite de ces differences
        float ratio = Math.min(ratioHeight,ratioWidht);
        image = Bitmap.createScaledBitmap(image, (int)(metrics.widthPixels), (int)(image.getHeight()*ratio), true);

        return image;
    }

    /**
     * Convertir le texte en une image bitmap
     * @param texte
     * @return
     */
    public Bitmap convertStringToBitmap(String texte){
       Bitmap bitmap = null ;
       try {
           byte[] decodeString =  Base64.decode(texte, Base64.DEFAULT);
           bitmap = BitmapFactory.decodeByteArray(decodeString,0,decodeString.length);
       }catch (Exception e){
           Log.d("erreur", "Impossible d'affiche l'image");
       }
       return bitmap;
    }

    /**
     * Recuperer et afficher un contact à partir de son identifiant
     * @param id
     */
    private void DisplayContact(long id) {
        // Get all of the notes from the database and create the item list
        Cursor c = mDbHelper.fetchContact(id);
        startManagingCursor(c);


        String[] from = new String[] { ContactsDbAdapter.KEY_NOM, ContactsDbAdapter.KEY_PRENOM, ContactsDbAdapter.KEY_TEL,
                ContactsDbAdapter.KEY_EMAIL, ContactsDbAdapter.KEY_ADRESSE, ContactsDbAdapter.KEY_FAVORY};
        int[] to = new int[] { R.id.nom_display,  R.id.prenom_display, R.id.tel_display, R.id.email_display,
                R.id.adresse_display, R.id.favoris_display};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.activity_display_contact, c, from, to,0);
        contacts.bindView(Nom,this,c);
        contacts.bindView(Prenom,this,c);
        contacts.bindView(Tel,this,c);
        contacts.bindView(Email,this,c);
        contacts.bindView(Adresse,this,c);
        ImgProfil.setImageBitmap(convertStringToBitmap(c.getString(c.getColumnIndex(ContactsDbAdapter.KEY_IMAGE))));
        contacts.bindView(favoris,this,c);
        if(favoris.getText().toString().matches("0")){
            favoris.setChecked(false);
            favoris.setText("");
        }else if(favoris.getText().toString().matches("1")){
            favoris.setChecked(true);
            favoris.setText("");
        }
    }

    /**
     * Methode permettant de lancer l'envoi d'un mail à un contact à partir de
     * son adresse mail;
     */
    private void SendEmail(){
        if (Adresse.getText().toString().trim().matches("")) {
            Toast.makeText(this, getString(R.string.alert_send_mail_error), Toast.LENGTH_SHORT).show();
        } else {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // The intent does not have a URI, so declare the "text/plain" MIME type
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {Email.getText().toString()}); // recipients
            startActivity(emailIntent);
        }
    }

    /**
     * Methode permettant de lancer l'envoi d'un sms à un contact à partir de
     * son numéro de téléphone;
     */
    private void SendSms(){
        Uri number = Uri.parse("smsto:"+Tel.getText().toString());
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO,number);
        startActivity(smsIntent);
    }

    /**
     * Methode permettant de lancer la localisation d'un contact sur une map à partir
     * de son adresse postale;
     */
    private void MapLocate() {
        if (Adresse.getText().toString().trim().matches("")) {
            Toast.makeText(this, getString(R.string.alert_locate_error), Toast.LENGTH_SHORT).show();
        } else {
            Uri location = Uri.parse("geo:0,0?q=" + Adresse.getText().toString());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
            startActivity(mapIntent);
        }
    }

    /**
     * Methode permettant de lancer un appel à partir du numero de telephone du
     * contact sélectionner
     */
    private void Call(){
        Uri number = Uri.parse("tel:"+Tel.getText().toString());
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    /**
     * Methode permettant de lancer la modification d'un contact
     * @param id_contact
     */
    private void Edit(long id_contact){
        Intent intent = new Intent( DisplayContactActivity.this, UpdateContactActivity.class);
        intent.putExtra("id_contact", id_contact);
        startActivity(intent);
    }
}
