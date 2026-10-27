package com.example.nimbus.ui.weather

import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.WeatherCondition

/** Une heure de la bande "Prochaines 24 h", déjà formatée pour l'affichage. */
data class HourlyUi(
    val label: String,        // "14:00" (ou "2:00 PM" en anglais)
    val temperature: String,  // "18°"
    val condition: WeatherCondition,
    /** Première heure de la liste : l'écran affiche "Maint." à la place de l'heure. */
    val isNow: Boolean = false,
)

/** Un jour de la liste "7 prochains jours", déjà formaté pour l'affichage. */
data class DailyUi(
    val dayLabel: String,     // "Mer.", "Jeu."…
    val condition: WeatherCondition,
    val rainChance: String,   // "70 %"
    val min: String,          // "11°"
    val max: String,          // "16°"
    /** L'écran affiche "Aujourd'hui" à la place du nom du jour. */
    val isToday: Boolean = false,
)

/** Tout ce que l'écran météo affiche. C'est le WeatherViewModel qui le remplit. */
data class WeatherUiState(
    val cityName: String = "",
    /** La ville affichée est "Ma position" : l'écran affiche ce libellé à la place du nom. */
    val isCurrentLocation: Boolean = false,
    /** La ville affichée est dans les favoris (étoile pleine). */
    val isFavorite: Boolean = false,
    val temperature: String = "",
    val condition: WeatherCondition = WeatherCondition.UNKNOWN,
    val feelsLike: String = "",
    val min: String = "",
    val max: String = "",
    val wind: String = "",
    val humidity: String = "",
    val hourly: List<HourlyUi> = emptyList(),
    val daily: List<DailyUi> = emptyList(),
    /** Minutes écoulées depuis le téléchargement des données (null s'il n'y a pas de données). */
    val minutesSinceUpdate: Long? = null,
    /** Premier chargement, rien à afficher pour l'instant. */
    val isLoading: Boolean = false,
    /** Actualisation en cours alors que des données sont déjà affichées. */
    val isRefreshing: Boolean = false,
    /** Dernière erreur de rafraîchissement, à afficher une fois. */
    val error: DataError? = null,
) {
    val hasWeather: Boolean get() = temperature.isNotEmpty()
}
