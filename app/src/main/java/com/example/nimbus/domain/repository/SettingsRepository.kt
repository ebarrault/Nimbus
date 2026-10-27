package com.example.nimbus.domain.repository

import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setTemperatureUnit(unit: TemperatureUnit)
    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)
    suspend fun setThemeMode(mode: ThemeMode)
    /** Change la ville affichée sur l'écran météo. */
    suspend fun setLastCity(city: City?)
}
