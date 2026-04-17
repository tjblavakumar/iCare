package com.icare.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NicknameRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val nicknamesKey = stringPreferencesKey("contact_nicknames")

    val nicknames: Flow<Map<String, String>> = dataStore.data.map { prefs ->
        val json = prefs[nicknamesKey] ?: "{}"
        try {
            Json.decodeFromString<Map<String, String>>(json)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getNicknames(): Map<String, String> {
        return nicknames.first()
    }

    suspend fun setNickname(userId: String, nickname: String) {
        dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<Map<String, String>>(prefs[nicknamesKey] ?: "{}")
            } catch (e: Exception) {
                emptyMap()
            }
            val updated = current.toMutableMap().apply {
                if (nickname.isBlank()) {
                    remove(userId)
                } else {
                    put(userId, nickname.trim())
                }
            }
            prefs[nicknamesKey] = Json.encodeToString(updated)
        }
    }

    suspend fun removeNickname(userId: String) {
        setNickname(userId, "")
    }

    suspend fun getNickname(userId: String): String? {
        return getNicknames()[userId]
    }
}
