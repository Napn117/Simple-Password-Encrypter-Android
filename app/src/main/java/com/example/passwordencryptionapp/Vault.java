package com.example.passwordencryptionapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Vault extends AppCompatActivity implements EntryAdapter.OnEditClickListener {

    private EntryAdapter entryAdapter;
    private List<PasswordEntry> passwordEntries;
    private Database databaseHelper;
    private Encryption encryptionHelper;
    private boolean isDecryptedView = false; // Track if the decrypted view is shown
    private Button toggleDecryptionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        // Initialize DatabaseHelper and EncryptionHelper
        databaseHelper = new Database(this);
        encryptionHelper = new Encryption(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton addPasswordButton = findViewById(R.id.addPasswordButton);

        // Initialize toggleDecryptionButton
        toggleDecryptionButton = findViewById(R.id.toggleDecryptionButton);

        // Load encrypted entries from the database
        passwordEntries = databaseHelper.getAllPasswordEntries();
        entryAdapter = new EntryAdapter(passwordEntries, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(entryAdapter);

        addPasswordButton.setOnClickListener(view -> showAddPasswordDialog());
        toggleDecryptionButton.setOnClickListener(view -> handleToggleDecryption());
    }


    @Override
    public void onEditClick(PasswordEntry entry) {
        showPinVerificationDialog(entry);
    }

    private void showPinVerificationDialog(PasswordEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN to Edit");

        View pinView = LayoutInflater.from(this).inflate(R.layout.dialog_input_pin, null);
        final EditText pinInput = pinView.findViewById(R.id.pinInput);

        builder.setView(pinView);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredPin = pinInput.getText().toString();
            if (isCorrectPin(enteredPin)) {
                showEditPasswordDialog(entry);  // Proceed to edit if PIN is correct
            } else {
                Toast.makeText(Vault.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void handleToggleDecryption() {
        if (!isDecryptedView) {
            // Prompt for PIN before showing decrypted passwords
            showPinVerificationForDecryption();
        } else {
            // Hide decrypted passwords without PIN
            reloadEncryptedData();
            toggleDecryptionButton.setText("Show Decrypted Passwords");
            isDecryptedView = false;
        }
    }


    private void showPinVerificationForDecryption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN to View Decrypted Passwords");

        View pinView = LayoutInflater.from(this).inflate(R.layout.dialog_input_pin, null);
        final EditText pinInput = pinView.findViewById(R.id.pinInput);

        builder.setView(pinView);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredPin = pinInput.getText().toString();
            if (isCorrectPin(enteredPin)) {
                showDecryptedPasswords();  // Show decrypted passwords if the PIN is correct
            } else {
                Toast.makeText(Vault.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private boolean isCorrectPin(String enteredPin) {
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
            return enteredPin.equals(storedPin);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error accessing PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showDecryptedPasswords() {
        try {
            for (PasswordEntry entry : passwordEntries) {
                // Decrypt each password
                String decryptedPassword = encryptionHelper.decrypt(entry.getPassword());
                entry.setPassword(decryptedPassword);
            }
            entryAdapter.notifyDataSetChanged();
            toggleDecryptionButton.setText("Hide Decrypted Passwords");
            isDecryptedView = true;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to decrypt passwords", Toast.LENGTH_SHORT).show();
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private void reloadEncryptedData() {
        // Reset each entry to its encrypted value
        for (PasswordEntry entry : passwordEntries) {
            entry.setPassword(entry.getOriginalEncryptedPassword());
        }
        entryAdapter.notifyDataSetChanged();
        isDecryptedView = false;
        toggleDecryptionButton.setText("Show Decrypted Passwords");
    }


    @SuppressLint("NotifyDataSetChanged")
    private void showAddPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Password");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_password, null);
        final EditText inputService = viewInflated.findViewById(R.id.inputService);
        final EditText inputUsername = viewInflated.findViewById(R.id.inputUsername);
        final EditText inputPassword = viewInflated.findViewById(R.id.inputPassword);

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String service = inputService.getText().toString();
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            if (!service.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                try {
                    // Encrypt the password before saving it
                    String encryptedPassword = encryptionHelper.encrypt(password);
                    PasswordEntry newEntry = new PasswordEntry(0, service, username, encryptedPassword);
                    newEntry.setOriginalEncryptedPassword(encryptedPassword); // Set the original encrypted password

                    // Save to database and get the assigned ID
                    long newId = databaseHelper.addPasswordEntry(newEntry);
                    newEntry.setId((int) newId); // Set the generated ID for future reference

                    // Add to the list and update UI
                    passwordEntries.add(newEntry);
                    entryAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to encrypt password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Vault.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }



    @SuppressLint("NotifyDataSetChanged")
    private void showEditPasswordDialog(PasswordEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Password");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_password, null);
        final EditText inputService = viewInflated.findViewById(R.id.inputService);
        final EditText inputUsername = viewInflated.findViewById(R.id.inputUsername);
        final EditText inputPassword = viewInflated.findViewById(R.id.inputPassword);

        // Prepopulate fields with existing values
        inputService.setText(entry.getServiceName());
        inputUsername.setText(entry.getUsername());
        inputPassword.setText(entry.getPassword());  // Decrypted password should be shown here

        builder.setView(viewInflated);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String serviceName = inputService.getText().toString();
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            try {
                // Encrypt the password before saving
                String encryptedPassword = encryptionHelper.encrypt(password);

                // Update the entry fields
                entry.setServiceName(serviceName);
                entry.setUsername(username);
                entry.setPassword(encryptedPassword);
                entry.setOriginalEncryptedPassword(encryptedPassword); // Update the original encrypted password

                // Save the updated entry to the database
                databaseHelper.updatePasswordEntry(entry);

                // Refresh display
                entryAdapter.notifyDataSetChanged();
                Toast.makeText(Vault.this, "Password updated", Toast.LENGTH_SHORT).show();
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to encrypt password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }



}
