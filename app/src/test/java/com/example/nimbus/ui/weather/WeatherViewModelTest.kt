package com.example.nimbus.ui.weather

import com.example.nimbus.MainDispatcherRule
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.fakes.FakeCityRepository
import com.example.nimbus.fakes.FakeSettingsRepository
import com.example.nimbus.fakes.FakeWeatherRepository
import com.example.nimbus.fakes.TestClock
import com.example.nimbus.fakes.TestData
import com.example.nimbus.fakes.sampleWeather
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weatherRepository = FakeWeatherRepository()
    private val cityRepository = FakeCityRepository()
    private val settingsRepository = FakeSettingsRepository()

    private val paris = TestData.paris
    private val pau = TestData.pau

    /**
     * Crée le ViewModel et observe uiState en arrière-plan, comme le ferait l'écran.
     * (uiState ne calcule rien tant que personne ne l'observe : SharingStarted.WhileSubscribed.)
     */
    private fun TestScope.createViewModel(): WeatherViewModel {
        val viewModel = WeatherViewModel(
            weatherRepository = weatherRepository,
            cityRepository = cityRepository,
            settingsRepository = settingsRepository,
            uiMapper = WeatherUiMapper(TestClock()),
        )
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.uiState.collect {} }
        return viewModel
    }

    // --- Choix de la ville ---

    @Test
    fun sansFavori_afficheParisParDefaut_etLeRafraichit() = runTest {
        val viewModel = createViewModel()

        assertEquals(WeatherViewModel.DEFAULT_CITY.name, viewModel.uiState.value.cityName)
        assertEquals(WeatherViewModel.DEFAULT_CITY, weatherRepository.refreshCalls.single().city)
        assertFalse(weatherRepository.refreshCalls.single().force) // la règle des 30 min s'applique
    }

    @Test
    fun avecFavoris_afficheLePremier() = runTest {
        cityRepository.favorites.value = listOf(pau, paris)

        val viewModel = createViewModel()

        assertEquals("Pau", viewModel.uiState.value.cityName)
    }

    @Test
    fun derniereVilleVue_estPrioritaire() = runTest {
        cityRepository.favorites.value = listOf(pau, paris)
        settingsRepository.setLastCityId(paris.id)

        val viewModel = createViewModel()

        assertEquals("Paris", viewModel.uiState.value.cityName)
        assertEquals(paris, weatherRepository.refreshCalls.last().city)
    }

    @Test
    fun changementDeVille_rafraichitLaNouvelleVille() = runTest {
        cityRepository.favorites.value = listOf(paris, pau)
        val viewModel = createViewModel()

        settingsRepository.setLastCityId(pau.id)

        assertEquals("Pau", viewModel.uiState.value.cityName)
        assertEquals(listOf(paris, pau), weatherRepository.refreshCalls.map { it.city })
    }

    // --- États de l'écran ---

    @Test
    fun pasDeCache_rafraichissementSansErreur_resteEnChargement() = runTest {
        // Le refresh réussit mais n'a encore rien écrit en cache
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertFalse(state.hasWeather)
        assertNull(state.error)
    }

    @Test
    fun meteoEnCache_contenuAffiche() = runTest {
        weatherRepository.setCachedWeather(WeatherViewModel.DEFAULT_CITY.id, sampleWeather())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.hasWeather)
        assertFalse(state.isLoading)
        assertEquals("18°", state.temperature)
    }

    @Test
    fun leCacheArriveApres_lEcranSeMetAJour() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.hasWeather)

        weatherRepository.setCachedWeather(WeatherViewModel.DEFAULT_CITY.id, sampleWeather())

        assertTrue(viewModel.uiState.value.hasWeather)
    }

    @Test
    fun erreurSansCache_afficheLErreur() = runTest {
        weatherRepository.nextResult = DataResult.Error(DataError.NETWORK)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(DataError.NETWORK, state.error)
        assertFalse(state.isLoading)
        assertFalse(state.hasWeather)
    }

    @Test
    fun erreurAvecCache_gardeLaMeteo_puisLErreurEstEffacee() = runTest {
        weatherRepository.setCachedWeather(WeatherViewModel.DEFAULT_CITY.id, sampleWeather())
        weatherRepository.nextResult = DataResult.Error(DataError.SERVER)
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.hasWeather)
        assertEquals(DataError.SERVER, viewModel.uiState.value.error)

        viewModel.onErrorShown()

        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.hasWeather)
    }

    // --- Actions de l'utilisateur ---

    @Test
    fun glisserPourActualiser_forceLAppelReseau() = runTest {
        val viewModel = createViewModel()

        viewModel.onRefresh()

        assertEquals(listOf(false, true), weatherRepository.refreshCalls.map { it.force })
    }

    @Test
    fun reessayer_apresUneErreur_relanceEtEffaceLErreur() = runTest {
        weatherRepository.nextResult = DataResult.Error(DataError.NETWORK)
        val viewModel = createViewModel()
        weatherRepository.nextResult = DataResult.Success(Unit)

        viewModel.onRetry()

        assertEquals(2, weatherRepository.refreshCalls.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun changementDUnites_rafraichitDansLesNouvellesUnites() = runTest {
        createViewModel()

        settingsRepository.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        settingsRepository.setWindSpeedUnit(WindSpeedUnit.MS)

        val last = weatherRepository.refreshCalls.last()
        assertEquals(TemperatureUnit.FAHRENHEIT, last.temperatureUnit)
        assertEquals(WindSpeedUnit.MS, last.windSpeedUnit)
    }

    @Test
    fun changementDeTheme_neRelancePasDAppelReseau() = runTest {
        createViewModel()

        settingsRepository.setThemeMode(com.example.nimbus.domain.model.ThemeMode.DARK)

        assertEquals(1, weatherRepository.refreshCalls.size)
    }

    @Test
    fun reglagesParDefaut_celsiusEtKmh() = runTest {
        createViewModel()

        val first = weatherRepository.refreshCalls.first()
        assertEquals(Settings().temperatureUnit, first.temperatureUnit)
        assertEquals(Settings().windSpeedUnit, first.windSpeedUnit)
    }
}
