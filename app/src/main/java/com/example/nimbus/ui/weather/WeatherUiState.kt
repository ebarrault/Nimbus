package com.example.nimbus.ui.weather

import com.example.nimbus.domain.model.WeatherCondition

/** Une heure de la bande "Prochaines 24 h", déjà formatée pour l'affichage. */
data class HourlyUi(
    val label: String,        // "Maint.", "14h"…
    val temperature: String,  // "18°"
    val condition: WeatherCondition,
)

/** Un jour de la liste "7 prochains jours", déjà formaté pour l'affichage. */
data class DailyUi(
    val dayLabel: String,     // "Auj.", "Mar."…
    val condition: WeatherCondition,
    val rainChance: String,   // "70 %"
    val min: String,          // "11°"
    val max: String,          // "16°"
)

/** Tout ce que l'écran météo affiche. Plus tard, c'est le ViewModel qui le remplira. */
data class WeatherUiState(
    val cityName: String = "",
    val temperature: String = "",
    val condition: WeatherCondition = WeatherCondition.UNKNOWN,
    val feelsLike: String = "",
    val min: String = "",
    val max: String = "",
    val wind: String = "",
    val humidity: String = "",
    val hourly: List<HourlyUi> = emptyList(),
    val daily: List<DailyUi> = emptyList(),
    val lastUpdated: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
