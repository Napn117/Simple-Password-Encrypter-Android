package com.example.passwordencryptionapp;

public class PasswordEntry {
    private int id;
    private String serviceName;
    private String username;
    private String password;  // Holds the current view state (encrypted or decrypted)
    private String originalEncryptedPassword; // Always holds the original encrypted password

    // Constructor
    public PasswordEntry(int id, String serviceName, String username, String encryptedPassword) {
        this.id = id;
        this.serviceName = serviceName;
        this.username = username;
        this.originalEncryptedPassword = encryptedPassword;  // Set the original encrypted value initially
        this.password = encryptedPassword;  // Start with the encrypted password
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOriginalEncryptedPassword() { return originalEncryptedPassword; }
    public void setOriginalEncryptedPassword(String originalEncryptedPassword) {
        this.originalEncryptedPassword = originalEncryptedPassword;
    }
}
