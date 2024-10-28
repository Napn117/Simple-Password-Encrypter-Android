package com.example.passwordencryptionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALIAS = "encryption_key";

    private final Context context;

    public Encryption(Context context) {
        this.context = context;
    }

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
            // Generate a new key if one doesn't exist and save it securely
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            sharedPreferences.edit().putString(KEY_ALIAS, encodedKey).apply();
        }

        byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    public String encrypt(String data) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public String decrypt(String encryptedData) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
        return new String(decryptedBytes);
    }
}
