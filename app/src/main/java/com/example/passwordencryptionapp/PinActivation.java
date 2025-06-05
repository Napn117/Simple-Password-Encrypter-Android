package com.example.passwordencryptionapp;

import static com.example.passwordencryptionapp.SecurityUtils.generateSalt;
import static com.example.passwordencryptionapp.SecurityUtils.hashPin;

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

    /**
     * This method is called when the activity is first created.
     * It sets up the screen for activating a new PIN by initializing the input fields and save button.
     * When the save button is clicked, it triggers the process of saving the new PIN.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_activation);
        newPinEditText = findViewById(R.id.newPinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        Button savePinButton = findViewById(R.id.savePinButton);
        savePinButton.setOnClickListener(view -> savePin());
    }

    /**
     * This method saves the new PIN entered by the user.
     * It checks that both the new PIN and confirm PIN fields are filled and that they match.
     * If they match, it saves the PIN to encrypted shared preferences.
     * If an error occurs during saving, an error message is displayed.
     */
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

            String salt = generateSalt(); // new
            String hashedPin = hashPin(newPin, salt); // new

            encryptedPrefs.edit()
                    .putString("user_salt", salt)
                    .putString("user_pin_hash", hashedPin)
                    .apply();

            Toast.makeText(this, "PIN saved securely", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Main.class));
            finish();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PIN", Toast.LENGTH_LONG).show();
        }
    }

}
