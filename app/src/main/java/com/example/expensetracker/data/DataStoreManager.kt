package com.example.expensetracker.data


import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "expensePreference")



class ExpensePreferenceManager(private val context: Context) {

    private val ACCESS_TOKEN = stringPreferencesKey("plaid_access_token")
    private val SYNC_CURSOR = stringPreferencesKey("plaid_sync_cursor")
    private val USER_NAME = stringPreferencesKey("user_name")

    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

    val lastSyncTime = context.dataStore.data.map { it[LAST_SYNC_TIME] ?: 0L }

    suspend fun saveSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_TIME] = time }
    }


    val accessToken = context.dataStore.data.map {
        preferences -> preferences[ACCESS_TOKEN]
    }
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit{ preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }


    val syncCursor = context.dataStore.data.map { preferences ->
        preferences[SYNC_CURSOR] ?: ""
    }

    suspend fun saveSyncCursor(cursor: String) {
        context.dataStore.edit { it[SYNC_CURSOR] = cursor }
    }


    val username = context.dataStore.data.map {
        preferences -> preferences[USER_NAME]
    }
    suspend fun saveUsername(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }



}