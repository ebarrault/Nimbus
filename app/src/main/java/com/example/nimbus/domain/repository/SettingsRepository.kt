package com.example.nimbus.domain.repository

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
    suspend fun setLastCityId(cityId: Long?)
}
