package com.example.nimbus.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

/** Le fichier de réglages de l'app (une seule instance par application, garanti par "by"). */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data
        .catch { e ->
            // Fichier illisible : on repart des valeurs par défaut plutôt que de planter.
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            Settings(
                temperatureUnit = prefs[Keys.TEMPERATURE_UNIT].toEnumOr(TemperatureUnit.CELSIUS),
                windSpeedUnit = prefs[Keys.WIND_SPEED_UNIT].toEnumOr(WindSpeedUnit.KMH),
                themeMode = prefs[Keys.THEME_MODE].toEnumOr(ThemeMode.SYSTEM),
                lastCity = prefs[Keys.LAST_CITY]?.let(::decodeCity),
            )
        }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[Keys.TEMPERATURE_UNIT] = unit.name }
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { it[Keys.WIND_SPEED_UNIT] = unit.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setLastCity(city: City?) {
        dataStore.edit { prefs ->
            if (city == null) {
                prefs.remove(Keys.LAST_CITY)
            } else {
                prefs[Keys.LAST_CITY] = json.encodeToString(StoredCity.serializer(), city.toStored())
            }
        }
    }

    /** Ville illisible (format d'une ancienne version…) : on l'ignore au lieu de planter. */
    private fun decodeCity(value: String): City? = try {
        json.decodeFromString(StoredCity.serializer(), value).toDomain()
    } catch (e: Exception) {
        null
    }

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_CITY = stringPreferencesKey("last_city")
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

/** Relit un enum enregistré par son nom ; valeur par défaut si absent ou inconnu. */
private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
