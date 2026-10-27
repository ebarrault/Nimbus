package com.example.nimbus.data.repository

import com.example.nimbus.data.local.entity.WeatherCacheEntity
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.fakes.FakeForecastApi
import com.example.nimbus.fakes.FakeWeatherCacheDao
import com.example.nimbus.fakes.TestClock
import com.example.nimbus.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Duration

class WeatherRepositoryImplTest {

    private lateinit var api: FakeForecastApi
    private lateinit var dao: FakeWeatherCacheDao
    private lateinit var clock: TestClock
    private lateinit var repository: WeatherRepositoryImpl

    private val paris = TestData.paris

    @Before
    fun setUp() {
        api = FakeForecastApi()
        dao = FakeWeatherCacheDao()
        clock = TestClock()
        repository = WeatherRepositoryImpl(api = api, cacheDao = dao, clock = clock)
    }

    private suspend fun refresh(
        force: Boolean = false,
        temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
        windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    ) = repository.refresh(paris, temperatureUnit, windSpeedUnit, force)

    @Test
    fun sansCache_observeEmetNull() = runTest {
        assertNull(repository.observeWeather(paris.id).first())
    }

    @Test
    fun sansCache_appelleLApi_etEnregistreLaMeteo() = runTest {
        val result = refresh()

        assertEquals(DataResult.Success(Unit), result)
        assertEquals(1, api.callCount)
        val weather = repository.observeWeather(paris.id).first()
        assertNotNull(weather)
        assertEquals(18.2, weather!!.current.temperature, 0.0)
        assertEquals(clock.now, weather.fetchedAt)
    }

    @Test
    fun cacheDeMoinsDe30Minutes_pasDAppelReseau() = runTest {
        refresh()
        clock.advanceBy(Duration.ofMinutes(10))

        refresh()

        assertEquals(1, api.callCount)
    }

    @Test
    fun cacheDePlusDe30Minutes_rappelleLApi() = runTest {
        refresh()
        clock.advanceBy(Duration.ofMinutes(35))

        refresh()

        assertEquals(2, api.callCount)
    }

    @Test
    fun actualisationForcee_rappelleLApiMemeSiCacheRecent() = runTest {
        refresh()

        refresh(force = true)

        assertEquals(2, api.callCount)
    }

    @Test
    fun changementDUnite_rappelleLApiAvecLaNouvelleUnite() = runTest {
        refresh()

        refresh(temperatureUnit = TemperatureUnit.FAHRENHEIT, windSpeedUnit = WindSpeedUnit.MS)

        assertEquals(2, api.callCount)
        assertEquals("fahrenheit", api.lastTemperatureUnit)
        assertEquals("ms", api.lastWindSpeedUnit)
        assertEquals(TemperatureUnit.FAHRENHEIT, repository.observeWeather(paris.id).first()!!.temperatureUnit)
    }

    @Test
    fun erreurReseau_renvoieErreur_etGardeLAncienCache() = runTest {
        refresh()
        val avant = repository.observeWeather(paris.id).first()
        clock.advanceBy(Duration.ofHours(2))
        api.error = IOException("mode avion")

        val result = refresh()

        assertEquals(DataResult.Error(DataError.NETWORK), result)
        assertEquals(avant, repository.observeWeather(paris.id).first()) // cache intact, même date
    }

    @Test
    fun erreurReseauSansCache_renvoieErreur_etRienEnCache() = runTest {
        api.error = IOException("mode avion")

        val result = refresh()

        assertEquals(DataResult.Error(DataError.NETWORK), result)
        assertNull(repository.observeWeather(paris.id).first())
    }

    @Test
    fun reponseInexploitable_nEcrasePasLeCache() = runTest {
        refresh()
        val avant = repository.observeWeather(paris.id).first()
        // Réponse avec une date illisible : la conversion échoue
        api.response = TestData.forecastDto.copy(current = TestData.forecastDto.current.copy(time = "hier"))

        val result = refresh(force = true)

        assertEquals(DataResult.Error(DataError.PARSING), result)
        assertEquals(avant, repository.observeWeather(paris.id).first())
    }

    @Test
    fun cacheCorrompu_observeEmetNull() = runTest {
        dao.upsert(WeatherCacheEntity(paris.id, "pas du json", "celsius", "kmh", 0L))

        assertNull(repository.observeWeather(paris.id).first())
    }
}
