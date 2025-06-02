package com.example.passwordencryptionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "encryption_key";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String ENCRYPTION_PREFIX = "[ENC]";

    private final Context context;

    /**
     * Constructor for the Encryption class.
     * Initializes the context which is used for accessing secure shared preferences.
     * @param context The application context.
     */
    public Encryption(Context context) {
        this.context = context;
    }

    /**
     * This method retrieves or generates the secret key used for encryption and decryption.
     * If the key does not already exist, it is generated and stored in encrypted shared preferences.
     * @return The secret key used for AES encryption and decryption.
     * @throws GeneralSecurityException If a security error occurs during key generation or retrieval.
     * @throws IOException If an I/O error occurs during key storage.
     */
    private Key getSecretKey() throws GeneralSecurityException, IOException {
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "encryption_prefs",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        String encodedKey = sharedPreferences.getString(KEY_ALIAS, null);
        if (encodedKey == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            sharedPreferences.edit().putString(KEY_ALIAS, encodedKey).apply();
        }
        byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * This method encrypts a given piece of data using AES-GCM.
     * It generates a new IV (initialization vector) for each encryption to ensure uniqueness.
     * The IV is prepended to the encrypted data to allow decryption.
     * @param data The plaintext data to be encrypted.
     * @return The encrypted data as a base64-encoded string, prefixed with an encryption identifier.
     * @throws GeneralSecurityException If an error occurs during the encryption process.
     * @throws IOException If an error occurs when retrieving the secret key.
     */
    public String encrypt(String data) throws GeneralSecurityException, IOException {
        if (data.startsWith(ENCRYPTION_PREFIX)) {
            return data;
        }
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), gcmParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
        return ENCRYPTION_PREFIX + Base64.encodeToString(combined, Base64.DEFAULT);
    }

    /**
     * This method decrypts a given encrypted string that was previously encrypted using AES-GCM.
     * It extracts the IV from the beginning of the encrypted data before proceeding with decryption.
     * @param encryptedData The encrypted data as a base64-encoded string, prefixed with an encryption identifier.
     * @return The original plaintext data.
     * @throws GeneralSecurityException If an error occurs during the decryption process.
     * @throws IOException If an error occurs when retrieving the secret key.
     */
    public String decrypt(String encryptedData) throws GeneralSecurityException, IOException {
        if (!encryptedData.startsWith(ENCRYPTION_PREFIX)) {
            return encryptedData;
        }
        encryptedData = encryptedData.substring(ENCRYPTION_PREFIX.length());
        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] iv = new byte[IV_LENGTH];
        byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
