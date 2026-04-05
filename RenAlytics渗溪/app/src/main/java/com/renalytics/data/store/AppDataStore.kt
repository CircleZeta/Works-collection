package com.renalytics.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "renalytics_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class AppDataStore(private val context: Context) {
    companion object {
        val BLUETOOTH_DEVICE_ADDRESS = stringPreferencesKey("bluetooth_device_address")
        val BLUETOOTH_DEVICE_NAME = stringPreferencesKey("bluetooth_device_name")
        val DEFAULT_USER_ID = stringPreferencesKey("default_user_id")
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    val bluetoothDeviceAddress: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[BLUETOOTH_DEVICE_ADDRESS] }

    val bluetoothDeviceName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[BLUETOOTH_DEVICE_NAME] }

    val defaultUserId: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[DEFAULT_USER_ID]?.toLongOrNull() }

    val appTheme: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[APP_THEME] }

    suspend fun saveBluetoothDevice(address: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[BLUETOOTH_DEVICE_ADDRESS] = address
            preferences[BLUETOOTH_DEVICE_NAME] = name
        }
    }

    suspend fun saveDefaultUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_USER_ID] = userId.toString()
        }
    }

    suspend fun saveAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme
        }
    }

    suspend fun clearBluetoothDevice() {
        context.dataStore.edit { preferences ->
            preferences.remove(BLUETOOTH_DEVICE_ADDRESS)
            preferences.remove(BLUETOOTH_DEVICE_NAME)
        }
    }
}
