# 🔐 Simple Android Password Manager with Encryption

This is a secure, lightweight Android password manager app built in Java. 
It allows users to safely store, view, and manage passwords using robust AES-GCM encryption.

---

## 📱 Features

- ✅ **AES-GCM encryption** for secure password storage
- ✅ **PIN** on first launch
- ✅ **View, add, edit, and delete** encrypted password entries
- ✅ **Local storage only** — no cloud or internet access for maximum privacy

---

## 🔒 How Encryption Works

- Uses **AES-GCM (Advanced Encryption Standard - Galois/Counter Mode)**.
- A new **random IV** is generated for each encryption operation.
- Data is encrypted using a **256-bit AES key** (stored securely via Android Keystore).
- Encrypted data includes an **authentication tag** to ensure integrity.
- Biometric or PIN access is required to decrypt data.

---

## ▶️ Build & Run

-Clone or Download the repo
-Open inside Android Studio
-Build APK or click run
