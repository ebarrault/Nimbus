package com.example.nimbus.domain

import com.example.nimbus.domain.model.CurrentWeather
import com.example.nimbus.domain.model.HourlyForecast
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.domain.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class WeatherNext24HoursTest {

    private val midnight = LocalDateTime.parse("2026-10-27T00:00")

    /** 48 heures de prévisions à partir de minuit, comme l'API. */
    private fun weatherAt(currentTime: LocalDateTime) = Weather(
        current = CurrentWeather(currentTime, 18.0, 16.0, 60, 10.0, WeatherCondition.CLEAR, isDay = true),
        hourly = List(48) { i -> HourlyForecast(midnight.plusHours(i.toLong()), 15.0, 0, WeatherCondition.CLEAR) },
        daily = emptyList(),
        timezone = "Europe/Paris",
        temperatureUnit = TemperatureUnit.CELSIUS,
        windSpeedUnit = WindSpeedUnit.KMH,
        fetchedAt = Instant.EPOCH,
    )

    @Test
    fun commenceALHeureEnCours_etContient24Heures() {
        val hours = weatherAt(LocalDateTime.parse("2026-10-27T13:45")).next24Hours()

        assertEquals(24, hours.size)
        assertEquals(LocalDateTime.parse("2026-10-27T13:00"), hours.first().time)
        assertEquals(LocalDateTime.parse("2026-10-28T12:00"), hours.last().time)
    }

    @Test
    fun enFinDeDonnees_renvoieCeQuiReste() {
        val hours = weatherAt(LocalDateTime.parse("2026-10-28T20:10")).next24Hours()

        assertEquals(4, hours.size) // 20h, 21h, 22h, 23h
    }
}
