package com.thanoxxinfnity.notesapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("notes")
private val NOTES_KEY = stringPreferencesKey("notes_json")

class NotesRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    val notes: Flow<List<Note>> = context.dataStore.data.map { prefs ->
        val raw = prefs[NOTES_KEY]
        if (raw.isNullOrBlank()) emptyList()
        else runCatching { json.decodeFromString<List<Note>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun saveAll(notes: List<Note>) {
        context.dataStore.edit { prefs ->
            prefs[NOTES_KEY] = json.encodeToString(notes)
        }
    }
}
