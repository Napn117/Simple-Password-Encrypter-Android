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

public class PinActivation extends AppCompatActivity {

    private EditText newPinEditText, confirmPinEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_activation);

        newPinEditText = findViewById(R.id.newPinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        Button savePinButton = findViewById(R.id.savePinButton);

        savePinButton.setOnClickListener(view -> savePin());
    }

    private void savePin() {
        String newPin = newPinEditText.getText().toString();
        String confirmPin = confirmPinEditText.getText().toString();

        if (newPin.isEmpty() || confirmPin.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            encryptedPrefs.edit().putString("user_pin", newPin).apply();

            Toast.makeText(this, "PIN saved successfully", Toast.LENGTH_SHORT).show();

            // Navigate to the MainActivity (or Vault directly if you want)
            Intent intent = new Intent(PinActivation.this, Main.class);
            startActivity(intent);
            finish();

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PIN", Toast.LENGTH_LONG).show();
        }
    }
}
