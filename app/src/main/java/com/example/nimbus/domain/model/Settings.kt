package com.example.nimbus.domain.model

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { KMH, MS }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Réglages de l'utilisateur. */
data class Settings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /**
     * Ville affichée sur l'écran météo (favori, ville cherchée ou "Ma position").
     * Enregistrée pour être rouverte au prochain lancement.
     */
    val lastCity: City? = null,
)
