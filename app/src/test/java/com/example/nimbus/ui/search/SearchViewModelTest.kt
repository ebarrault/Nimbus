package com.example.nimbus.ui.search

import com.example.nimbus.MainDispatcherRule
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.LocationResult
import com.example.nimbus.fakes.FakeCityRepository
import com.example.nimbus.fakes.FakeLocationProvider
import com.example.nimbus.fakes.FakeSettingsRepository
import com.example.nimbus.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cityRepository = FakeCityRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val locationProvider = FakeLocationProvider()

    private val paris = TestData.paris
    private val pau = TestData.pau

    /** Nombre de fois où l'écran a reçu "ville choisie, reviens à la météo". */
    private var citySelectedEvents = 0

    private fun TestScope.createViewModel(): SearchViewModel {
        val viewModel = SearchViewModel(cityRepository, settingsRepository, locationProvider)
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.uiState.collect {} }
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.citySelected.collect { citySelectedEvents++ } }
        // L'état n'est complet qu'après le premier passage du délai de 300 ms (texte vide)
        waitDebounce()
        return viewModel
    }

    /** Laisse passer le délai de 300 ms (en temps virtuel : le test reste instantané). */
    private fun TestScope.waitDebounce() {
        advanceTimeBy(SearchViewModel.DEBOUNCE_MS + 1)
        runCurrent()
    }

    // --- Recherche ---

    @Test
    fun auDepart_rienNEstCherche() = runTest {
        val viewModel = createViewModel()
        waitDebounce()

        assertEquals(SearchResults.Idle, viewModel.uiState.value.results)
        assertTrue(cityRepository.searchQueries.isEmpty())
    }

    @Test
    fun saisie_laRechercheAttend300ms() = runTest {
        cityRepository.searchResult = DataResult.Success(listOf(pau))
        val viewModel = createViewModel()

        viewModel.onQueryChange("Pau")
        advanceTimeBy(200)
        runCurrent()
        assertTrue(cityRepository.searchQueries.isEmpty()) // pas encore

        waitDebounce()
        assertEquals(listOf("Pau"), cityRepository.searchQueries)
        assertEquals(SearchResults.Found(listOf(pau)), viewModel.uiState.value.results)
    }

    @Test
    fun frappeRapide_uneSeuleRequete() = runTest {
        val viewModel = createViewModel()

        listOf("P", "Pa", "Pau").forEach {
            viewModel.onQueryChange(it)
            advanceTimeBy(100) // l'utilisateur tape plus vite que 300 ms
        }
        waitDebounce()

        assertEquals(listOf("Pau"), cityRepository.searchQueries)
    }

    @Test
    fun uneSeuleLettre_pasDeRequete() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("P")
        waitDebounce()

        assertEquals(SearchResults.Idle, viewModel.uiState.value.results)
        assertTrue(cityRepository.searchQueries.isEmpty())
    }

    @Test
    fun aucuneVille_resultatVide() = runTest {
        cityRepository.searchResult = DataResult.Success(emptyList())
        val viewModel = createViewModel()

        viewModel.onQueryChange("zzzz")
        waitDebounce()

        assertEquals(SearchResults.Empty, viewModel.uiState.value.results)
    }

    @Test
    fun erreurReseau_puisReessayer_relanceLaMemeRecherche() = runTest {
        cityRepository.searchResult = DataResult.Error(DataError.NETWORK)
        val viewModel = createViewModel()
        viewModel.onQueryChange("Pau")
        waitDebounce()
        assertEquals(SearchResults.Failed(DataError.NETWORK), viewModel.uiState.value.results)

        cityRepository.searchResult = DataResult.Success(listOf(pau))
        viewModel.onRetry()
        runCurrent()

        assertEquals(listOf("Pau", "Pau"), cityRepository.searchQueries)
        assertEquals(SearchResults.Found(listOf(pau)), viewModel.uiState.value.results)
    }

    // --- Choix d'une ville et favoris ---

    @Test
    fun clicSurUneVille_laMemorise_etDemandeLeRetour() = runTest {
        val viewModel = createViewModel()

        viewModel.onCityClick(pau)

        assertEquals(pau, settingsRepository.settings.first().lastCity)
        assertEquals(1, citySelectedEvents)
    }

    @Test
    fun etoile_ajoutePuisRetireDesFavoris() = runTest {
        val viewModel = createViewModel()

        viewModel.onToggleFavorite(pau)
        assertEquals(setOf(pau.id), viewModel.uiState.value.favoriteIds)

        viewModel.onToggleFavorite(pau)
        assertTrue(viewModel.uiState.value.favoriteIds.isEmpty())
    }

    @Test
    fun favorisExistants_sontMarques() = runTest {
        cityRepository.favorites.value = listOf(paris)

        val viewModel = createViewModel()

        assertEquals(setOf(paris.id), viewModel.uiState.value.favoriteIds)
    }

    // --- Ma position ---

    @Test
    fun autorisationRefusee_messageSansAppelALaLocalisation() = runTest {
        val viewModel = createViewModel()

        viewModel.onLocationPermissionResult(granted = false)

        assertEquals(LocationProblem.PERMISSION_DENIED, viewModel.uiState.value.locationProblem)
        assertEquals(0, locationProvider.callCount)

        viewModel.onLocationProblemShown()
        assertNull(viewModel.uiState.value.locationProblem)
    }

    @Test
    fun positionTrouvee_devientLaVilleAffichee() = runTest {
        locationProvider.result = LocationResult.Success(latitude = 43.3, longitude = -0.36)
        val viewModel = createViewModel()

        viewModel.onLocationPermissionResult(granted = true)

        val city = settingsRepository.settings.first().lastCity!!
        assertEquals(City.CURRENT_LOCATION_ID, city.id)
        assertEquals(43.3, city.latitude, 0.0)
        assertEquals(1, citySelectedEvents)
        assertFalse(viewModel.uiState.value.isLocating)
    }

    @Test
    fun localisationDesactivee_message() = runTest {
        locationProvider.result = LocationResult.LocationDisabled
        val viewModel = createViewModel()

        viewModel.onLocationPermissionResult(granted = true)

        assertEquals(LocationProblem.LOCATION_DISABLED, viewModel.uiState.value.locationProblem)
        assertEquals(0, citySelectedEvents)
    }

    @Test
    fun positionIntrouvable_message() = runTest {
        locationProvider.result = LocationResult.Unavailable
        val viewModel = createViewModel()

        viewModel.onLocationPermissionResult(granted = true)

        assertEquals(LocationProblem.UNAVAILABLE, viewModel.uiState.value.locationProblem)
    }
}
