package com.example.nimbus.domain.model

/**
 * Conditions météo affichées par l'app.
 * Chaque valeur correspond à un ou plusieurs codes WMO renvoyés par Open-Meteo.
 */
enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN_LIGHT,
    RAIN_MODERATE,
    RAIN_HEAVY,
    FREEZING_RAIN,
    SNOW,
    SNOW_GRAINS,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    THUNDERSTORM_HAIL,
    UNKNOWN,
}
