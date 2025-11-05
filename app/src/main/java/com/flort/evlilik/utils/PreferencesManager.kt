package com.flort.evlilik.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@SuppressLint("StaticFieldLeak")
class PreferencesManager(private val context: Context) {
    private val TAG = "PreferencesManager"

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        val token = preferences[AUTH_TOKEN]    
        token
    }

    suspend fun saveAuthToken(token: String) {    
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }        
    }

    suspend fun clearAuthToken() {        
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
        }
 
    }

    suspend fun getAuthToken(): String? {
        return authToken.first()
    }

    suspend fun hasValidToken(): Boolean {
        val token = getAuthToken()
        return !token.isNullOrEmpty()
    }

    suspend fun isTokenValid(): Boolean {
        val token = getAuthToken()
        if (token.isNullOrEmpty()) {        
            return false
        }
        
        val parts = token.split(".")
        if (parts.size != 3) {        
            return false
        }
        return true
    }
} 