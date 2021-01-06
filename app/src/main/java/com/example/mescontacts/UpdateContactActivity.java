package com.example.mescontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class UpdateContactActivity extends AppCompatActivity {
    private ImageView ImgProfil;
    private ScrollView ImgContainer, AttributContainer;
    private EditText Nom,Prenom,Tel,Email,Adresse;
    private int int_fav=0;
    private Switch favoris;
    private ContactsDbAdapter mDbHelper;
    private Button btn, btnAnnuler;
    private String Img_txt = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

            //Instancier l'attribut
        InstantiateAttribut();

            // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        final long id_contact = intent.getLongExtra("id_contact",0);

            // Afficher les details du contacts
        DisplayContact(id_contact);

            //CLique pour enregistrer la modification
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateContact(v,id_contact);
            }
        });

            //Clique sur le champs de l'ajout de la photo de profil
        ImgProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto(v);
            }
        });

            //Action du switch des favoris
        favoris.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    int_fav=1;
                }else{
                    int_fav=0;
                }
            }

        });

            //Clique sur le bouton annuler
        btnAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

    }

    /**
     * Instanciation des attribut de la classe
     */
    public void InstantiateAttribut(){
        ImgProfil =  findViewById(R.id.Img_profil_update);
        Bitmap defaultImg = BitmapFactory.decodeResource(getResources(), R.drawable.profil);
        defaultImg=ResizeImg(defaultImg, 0.4f);
        Img_txt = convertBitmapToString(defaultImg);
        ImgProfil.setImageBitmap(defaultImg);
        AttributContainer = findViewById(R.id.AttributContainer_update);
        ImgContainer = findViewById(R.id.ImgContainer_update);
        Nom = findViewById(R.id.nom_update);
        Prenom = findViewById(R.id.prenom_update);
        Tel= findViewById(R.id.tel_update);
        Email =findViewById(R.id.email_update);
        Adresse = findViewById(R.id.adresse_update);
        favoris = findViewById(R.id.favoris_update);
        mDbHelper = new ContactsDbAdapter(this);
        mDbHelper.open();
        btn = findViewById(R.id.btnUpdate);
        btnAnnuler = findViewById(R.id.btnAnnulerUpdate);

        //Redimensionner les containers
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ScreenHeight = metrics.heightPixels;
        ImgContainer.setMinimumHeight((int)(ScreenHeight*0.4f));
        AttributContainer.setMinimumHeight((int)(ScreenHeight*0.4f));
    }

    /**
     * Methode assurant l'acces à la galerie
     * @param view
     */
    public void addPhoto(View view){
        Intent GalerieAcces = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(GalerieAcces,1);
    }

    /**
     * Resultat de l'acces a la galerie aprés la selection d'une image
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            Uri ImgUri = data.getData();
            //Chemin des images sur mon telephone
            String [] FilePath = {MediaStore.Images.Media.DATA};
            //Cursur pour recuper le chemin de l'image selectionné
            Cursor cursor = this.getContentResolver().query(ImgUri,FilePath,null, null, null);
            cursor.moveToFirst();
            int ColumnIndex = cursor.getColumnIndex(FilePath[0]);
            String ImgPath = cursor.getString(ColumnIndex);
            cursor.close();
            //Recupere l'image à partir du chemin
            Bitmap image;
            image = BitmapFactory.decodeFile(ImgPath);
            image = ResizeImg(image, 0.4f);
            Img_txt = convertBitmapToString(image);
            ImgProfil.setImageBitmap(image);
        }
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
     * Convertir une image bitmap en texte
     * @param bitmap
     * @return
     */
    public String convertBitmapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes,0);
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
        int[] to = new int[] { R.id.nom_update,  R.id.prenom_update, R.id.tel_update, R.id.email_update,
                R.id.adresse_update, R.id.favoris_update};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.activity_update_contact, c, from, to,0);
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
     * Methode Permettant la modification d'un contact
     * @param view
     */
    public void UpdateContact (View view,long id_contact){
        if(Nom.getText().toString().trim().matches("") || Tel.getText().toString().trim().matches("")){
            Toast.makeText(this, getString(R.string.alert_contraint_contact), Toast.LENGTH_SHORT).show();
        }else{
            boolean add = mDbHelper.updateContacts(id_contact,Nom.getText().toString(), Prenom.getText().toString(), Tel.getText().toString(),
                    Email.getText().toString(), Adresse.getText().toString(), int_fav,Img_txt);
            if(add==true){
                finish();
            }
        }
    }
}
