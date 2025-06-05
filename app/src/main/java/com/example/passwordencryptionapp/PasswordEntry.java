package com.example.passwordencryptionapp;

public class PasswordEntry {
    private int id;
    private String serviceName;
    private String username;
    private String password;
    private boolean isEncrypted;

    public PasswordEntry(int id, String serviceName, String username, String encryptedPassword) {
        this.id = id;
        this.serviceName = serviceName;
        this.username = username;
        this.password = encryptedPassword;
        this.isEncrypted = true;
    }

    public int getId() { return id; }
    public void setId(Integer id){ this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEncrypted() { return isEncrypted; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }

}
