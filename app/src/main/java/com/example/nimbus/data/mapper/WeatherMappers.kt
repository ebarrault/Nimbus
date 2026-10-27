package com.example.nimbus.data.mapper

import com.example.nimbus.data.local.entity.WeatherCacheEntity
import com.example.nimbus.data.remote.dto.ForecastResponseDto
import com.example.nimbus.domain.WeatherCodeMapper
import com.example.nimbus.domain.model.CurrentWeather
import com.example.nimbus.domain.model.DailyForecast
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WindSpeedUnit
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/** Code utilisé quand une heure n'a pas de code météo : donnera WeatherCondition.UNKNOWN. */
private const val MISSING_CODE = -1

/**
 * Transforme la réponse de l'API en modèle du domaine.
 * Les tableaux parallèles "hourly" et "daily" sont regroupés en listes d'objets.
 * Une heure ou un jour sans température est ignoré.
 */
fun ForecastResponseDto.toDomain(
    temperatureUnit: TemperatureUnit,
    windSpeedUnit: WindSpeedUnit,
    fetchedAt: Instant,
): Weather {
    val hourlyForecasts = hourly.time.indices.mapNotNull { i ->
        val temperature = hourly.temperature.getOrNull(i) ?: return@mapNotNull null
        HourlyForecast(
            time = LocalDateTime.parse(hourly.time[i]),
            temperature = temperature,
            precipitationProbability = hourly.precipitationProbability.getOrNull(i),
            condition = WeatherCodeMapper.fromCode(hourly.weatherCode.getOrNull(i) ?: MISSING_CODE),
        )
    }

    val dailyForecasts = daily.time.indices.mapNotNull { i ->
        val min = daily.temperatureMin.getOrNull(i) ?: return@mapNotNull null
        val max = daily.temperatureMax.getOrNull(i) ?: return@mapNotNull null
        DailyForecast(
            date = LocalDate.parse(daily.time[i]),
            minTemperature = min,
            maxTemperature = max,
            precipitationProbability = daily.precipitationProbabilityMax.getOrNull(i),
            condition = WeatherCodeMapper.fromCode(daily.weatherCode.getOrNull(i) ?: MISSING_CODE),
            sunrise = daily.sunrise.getOrNull(i)?.let(LocalDateTime::parse),
            sunset = daily.sunset.getOrNull(i)?.let(LocalDateTime::parse),
        )
    }

    return Weather(
        current = CurrentWeather(
            time = LocalDateTime.parse(current.time),
            temperature = current.temperature,
            apparentTemperature = current.apparentTemperature,
            humidity = current.humidity,
            windSpeed = current.windSpeed,
            condition = WeatherCodeMapper.fromCode(current.weatherCode),
            isDay = current.isDay == 1,
        ),
        hourly = hourlyForecasts,
        daily = dailyForecasts,
        timezone = timezone,
        temperatureUnit = temperatureUnit,
        windSpeedUnit = windSpeedUnit,
        fetchedAt = fetchedAt,
    )
}

/** Relit une entrée du cache. Renvoie null si le JSON stocké est illisible (au lieu de planter). */
fun WeatherCacheEntity.toDomain(json: Json): Weather? = try {
    json.decodeFromString(ForecastResponseDto.serializer(), this.json).toDomain(
        temperatureUnit = temperatureUnitFromApi(temperatureUnit),
        windSpeedUnit = windSpeedUnitFromApi(windSpeedUnit),
        fetchedAt = Instant.ofEpochMilli(fetchedAt),
    )
} catch (e: Exception) {
    null
}
