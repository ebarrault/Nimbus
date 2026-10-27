package com.example.nimbus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Réponse de https://api.open-meteo.com/v1/forecast
 * Attention : "hourly" et "daily" sont des tableaux parallèles (time[i] va avec temperature[i]…).
 * Les valeurs peuvent être null quand un modèle météo n'a pas la donnée.
 */
@Serializable
data class ForecastResponseDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("is_day") val isDay: Int,
)

@Serializable
data class HourlyDto(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double?>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
)

@Serializable
data class DailyDto(
    val time: List<String>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    val sunrise: List<String?> = emptyList(),
    val sunset: List<String?> = emptyList(),
)
