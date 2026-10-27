package com.example.nimbus.ui.weather

import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WindSpeedUnit
import java.time.Clock
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Transforme les données du domaine (nombres, dates) en textes prêts à afficher ("18°", "14:00"…).
 * Du Kotlin pur, sans Android : facile à tester.
 */
class WeatherUiMapper @Inject constructor(
    private val clock: Clock,
) {

    fun map(city: City, weather: Weather, locale: Locale = Locale.getDefault()): WeatherUiState {
        val hourFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", locale)
        // "Aujourd'hui" dans le fuseau de la ville, pas celui du téléphone
        val today = weather.current.time.toLocalDate()
        val todayForecast = weather.daily.firstOrNull { it.date == today }
        val windUnit = when (weather.windSpeedUnit) {
            WindSpeedUnit.KMH -> "km/h"
            WindSpeedUnit.MS -> "m/s"
        }

        return WeatherUiState(
            cityName = city.name,
            temperature = weather.current.temperature.asDegrees(),
            condition = weather.current.condition,
            feelsLike = weather.current.apparentTemperature.asDegrees(),
            min = todayForecast?.minTemperature?.asDegrees() ?: NO_VALUE,
            max = todayForecast?.maxTemperature?.asDegrees() ?: NO_VALUE,
            wind = "${weather.current.windSpeed.roundToInt()} $windUnit",
            humidity = "${weather.current.humidity} %",
            hourly = weather.next24Hours().mapIndexed { index, hour ->
                HourlyUi(
                    label = hour.time.format(hourFormatter),
                    temperature = hour.temperature.asDegrees(),
                    condition = hour.condition,
                    isNow = index == 0,
                )
            },
            daily = weather.daily.map { day ->
                DailyUi(
                    dayLabel = day.date.format(dayFormatter).replaceFirstChar { it.titlecase(locale) },
                    condition = day.condition,
                    rainChance = day.precipitationProbability?.let { "$it %" } ?: NO_VALUE,
                    min = day.minTemperature.asDegrees(),
                    max = day.maxTemperature.asDegrees(),
                    isToday = day.date == today,
                )
            },
            minutesSinceUpdate = Duration.between(weather.fetchedAt, clock.instant()).toMinutes().coerceAtLeast(0),
        )
    }

    private fun Double.asDegrees(): String = "${roundToInt()}°"

    companion object {
        const val NO_VALUE = "–"
    }
}
