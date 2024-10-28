package com.example.passwordencryptionapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryption = new Encryption(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SERVICE_NAME + " TEXT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addPasswordEntry(PasswordEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_SERVICE_NAME, entry.getServiceName());
            values.put(COLUMN_USERNAME, entry.getUsername());
            String encryptedPassword = encryption.encrypt(entry.getPassword());
            values.put(COLUMN_PASSWORD, encryptedPassword);

            db.insert(TABLE_NAME, null, values);
            db.close();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

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

                // Add new PasswordEntry with id
                entries.add(new PasswordEntry(id, serviceName, username, encryptedPassword));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return entries;
    }


    public void updatePasswordEntry(PasswordEntry entry) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_SERVICE_NAME, entry.getServiceName());
            values.put(COLUMN_USERNAME, entry.getUsername());

            // Encrypt the password before updating
            String encryptedPassword = encryption.encrypt(entry.getPassword());
            values.put(COLUMN_PASSWORD, encryptedPassword);

            // Update the entry based on its unique id
            db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(entry.getId())});
            db.close();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

}

