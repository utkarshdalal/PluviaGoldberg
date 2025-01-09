package com.OxGames.Pluvia

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.OxGames.Pluvia.ui.enums.Orientation
import `in`.dragonbra.javasteam.enums.EPersonaState
import java.util.EnumSet
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
        name = "PluviaPreferences",
        corruptionHandler = ReplaceFileCorruptionHandler {
            Timber.e("Preferences (somehow got) corrupted, resetting.")
            emptyPreferences()
        },
    )

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("PrefManager"))

    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.datastore
    }

    fun clearPreferences() {
        scope.launch {
            dataStore.edit { it.clear() }
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        getPref(booleanPreferencesKey(key), defaultValue)

    fun getString(key: String, defaultValue: String): String =
        getPref(stringPreferencesKey(key), defaultValue)

    @Suppress("SameParameterValue")
    private fun <T> getPref(key: Preferences.Key<T>, defaultValue: T): T = runBlocking {
        dataStore.data.first()[key] ?: defaultValue
    }

    @Suppress("SameParameterValue")
    private fun <T> setPref(key: Preferences.Key<T>, value: T) {
        scope.launch {
            dataStore.edit { pref -> pref[key] = value }
        }
    }

    // private fun <T> removePref(key: Preferences.Key<T>) {
    //     scope.launch {
    //         dataStore.edit { pref -> pref.remove(key) }
    //     }
    // }

    /* Login Info */
    private val CELL_ID = intPreferencesKey("cell_id")
    var cellId: Int
        get() = getPref(CELL_ID, 0)
        set(value) {
            setPref(CELL_ID, value)
        }

    private val USER_NAME = stringPreferencesKey("user_name")
    var username: String
        get() = getPref(USER_NAME, "")
        set(value) {
            setPref(USER_NAME, value)
        }

    private val APP_INSTALL_PATH = stringPreferencesKey("app_install_path")
    var appInstallPath: String
        get() = getPref(APP_INSTALL_PATH, SteamService.defaultAppInstallPath)
        set(value) {
            setPref(APP_INSTALL_PATH, value)
        }

    private val APP_STAGING_PATH = stringPreferencesKey("app_staging_path")
    var appStagingPath: String
        get() = getPref(APP_STAGING_PATH, SteamService.defaultAppStagingPath)
        set(value) {
            setPref(APP_STAGING_PATH, value)
        }

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    var accessToken: String
        get() = getPref(ACCESS_TOKEN, "")
        set(value) {
            setPref(ACCESS_TOKEN, value)
        }

    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    var refreshToken: String
        get() = getPref(REFRESH_TOKEN, "")
        set(value) {
            setPref(REFRESH_TOKEN, value)
        }

    // Special: Because null value.
    private val CLIENT_ID = longPreferencesKey("client_id")
    var clientId: Long?
        get() = runBlocking { dataStore.data.first()[CLIENT_ID] }
        set(value) {
            scope.launch {
                dataStore.edit { pref -> pref[CLIENT_ID] = value!! }
            }
        }

    private val REMEMBER_PASSWORD = booleanPreferencesKey("remember_password")
    var rememberPassword: Boolean
        get() = getPref(REMEMBER_PASSWORD, false)
        set(value) {
            setPref(REMEMBER_PASSWORD, value)
        }

    private val PASSWORD = stringPreferencesKey("password")
    var password: String
        get() = getPref(PASSWORD, "")
        set(value) {
            setPref(PASSWORD, value)
        }

    /**
     * Get or Set the last known Persona State. See [EPersonaState]
     */
    private val PERSONA_STATE = intPreferencesKey("persona_state")
    var personaState: EPersonaState
        get() {
            val value = getPref(PERSONA_STATE, EPersonaState.Online.code())
            return EPersonaState.from(value)
        }
        set(value) {
            setPref(PERSONA_STATE, value.code())
        }

    private val ALLOWED_ORIENTATION = intPreferencesKey("allowed_orientation")
    var allowedOrientation: EnumSet<Orientation>
        get() {
            val defaultValue = Orientation.toInt(
                EnumSet.of(Orientation.LANDSCAPE, Orientation.REVERSE_LANDSCAPE),
            )
            val value = getPref(ALLOWED_ORIENTATION, defaultValue)
            return Orientation.fromInt(value)
        }
        set(value) {
            setPref(ALLOWED_ORIENTATION, Orientation.toInt(value))
        }

    private val TIPPED = booleanPreferencesKey("tipped")
    var tipped: Boolean
        get() {
            val value = getPref(TIPPED, false)
            return value
        }
        set(value) {
            setPref(TIPPED, value)
        }
}
