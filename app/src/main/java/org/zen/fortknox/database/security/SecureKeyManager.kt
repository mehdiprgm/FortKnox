package org.zen.fortknox.database.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import android.util.Base64
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import org.zen.fortknox.tools.preferencesName

object SecureKeyManager {

    // We keep this to ensure the key is created or retrieved from Keystore
    // It returns the alias String, which is automatically handled by the Builder below.
    private val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

//    private const val PREFS_FILE_NAME = "keyPreferences"
    private const val DB_KEY_PREF = "db_encryption_key"

    private fun generateDbKey(): String {
        val bytes = ByteArray(32) // 32 bytes for 256-bit encryption
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun getDbKey(context: Context): ByteArray {

        // --- THE FIX: Rely on MasterKeys.getOrCreate() and MasterKey.Builder's behavior ---
        // 1. Create the MasterKey object using the recommended builder pattern.
        // We pass the context and the KEY_ALIAS as the second parameter.
        val masterKey: MasterKey = MasterKey.Builder(context, masterKeyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        // --- END FIX ---

        // 2. Pass the MasterKey object to the EncryptedSharedPreferences.create function
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            preferencesName,
            masterKey, // Passing the MasterKey object
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var dbKeyString = sharedPrefs.getString(DB_KEY_PREF, null)
        if (dbKeyString == null) {
            dbKeyString = generateDbKey()
            sharedPrefs.edit { putString(DB_KEY_PREF, dbKeyString) }
        }

        return Base64.decode(dbKeyString, Base64.NO_WRAP)
    }
}