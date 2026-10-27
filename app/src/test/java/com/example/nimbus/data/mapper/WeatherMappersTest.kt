package com.example.nimbus.data.mapper

import com.example.nimbus.data.local.entity.WeatherCacheEntity
import com.example.nimbus.data.remote.NetworkFactory
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.fakes.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class WeatherMappersTest {

    private val fetchedAt = Instant.parse("2026-10-27T12:46:00Z")
    private val weather = TestData.forecastDto.toDomain(TemperatureUnit.CELSIUS, WindSpeedUnit.KMH, fetchedAt)

    @Test
    fun meteoActuelle_estConvertie() {
        with(weather.current) {
            assertEquals(LocalDateTime.parse("2026-10-27T13:45"), time)
            assertEquals(18.2, temperature, 0.0)
            assertEquals(16.4, apparentTemperature, 0.0)
            assertEquals(62, humidity)
            assertEquals(14.0, windSpeed, 0.0)
            assertEquals(WeatherCondition.PARTLY_CLOUDY, condition)
            assertTrue(isDay)
        }
        assertEquals("Europe/Paris", weather.timezone)
        assertEquals(fetchedAt, weather.fetchedAt)
    }

    @Test
    fun tableauxHoraires_sontRegroupes_etHeureSansTemperatureIgnoree() {
        // 4 heures dans le JSON, mais celle de 14h n'a pas de température
        assertEquals(3, weather.hourly.size)
        assertEquals(
            listOf("2026-10-27T12:00", "2026-10-27T13:00", "2026-10-27T15:00").map(LocalDateTime::parse),
            weather.hourly.map { it.time },
        )
    }

    @Test
    fun heureSansCodeNiProbabilite_donneUnknownEtNull() {
        val quinzeHeures = weather.hourly.last()
        assertEquals(WeatherCondition.UNKNOWN, quinzeHeures.condition)
        assertNull(quinzeHeures.precipitationProbability)
    }

    @Test
    fun jours_sontConvertis() {
        assertEquals(2, weather.daily.size)
        with(weather.daily[1]) {
            assertEquals(LocalDate.parse("2026-10-28"), date)
            assertEquals(11.2, minTemperature, 0.0)
            assertEquals(16.3, maxTemperature, 0.0)
            assertEquals(70, precipitationProbability)
            assertEquals(WeatherCondition.RAIN_MODERATE, condition)
            assertEquals(LocalDateTime.parse("2026-10-28T07:14"), sunrise)
        }
    }

    @Test
    fun cacheValide_estRelu() {
        val entry = WeatherCacheEntity(1L, TestData.forecastJson, "fahrenheit", "ms", fetchedAt.toEpochMilli())

        val cached = entry.toDomain(NetworkFactory.json)

        assertNotNull(cached)
        assertEquals(TemperatureUnit.FAHRENHEIT, cached!!.temperatureUnit)
        assertEquals(WindSpeedUnit.MS, cached.windSpeedUnit)
        assertEquals(fetchedAt, cached.fetchedAt)
    }

    @Test
    fun cacheCorrompu_renvoieNullSansPlanter() {
        val entry = WeatherCacheEntity(1L, "ceci n'est pas du JSON", "celsius", "kmh", 0L)

        assertNull(entry.toDomain(NetworkFactory.json))
    }

    @Test
    fun unites_versEtDepuisLApi() {
        assertEquals("fahrenheit", TemperatureUnit.FAHRENHEIT.apiValue)
        assertEquals("ms", WindSpeedUnit.MS.apiValue)
        TemperatureUnit.entries.forEach { assertEquals(it, temperatureUnitFromApi(it.apiValue)) }
        WindSpeedUnit.entries.forEach { assertEquals(it, windSpeedUnitFromApi(it.apiValue)) }
    }
}
