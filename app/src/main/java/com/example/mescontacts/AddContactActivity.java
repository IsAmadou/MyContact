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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;


public class AddContactActivity extends AppCompatActivity {

    private ImageView ImgProfil;
    private ScrollView ImgContainer, AttributContainer;
    private EditText Nom,Prenom,Tel,Email,Adresse;
    private int int_fav=0;
    private Switch favoris;
    private ContactsDbAdapter mDbHelper;
    private Button btn;
    private String Img_txt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

            //Instancier l'attribut
       InstantiateAttribut();

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

            //Recuperation du qr decripté et remplissage des champs
        Intent intent = getIntent();
        final String qr_texte = intent.getStringExtra("texte");
        if (!(qr_texte.matches("SimpleAdd"))){
            displayQrInfo(qr_texte);
        }
    }

    /**
     * Instanciation les attributs de la classe
     */
    public void InstantiateAttribut(){
        ImgProfil =  findViewById(R.id.Img_profil);
        Bitmap defaultImg = BitmapFactory.decodeResource(getResources(), R.drawable.profil);
        defaultImg=ResizeImg(defaultImg, 0.4f);
        Img_txt=convertBitmapToString(defaultImg);
        ImgProfil.setImageBitmap(defaultImg);
        AttributContainer = findViewById(R.id.AttributContainer);
        ImgContainer = findViewById(R.id.ImgContainer);
        Nom = findViewById(R.id.nom);
        Prenom = findViewById(R.id.prenom);
        Tel= findViewById(R.id.tel);
        Email =findViewById(R.id.email);
        Adresse = findViewById(R.id.adresse);
        favoris = findViewById(R.id.favoris);
        mDbHelper = new ContactsDbAdapter(this);
        mDbHelper.open();
         btn = findViewById(R.id.btnAjout);

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
     * Methode Permettant l'ajout d'un contact
     * @param view
     */
    public void addContact (View view){
        if(Nom.getText().toString().trim().matches("") || Tel.getText().toString().trim().matches("")){
            Toast.makeText(this, getString(R.string.alert_contraint_contact), Toast.LENGTH_SHORT).show();
        }else{
            long add = mDbHelper.createContacts(Nom.getText().toString(), Prenom.getText().toString(), Tel.getText().toString(),
                    Email.getText().toString(), Adresse.getText().toString(), int_fav,Img_txt);
            if(add!=-1){
                Toast.makeText(this,getString(R.string.alert_add_contact) , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Methode permettant de remplir le contenu des champs pour l'ajout d'un contact
     * avec les informations décriptées du qr_code
     * @param qr_texte
     */
    public void displayQrInfo(String qr_texte){
        String Qr_part[] = qr_texte.split("'%¨¨%");
        Nom.setText(Qr_part[1]);
        Prenom.setText(Qr_part[2]);
        Tel.setText(Qr_part[3]);
        Email.setText(Qr_part[4]);
        Adresse.setText(Qr_part[5]);
        if(Qr_part[6].matches("0")){
            favoris.setChecked(false);
        }else if(Qr_part[6].matches("1")){
            favoris.setChecked(true);

        }
    }
}
