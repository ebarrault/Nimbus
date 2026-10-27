package com.example.nimbus.ui.weather

import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.fakes.TestClock
import com.example.nimbus.fakes.TestData
import com.example.nimbus.fakes.sampleWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.util.Locale

class WeatherUiMapperTest {

    private val fetchedAt = Instant.parse("2026-10-27T12:46:00Z")
    private val clock = TestClock(fetchedAt.plus(Duration.ofMinutes(12)))
    private val mapper = WeatherUiMapper(clock)

    private fun map(locale: Locale = Locale.FRENCH, windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH) =
        mapper.map(TestData.paris, sampleWeather(fetchedAt, TemperatureUnit.CELSIUS, windSpeedUnit), locale)

    @Test
    fun meteoActuelle_valeursArrondiesEtUnites() {
        val state = map()

        assertEquals("Paris", state.cityName)
        assertEquals("18°", state.temperature)      // 18.2
        assertEquals("16°", state.feelsLike)        // 16.4
        assertEquals("14 km/h", state.wind)
        assertEquals("62 %", state.humidity)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, state.condition)
        assertTrue(state.hasWeather)
    }

    @Test
    fun ventEnMetresParSeconde() {
        assertEquals("14 m/s", map(windSpeedUnit = WindSpeedUnit.MS).wind)
    }

    @Test
    fun minEtMax_sontCeuxDAujourdhui() {
        val state = map()

        assertEquals("12°", state.min)
        assertEquals("20°", state.max)
    }

    @Test
    fun heures_commencentALHeureEnCours_avecMaint() {
        val hourly = map().hourly

        // JSON : 12h, 13h, (14h sans température), 15h ; il est 13h45 → 13h et 15h
        assertEquals(listOf("13:00", "15:00"), hourly.map { it.label })
        assertTrue(hourly.first().isNow)
        assertFalse(hourly.last().isNow)
    }

    @Test
    fun jours_nomCourtSelonLaLangue_etAujourdhuiMarque() {
        val fr = map(Locale.FRENCH).daily
        assertTrue(fr[0].isToday)
        assertFalse(fr[1].isToday)
        assertEquals("Mer.", fr[1].dayLabel)       // 28/10/2026 est un mercredi

        assertEquals("Wed", map(Locale.ENGLISH).daily[1].dayLabel)
    }

    @Test
    fun jours_risqueDePluieEtTemperatures() {
        val demain = map().daily[1]

        assertEquals("70 %", demain.rainChance)
        assertEquals("11°", demain.min)
        assertEquals("16°", demain.max)
    }

    @Test
    fun minutesDepuisLaMiseAJour() {
        assertEquals(12L, map().minutesSinceUpdate)
    }

    @Test
    fun horlogeEnRetard_pasDeMinutesNegatives() {
        clock.now = fetchedAt.minus(Duration.ofMinutes(3))

        assertEquals(0L, map().minutesSinceUpdate)
    }
}
