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

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main extends AppCompatActivity {

    private EditText pinEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPinSet()) {
            // Set up the layout for PIN entry
            setContentView(R.layout.activity_main);
            pinEditText = findViewById(R.id.pinEditText);
            loginButton = findViewById(R.id.loginButton);

            setupPinLogin(); // Initialize PIN login
        } else {
            // Redirect to PIN setup
            Intent intent = new Intent(Main.this, PinActivation.class);
            startActivity(intent);
            finish();
        }
    }

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

            return encryptedPrefs.contains("user_pin");

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error checking PIN status", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

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

                String storedPin = encryptedPrefs.getString("user_pin", "");

                if (enteredPin.equals(storedPin)) {
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

    private void navigateToVault() {
        Intent intent = new Intent(Main.this, Vault.class);
        startActivity(intent);
        finish();
    }
}
