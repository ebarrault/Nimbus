package com.example.nimbus.data.mapper

import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WindSpeedUnit

/** Valeurs attendues par les paramètres "temperature_unit" et "wind_speed_unit" de l'API. */
val TemperatureUnit.apiValue: String
    get() = when (this) {
        TemperatureUnit.CELSIUS -> "celsius"
        TemperatureUnit.FAHRENHEIT -> "fahrenheit"
    }

val WindSpeedUnit.apiValue: String
    get() = when (this) {
        WindSpeedUnit.KMH -> "kmh"
        WindSpeedUnit.MS -> "ms"
    }

fun temperatureUnitFromApi(value: String): TemperatureUnit =
    TemperatureUnit.entries.firstOrNull { it.apiValue == value } ?: TemperatureUnit.CELSIUS

fun windSpeedUnitFromApi(value: String): WindSpeedUnit =
    WindSpeedUnit.entries.firstOrNull { it.apiValue == value } ?: WindSpeedUnit.KMH
