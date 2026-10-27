package com.example.nimbus.fakes

import com.example.nimbus.data.mapper.toDomain
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.domain.repository.CityRepository
import com.example.nimbus.domain.repository.SettingsRepository
import com.example.nimbus.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/** Une météo complète d'exemple (tirée de forecast_paris.json). */
fun sampleWeather(
    fetchedAt: Instant = Instant.parse("2026-10-27T12:46:00Z"),
    temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
): Weather = TestData.forecastDto.toDomain(temperatureUnit, windSpeedUnit, fetchedAt)

/**
 * Faux WeatherRepository : le cache est une simple Map, et chaque appel à refresh()
 * est enregistré pour que les tests vérifient quand et comment le ViewModel rafraîchit.
 */
class FakeWeatherRepository : WeatherRepository {

    data class RefreshCall(
        val city: City,
        val temperatureUnit: TemperatureUnit,
        val windSpeedUnit: WindSpeedUnit,
        val force: Boolean,
    )

    private val cache = MutableStateFlow<Map<Long, Weather>>(emptyMap())
    val refreshCalls = mutableListOf<RefreshCall>()

    /** Ce que renverra le prochain refresh(). */
    var nextResult: DataResult<Unit> = DataResult.Success(Unit)

    fun setCachedWeather(cityId: Long, weather: Weather) = cache.update { it + (cityId to weather) }

    override fun observeWeather(cityId: Long): Flow<Weather?> = cache.map { it[cityId] }

    override suspend fun refresh(
        city: City,
        temperatureUnit: TemperatureUnit,
        windSpeedUnit: WindSpeedUnit,
        force: Boolean,
    ): DataResult<Unit> {
        refreshCalls += RefreshCall(city, temperatureUnit, windSpeedUnit, force)
        return nextResult
    }
}

class FakeCityRepository : CityRepository {
    val favorites = MutableStateFlow<List<City>>(emptyList())

    override suspend fun search(query: String): DataResult<List<City>> = DataResult.Success(emptyList())
    override fun observeFavorites(): Flow<List<City>> = favorites
    override suspend fun addFavorite(city: City) = favorites.update { if (city in it) it else it + city }
    override suspend fun removeFavorite(cityId: Long) = favorites.update { list -> list.filterNot { it.id == cityId } }
    override suspend fun reorderFavorites(orderedIds: List<Long>) = favorites.update { list ->
        orderedIds.mapNotNull { id -> list.firstOrNull { it.id == id } }
    }
}

class FakeSettingsRepository(initial: Settings = Settings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<Settings> = state
    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = state.update { it.copy(temperatureUnit = unit) }
    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) = state.update { it.copy(windSpeedUnit = unit) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setLastCity(city: City?) = state.update { it.copy(lastCity = city) }
}
