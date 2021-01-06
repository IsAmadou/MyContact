/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.mescontacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ContactsDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_NOM = "nom";
    public static final String KEY_PRENOM = "prenom";
    public static final String KEY_TEL = "tel";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ADRESSE = "adresse";
    public static final String KEY_FAVORY = "favoris";
    public static final String KEY_IMAGE = "image";

    private static final String TAG = "ContactsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table contacts (_id integer primary key autoincrement, "
        + "nom text not null, prenom text not null, tel text not null, "
        +" email text not null, adresse text not null, favoris integer not null,"
        + "image text not null );";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ContactsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the contact database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ContactsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new contact using all attributs. If the contact is
     * successfully created return the new rowId for that contact, otherwise return
     * a -1 to indicate failure.
     * 
     * @param nom the nom of the contact
     * @param prenom the prenom of the contact
     * @param tel the tel of the contact
     * @param email the email of the contact
     * @param adresse the adresse of the contact
     * @param favoris the favoris of the contact
     * @param image the image of the contact in text format
     * @return rowId or -1 if failed
     */
    public long createContacts(String nom, String prenom, String tel, String email, String adresse, int favoris, String image) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NOM, nom);
        initialValues.put(KEY_PRENOM, prenom);
        initialValues.put(KEY_TEL, tel);
        initialValues.put(KEY_EMAIL,email);
        initialValues.put(KEY_ADRESSE, adresse);
        initialValues.put(KEY_FAVORY, favoris);
        initialValues.put(KEY_IMAGE, image);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the contact with the given rowId
     * 
     * @param rowId id of contact to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteContacts(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all contacts in the database
     * 
     * @return Cursor over all contacts
     */
    public Cursor fetchAllContacts() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NOM, KEY_PRENOM, KEY_TEL, KEY_EMAIL,
           KEY_ADRESSE, KEY_FAVORY, KEY_IMAGE}, null, null, null, null, KEY_NOM+" COLLATE NOCASE");
    }

    /**
     * Return a Cursor over the list of all favorite contacts in the database
     *
     * @return Cursor over all favorite contacts
     */
    public Cursor fetchAllfavoriteContacts() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NOM, KEY_PRENOM, KEY_TEL, KEY_EMAIL,
                KEY_ADRESSE, KEY_FAVORY,KEY_IMAGE}, KEY_FAVORY+"="+1, null, null, null, KEY_NOM+" COLLATE NOCASE");
    }


    /**
     * Return a Cursor positioned at the contact that matches the given rowId
     * 
     * @param rowId id of contact to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchContact(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NOM, KEY_PRENOM, KEY_TEL,
                 KEY_EMAIL, KEY_ADRESSE, KEY_FAVORY,KEY_IMAGE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the contact using the details provided. The contact to be updated is
     * specified using the rowId, and it is altered to use the parameters
     * values passed in
     * 
     * @param rowId id of contact to update
     * @param nom the nom of the contact
     * @param prenom the prenom of the contact
     * @param tel the tel of the contact
     * @param email the email of the contact
     * @param adresse the adresse of the contact
     * @param favoris the favoris of the contact
     * @param image the image of the contact in text format
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateContacts(long rowId, String nom, String prenom, String tel, String email, String adresse, int favoris, String image) {
        ContentValues args = new ContentValues();
        args.put(KEY_NOM, nom);
        args.put(KEY_PRENOM, prenom);
        args.put(KEY_TEL, tel);
        args.put(KEY_EMAIL,email);
        args.put(KEY_ADRESSE, adresse);
        args.put(KEY_FAVORY, favoris);
        args.put(KEY_IMAGE,image);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
