package com.example.nimbus.ui.weather

import com.example.nimbus.domain.model.WeatherCondition

/** Fausses données pour les Previews et les tests d'interface. */
val sampleHourly: List<HourlyUi> = List(24) { i ->
    HourlyUi(
        label = "${(13 + i) % 24}:00",
        temperature = "${18 - i / 3}°",
        condition = when {
            i < 3 -> WeatherCondition.PARTLY_CLOUDY
            i < 6 -> WeatherCondition.RAIN_LIGHT
            i < 12 -> WeatherCondition.OVERCAST
            else -> WeatherCondition.CLEAR
        },
        isNow = i == 0,
    )
}

val sampleDaily: List<DailyUi> = listOf(
    DailyUi("Mar.", WeatherCondition.PARTLY_CLOUDY, "10 %", "12°", "20°", isToday = true),
    DailyUi("Mer.", WeatherCondition.RAIN_MODERATE, "70 %", "11°", "16°"),
    DailyUi("Jeu.", WeatherCondition.CLEAR, "0 %", "9°", "18°"),
    DailyUi("Ven.", WeatherCondition.OVERCAST, "20 %", "10°", "17°"),
    DailyUi("Sam.", WeatherCondition.RAIN_SHOWERS, "60 %", "8°", "14°"),
    DailyUi("Dim.", WeatherCondition.THUNDERSTORM, "80 %", "9°", "15°"),
    DailyUi("Lun.", WeatherCondition.MAINLY_CLEAR, "5 %", "7°", "16°"),
)

val sampleWeatherUiState = WeatherUiState(
    cityName = "Paris",
    temperature = "18°",
    condition = WeatherCondition.PARTLY_CLOUDY,
    feelsLike = "16°",
    min = "12°",
    max = "20°",
    wind = "14 km/h",
    humidity = "62 %",
    hourly = sampleHourly,
    daily = sampleDaily,
    minutesSinceUpdate = 5,
)
