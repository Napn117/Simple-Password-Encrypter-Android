package com.example.passwordencryptionapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "passwords.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "password_entries";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SERVICE_NAME = "service_name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private final Encryption encryption;

    /**
     * Constructor for the Database helper class.
     * Initializes the database and the encryption helper for managing encrypted password entries.
     * @param context The application context.
     */
    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryption = new Encryption(context);
    }

    /**
     * This method is called when the database is first created.
     * It creates the password entries table with columns for ID, service name, username, and password.
     * @param db The database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SERVICE_NAME + " TEXT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + "salt TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    /**
     * This method is called when the database version is upgraded.
     * It drops the existing password entries table and creates a new one.
     * @param db The database instance.
     * @param oldVersion The old version number of the database.
     * @param newVersion The new version number of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * This method adds a new password entry to the database.
     * It encrypts the password before saving and returns the ID of the newly created row.
     * @param entry The password entry to add.
     * @return The ID of the new row or -1 if an error occurs.
     */
    public long addPasswordEntry(PasswordEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_SERVICE_NAME, entry.getServiceName());
            values.put(COLUMN_USERNAME, entry.getUsername());

            String encryptedPassword = encryption.encrypt(entry.getPassword());
            entry.setEncrypted(true);
            values.put(COLUMN_PASSWORD, encryptedPassword);
            long newRowId = db.insert(TABLE_NAME, null, values);
            db.close();
            return newRowId;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * This method retrieves all password entries from the database.
     * It creates a list of PasswordEntry objects, initializing each one as encrypted.
     * @return A list of all password entries in the database.
     */
    public List<PasswordEntry> getAllPasswordEntries() {
        List<PasswordEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String serviceName = cursor.getString(cursor.getColumnIndex(COLUMN_SERVICE_NAME));
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                @SuppressLint("Range") String encryptedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                PasswordEntry entry = new PasswordEntry(id, serviceName, username, encryptedPassword);
                entry.setEncrypted(true);
                Log.d("EncryptionDebug", "Loaded entry with encrypted password: " + encryptedPassword + ", isEncrypted: " + entry.isEncrypted());

                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return entries;
    }

    /**
     * This method updates an existing password entry in the database.
     * If the password is not encrypted, it encrypts it before saving.
     * @param entry The password entry to update.
     */
    public void updatePasswordEntry(PasswordEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_SERVICE_NAME, entry.getServiceName());
            values.put(COLUMN_USERNAME, entry.getUsername());
            String passwordToSave = entry.getPassword();
            if (!entry.isEncrypted()) {
                passwordToSave = encryption.encrypt(entry.getPassword());
                entry.setEncrypted(true);
                Log.d("EncryptionDebug", "Re-encrypting password on update: " + passwordToSave);
            }
            values.put(COLUMN_PASSWORD, passwordToSave);
            db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(entry.getId())});
            db.close();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method deletes a password entry from the database.
     * @param entryId The ID of the password entry to delete.
     */
    public void deletePasswordEntry(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(entryId)});
        db.close();
    }
}
