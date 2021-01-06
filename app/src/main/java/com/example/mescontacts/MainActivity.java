package com.example.mescontacts;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ContactsDbAdapter mDbHelper;
    private ListView ListContact;
    private TabLayout tabContact;
    private String page;
    private static final int CAMERA_PERMISSION_CODE=101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

            //Instantiation des attributs
        Instantiate();
        fillData(page);

            //Clique sur le bouton amenant à l'ajout d'un contact
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                intent.putExtra("texte", "SimpleAdd");
                startActivity(intent);
            }
         });

            //Clique sur un contact de la list
        ListContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int  position, long id) {
                Intent intent = new Intent(MainActivity.this, DisplayContactActivity.class);
                intent.putExtra("id_contact", id);
                startActivity(intent);
            }
        });

            //clique pour voir tous les contacts ou seul les favoris
        tabContact.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
           @Override
           public void onTabSelected(TabLayout.Tab tab) {
               if(tab.getText().toString().matches(getString(R.string.display_favoris))){
                   page = "favoris";
                   fillData( page);
               }else if(tab.getText().toString().matches(getString(R.string.display_all))){
                   page = "all_contact";
                   fillData(page);
               }
           }

           @Override
           public void onTabUnselected(TabLayout.Tab tab) {

           }

           @Override
           public void onTabReselected(TabLayout.Tab tab) {

           }
       });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData(page);
    }

    /**
     * Instancier les attributs
     */
    public void Instantiate(){
        mDbHelper = new ContactsDbAdapter(this);
        mDbHelper.open();
        ListContact = findViewById(R.id.ListView);
        page = "all_contact";
        tabContact = findViewById(R.id.tabContacts);
        registerForContextMenu(ListContact);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.btnReadQr:
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission(Manifest.permission.CAMERA)){
                        openScanner();
                    }
                    else{
                        requestPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
                    }
                }
                else{
                    openScanner();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

         //Menu contextuel
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor SelectedTaskCursor = (Cursor) ListContact.getItemAtPosition(info.position);
       final long id_contact = SelectedTaskCursor.getLong(SelectedTaskCursor.getColumnIndex("_id"));
       switch (item.getItemId()) {
            case R.id.btnDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.message_alert)
                        .setTitle(R.string.title_alert);
                builder.setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mDbHelper.deleteContacts(id_contact);
                        fillData("all_contact");
                    }
                });
                builder.setNegativeButton(R.string.cancel_alert, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.btnQR:
                Intent intent = new Intent(MainActivity.this, DisplayQrActivity.class);
                intent.putExtra("id_contact", id_contact);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void fillData(String action) {
        // Get all of the notes from the database and create the item list
        Cursor c = null;
        if (action.matches("favoris")) {
            c = mDbHelper.fetchAllfavoriteContacts();
        } else {
            c = mDbHelper.fetchAllContacts();
        }
        startManagingCursor(c);

        String[] from = new String[] {ContactsDbAdapter.KEY_NOM,ContactsDbAdapter.KEY_PRENOM};
        int[] to = new int[] { R.id.text1,R.id.text2};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter contacts =
                new SimpleCursorAdapter(this, R.layout.contacts_row, c, from, to,0);

        ListContact.setAdapter(contacts);
    }

    /**
     * Methode pour ouvrir le scanner
     */
    private void openScanner() {
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if(result.getContents()!=null){
                String Qr_texte;
                Qr_texte = result.getContents();
                if (Qr_texte.substring(0,9).matches("Baba23'%%")){
                    Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                    intent.putExtra("texte", Qr_texte);
                    startActivity(intent);
                }else {
                    Toast.makeText(this, getString(R.string.alert_error_read_Qr), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Verifier que la permission de la camera est autorisé
     * @param permission
     * @return true or false
     */
    private boolean checkPermission(String permission){
        int result= ContextCompat.checkSelfPermission(MainActivity.this,permission);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Demander la permission pour les version d'android > 6
     * @param permision
     * @param code
     */
    private void requestPermission(String permision,int code){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permision)){

        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permision},code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openScanner();
                }
        }
    }

}
