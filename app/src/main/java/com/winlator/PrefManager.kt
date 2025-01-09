package com.winlator

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Kind of ugly, but works to be a universal preference manager.
 */
object PrefManager {

    private val Context.datastore by preferencesDataStore(
        name = "WinlatorPreferences",
        corruptionHandler = ReplaceFileCorruptionHandler {
            Timber.e("Preferences (somehow got) corrupted, resetting.")
            emptyPreferences()
        },
    )

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("PrefManager"))

    private var dataStore: DataStore<Preferences>? = null

    @JvmStatic
    fun init(context: Context) {
        if (dataStore == null) {
            dataStore = context.datastore
        }
    }

    @JvmStatic
    fun deInit() {
        dataStore = null
    }

    @JvmStatic
    fun getString(key: String, defaultValue: String): String =
        getPref(stringPreferencesKey(key), defaultValue)

    @JvmStatic
    fun putString(key: String, value: String) {
        setPref(stringPreferencesKey(key), value)
    }

    @JvmStatic
    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        getPref(booleanPreferencesKey(key), defaultValue)

    @JvmStatic
    fun putBoolean(key: String, value: Boolean) {
        setPref(booleanPreferencesKey(key), value)
    }

    private fun <T> getPref(key: Preferences.Key<T>, defaultValue: T): T = runBlocking {
        dataStore!!.data.first()[key] ?: defaultValue
    }

    private fun <T> setPref(key: Preferences.Key<T>, value: T) {
        scope.launch {
            dataStore!!.edit { pref -> pref[key] = value }
        }
    }
}
