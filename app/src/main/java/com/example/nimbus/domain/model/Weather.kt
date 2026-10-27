package com.example.nimbus.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/** Météo complète d'une ville, telle que stockée en cache. */
data class Weather(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    /** Fuseau horaire de la ville (ex. "Europe/Paris") : les heures ci-dessous sont dans ce fuseau. */
    val timezone: String,
    val temperatureUnit: TemperatureUnit,
    val windSpeedUnit: WindSpeedUnit,
    /** Moment où les données ont été téléchargées. */
    val fetchedAt: Instant,
) {
    /** Les 24 prochaines heures à partir de l'heure actuelle (l'API commence à minuit). */
    fun next24Hours(): List<HourlyForecast> {
        val currentHour = current.time.withMinute(0).withSecond(0).withNano(0)
        return hourly.filter { !it.time.isBefore(currentHour) }.take(24)
    }
}

data class CurrentWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val precipitationProbability: Int?,
    val condition: WeatherCondition,
)

data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val precipitationProbability: Int?,
    val condition: WeatherCondition,
    val sunrise: LocalDateTime?,
    val sunset: LocalDateTime?,
)
