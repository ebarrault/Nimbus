package com.example.nimbus.domain.model

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { KMH, MS }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Réglages de l'utilisateur. */
data class Settings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Dernière ville affichée, pour la rouvrir au lancement. */
    val lastCityId: Long? = null,
)
