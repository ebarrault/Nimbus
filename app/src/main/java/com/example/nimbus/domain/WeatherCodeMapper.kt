package com.example.nimbus.domain

import com.example.nimbus.domain.model.WeatherCondition

/**
 * Convertit un code météo WMO (champ "weather_code" d'Open-Meteo) en [WeatherCondition].
 * Tableau des codes : https://open-meteo.com/en/docs#weather_variable_documentation
 */
object WeatherCodeMapper {

    fun fromCode(code: Int): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        1 -> WeatherCondition.MAINLY_CLEAR
        2 -> WeatherCondition.PARTLY_CLOUDY
        3 -> WeatherCondition.OVERCAST
        45, 48 -> WeatherCondition.FOG
        51, 53, 55 -> WeatherCondition.DRIZZLE
        56, 57 -> WeatherCondition.FREEZING_DRIZZLE
        61 -> WeatherCondition.RAIN_LIGHT
        63 -> WeatherCondition.RAIN_MODERATE
        65 -> WeatherCondition.RAIN_HEAVY
        66, 67 -> WeatherCondition.FREEZING_RAIN
        71, 73, 75 -> WeatherCondition.SNOW
        77 -> WeatherCondition.SNOW_GRAINS
        80, 81, 82 -> WeatherCondition.RAIN_SHOWERS
        85, 86 -> WeatherCondition.SNOW_SHOWERS
        95 -> WeatherCondition.THUNDERSTORM
        96, 99 -> WeatherCondition.THUNDERSTORM_HAIL
        else -> WeatherCondition.UNKNOWN
    }
}
