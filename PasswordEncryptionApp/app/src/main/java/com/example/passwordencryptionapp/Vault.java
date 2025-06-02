package com.example.passwordencryptionapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private boolean isDecryptedView = false;
    private Button toggleDecryptionButton;

    /**
     * This method is called when the activity is first created.
     * It sets up the vault screen by initializing the database, encryption helpers, RecyclerView,
     * and buttons for adding and toggling the decryption view of password entries.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        databaseHelper = new Database(this);
        encryptionHelper = new Encryption(this);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton addPasswordButton = findViewById(R.id.addPasswordButton);
        toggleDecryptionButton = findViewById(R.id.toggleDecryptionButton);
        passwordEntries = databaseHelper.getAllPasswordEntries();
        entryAdapter = new EntryAdapter(passwordEntries, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(entryAdapter);
        addPasswordButton.setOnClickListener(view -> showAddPasswordDialog());
        toggleDecryptionButton.setOnClickListener(view -> handleToggleDecryption());
    }

    /**
     * This method is called when the edit button of a password entry is clicked.
     * It shows a dialog prompting the user to enter their PIN before allowing them to edit the entry.
     */
    @Override
    public void onEditClick(PasswordEntry entry) {
        showPinVerificationDialog(entry);
    }

    /**
     * This method shows a dialog for PIN verification before allowing the user to edit a password entry.
     * If the entered PIN is correct, it proceeds to the edit screen for the password entry.
     */
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

    /**
     * This method handles the toggle decryption button click.
     * It prompts the user to enter their PIN before showing decrypted passwords.
     * If the passwords are already decrypted, it hides them and reloads the encrypted data.
     */
    private void handleToggleDecryption() {
        if (!isDecryptedView) {
            showPinVerificationForDecryption();
        } else {
            reloadEncryptedData();
            toggleDecryptionButton.setText("Show Decrypted Passwords");
            isDecryptedView = false;
        }
    }

    /**
     * This method shows a dialog for PIN verification before decrypting the password entries.
     * If the entered PIN is correct, it proceeds to decrypt and display the passwords.
     */
    private void showPinVerificationForDecryption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN to View Decrypted Passwords");
        View pinView = LayoutInflater.from(this).inflate(R.layout.dialog_input_pin, null);
        final EditText pinInput = pinView.findViewById(R.id.pinInput);
        builder.setView(pinView);
        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredPin = pinInput.getText().toString();
            if (isCorrectPin(enteredPin)) {
                showDecryptedPasswords();
            } else {
                Toast.makeText(Vault.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * This method checks if the entered PIN matches the stored PIN in encrypted shared preferences.
     * If an error occurs during this process, an error message is displayed.
     * @param enteredPin The PIN entered by the user.
     * @return true if the entered PIN matches the stored PIN, false otherwise.
     */
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

    /**
     * This method decrypts all password entries and updates the RecyclerView to show the decrypted passwords.
     * It also updates the toggle button text to indicate that the passwords are currently decrypted.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void showDecryptedPasswords() {
        try {
            for (PasswordEntry entry : passwordEntries) {
                if (entry.isEncrypted()) {
                    String decryptedPassword = encryptionHelper.decrypt(entry.getPassword());
                    entry.setPassword(decryptedPassword);
                    entry.setEncrypted(false);
                    Log.d("EncryptionDebug", "Decrypting password for entry: " + entry.getServiceName());
                }
            }
            entryAdapter.notifyDataSetChanged();
            toggleDecryptionButton.setText("Hide Decrypted Passwords");
            isDecryptedView = true;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to decrypt passwords", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method re-encrypts all password entries and updates the RecyclerView to show the encrypted passwords.
     * It also updates the toggle button text to indicate that the passwords are currently encrypted.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void reloadEncryptedData() {
        try {
            for (PasswordEntry entry : passwordEntries) {
                if (!entry.isEncrypted()) {
                    String encryptedPassword = encryptionHelper.encrypt(entry.getPassword());
                    entry.setPassword(encryptedPassword);
                    entry.setEncrypted(true);
                    Log.d("EncryptionDebug", "Re-encrypting password for entry: " + entry.getServiceName());
                }
            }
            entryAdapter.notifyDataSetChanged();
            toggleDecryptionButton.setText("Show Decrypted Passwords");
            isDecryptedView = false;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to re-encrypt passwords", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method shows a dialog for adding a new password entry.
     * It collects the service name, username, and password from the user, encrypts the password,
     * and saves the new entry to the database.
     */
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
                    String encryptedPassword = encryptionHelper.encrypt(password);
                    Log.d("EncryptionDebug", "Encrypted new password: " + encryptedPassword);
                    PasswordEntry newEntry = new PasswordEntry(0, service, username, encryptedPassword);
                    long newId = databaseHelper.addPasswordEntry(newEntry);
                    newEntry.setId((int) newId);
                    newEntry.setEncrypted(true);
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

    /**
     * This method shows a dialog for editing an existing password entry.
     * It allows the user to update the service name, username, and password,
     * encrypts the updated password, and saves the changes to the database.
     * The user can also delete the password entry from this dialog.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void showEditPasswordDialog(PasswordEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Password");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_password, null);
        final EditText inputService = viewInflated.findViewById(R.id.inputService);
        final EditText inputUsername = viewInflated.findViewById(R.id.inputUsername);
        final EditText inputPassword = viewInflated.findViewById(R.id.inputPassword);
        inputService.setText(entry.getServiceName());
        inputUsername.setText(entry.getUsername());
        inputPassword.setText(entry.getPassword());
        builder.setView(viewInflated);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String serviceName = inputService.getText().toString();
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            try {
                String encryptedPassword = encryptionHelper.encrypt(password);
                entry.setServiceName(serviceName);
                entry.setUsername(username);
                entry.setPassword(encryptedPassword);
                entry.setEncrypted(true);
                databaseHelper.updatePasswordEntry(entry);
                entryAdapter.notifyDataSetChanged();
                Toast.makeText(Vault.this, "Password updated", Toast.LENGTH_SHORT).show();
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to encrypt password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Password")
                    .setMessage("Are you sure you want to permanently delete this password entry?")
                    .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                        databaseHelper.deletePasswordEntry(entry.getId());
                        passwordEntries.remove(entry);
                        entryAdapter.notifyDataSetChanged();
                        Toast.makeText(Vault.this, "Password deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        builder.show();
    }

}