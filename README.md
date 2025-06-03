# Android Encrypted Password Vault App

## 🔐 Overview

This Android app allows users to securely store and manage passwords behind a user-defined PIN. It uses `EncryptedSharedPreferences` and AES-GCM encryption to protect sensitive data. Passwords are stored encrypted in a local database and can be decrypted temporarily after PIN verification.

---

## ✨ Features

- Set and verify a 4-digit PIN to access the vault
- Securely store, edit, and delete password entries
- Toggle between encrypted and decrypted password views (requires re-entering PIN)
- All data is encrypted using Android's security libraries (AES-GCM via `EncryptedSharedPreferences`)
- PIN verification required for sensitive operations (viewing or editing passwords)
- Local SQLite database integration for storing password entries

---

## 🔐 Security Design

- PIN is stored using `EncryptedSharedPreferences` with a MasterKey (`AES256_GCM_SPEC`)
- Password entries are encrypted individually using AES-GCM before being written to the database
- Users must re-authenticate (re-enter PIN) to:
  - View decrypted passwords
  - Edit a password entry
- No sensitive data is shown or editable without explicit user verification

---

## 💡 Potential Improvements

- 🔑 **Add a Salt** when hashing or encrypting the PIN  
  Currently, the PIN is stored and compared as-is. Adding a random salt would reduce the risk of precomputed attack vectors or reused values.

- 👆 **Biometric Unlock**  
  Enable fingerprint or face unlock for faster secure access.

- 🧹 **PIN Retry Limits / Lockout**  
  Prevent brute-force attempts by limiting PIN attempts or implementing a lockout timer.

- 📄 **Export/Import Vault**  
  Optionally allow users to backup or restore their encrypted vault entries (with caution).

- 📱 **Improved UI/UX**  
  Add password strength indicators, autofill integration, or dark mode.

---

## 🧪 Usage Flow

1. **First Launch**: User sets a PIN.
2. **Subsequent Launches**: PIN entry screen is shown for authentication.
3. **Vault Activity**:
   - View list of encrypted password entries
   - Add new entries using a form
   - Toggle decrypted/encrypted view with PIN
   - Edit or delete entries (requires PIN re-verification)

---

## 📌 Notes

- Designed to demonstrate secure Android app development using Jetpack Security
- Does **not use any third-party libraries** for encryption or password storage
- Tested on Android 11+ (API 30 and above)
- All sensitive operations are logged using `Log.d` for debugging only; remove before production use

---

# ⚙️ Build & Run Instructions

- Open the project in Android Studio
- Run the app on a physical device or emulator with Android 10+ (API 29 or higher)
- On first launch, set a secure 4-digit PIN
- After logging in, use the Vault screen to:
- Add encrypted password entries
- View decrypted passwords (requires PIN re-entry)
- Edit or delete existing entries securely
