package com.example.passwordencryptionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import static com.example.passwordencryptionapp.SecurityUtils.hashPin;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main extends AppCompatActivity {

    private EditText pinEditText;
    private Button loginButton;

    /**
     * This method is called when the activity is first created.
     * It checks if a PIN is already set. If a PIN is set, it displays the login screen.
     * Otherwise, it redirects the user to the PIN activation screen to create a new PIN.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isPinSet()) {
            setContentView(R.layout.activity_main);
            pinEditText = findViewById(R.id.pinEditText);
            loginButton = findViewById(R.id.loginButton);
            setupPinLogin();
        } else {
            Intent intent = new Intent(Main.this, PinActivation.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * This method checks if the user has already set a PIN.
     * It accesses the encrypted shared preferences to see if a PIN value exists.
     * If an error occurs during this process, it displays an error message.
     * @return true if a PIN is set, false otherwise.
     */
    private boolean isPinSet() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return encryptedPrefs.contains("user_pin_hash");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error checking PIN status", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * This method sets up the login functionality.
     * When the login button is clicked, it checks if the entered PIN matches the stored PIN.
     * If the PIN is correct, it navigates to the Vault activity. Otherwise, it displays an error message.
     */
    private void setupPinLogin() {
        loginButton.setOnClickListener(view -> {
            String enteredPin = pinEditText.getText().toString();
            try {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                        "secure_prefs",
                        masterKeyAlias,
                        this,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                String storedSalt = encryptedPrefs.getString("user_salt", "");
                String storedHash = encryptedPrefs.getString("user_pin_hash", "");

                String enteredHash = hashPin(enteredPin, storedSalt);

                if (enteredHash.equals(storedHash)) {
                    navigateToVault();
                } else {
                    Toast.makeText(Main.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
                Toast.makeText(Main.this, "Error accessing PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This method navigates the user to the Vault activity.
     * The Vault activity is where the user can view and manage their stored passwords.
     */
    private void navigateToVault() {
        Intent intent = new Intent(Main.this, Vault.class);
        startActivity(intent);
        finish();
    }
}