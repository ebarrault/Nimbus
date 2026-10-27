package com.example.nimbus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbus.data.repository.CityRepositoryImpl
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.LocationResult
import com.example.nimbus.domain.repository.CityRepository
import com.example.nimbus.domain.repository.LocationProvider
import com.example.nimbus.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Résultat de la recherche, tel que l'écran l'affiche. */
sealed interface SearchResults {
    /** Moins de 2 caractères : on n'a rien cherché. */
    data object Idle : SearchResults
    data object Loading : SearchResults
    data class Found(val cities: List<City>) : SearchResults
    data object Empty : SearchResults
    data class Failed(val error: DataError) : SearchResults
}

/** Problème rencontré avec "Ma position". */
enum class LocationProblem { PERMISSION_DENIED, LOCATION_DISABLED, UNAVAILABLE }

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults.Idle,
    val favoriteIds: Set<Long> = emptySet(),
    val isLocating: Boolean = false,
    val locationProblem: LocationProblem? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val query = MutableStateFlow("")
    /** Incrémenté par "Réessayer" pour relancer la même recherche. */
    private val retryCount = MutableStateFlow(0)
    private val location = MutableStateFlow(Pair<Boolean, LocationProblem?>(false, null))

    /**
     * La recherche : on attend 300 ms sans frappe (debounce), on ignore un texte identique
     * (distinctUntilChanged), et une nouvelle saisie annule la requête précédente (flatMapLatest).
     */
    private val results: Flow<SearchResults> = query
        .map { it.trim() }
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .combine(retryCount) { text, _ -> text }
        .flatMapLatest { text ->
            flow {
                if (text.length < CityRepositoryImpl.MIN_QUERY_LENGTH) {
                    emit(SearchResults.Idle)
                    return@flow
                }
                emit(SearchResults.Loading)
                emit(
                    when (val result = cityRepository.search(text)) {
                        is DataResult.Success ->
                            if (result.data.isEmpty()) SearchResults.Empty else SearchResults.Found(result.data)
                        is DataResult.Error -> SearchResults.Failed(result.error)
                    },
                )
            }
        }

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        results,
        cityRepository.observeFavorites(),
        location,
    ) { text, found, favorites, (isLocating, problem) ->
        SearchUiState(
            query = text,
            results = found,
            favoriteIds = favorites.map { it.id }.toSet(),
            isLocating = isLocating,
            locationProblem = problem,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    /** Événement ponctuel : une ville a été choisie, l'écran doit revenir à la météo. */
    private val _citySelected = Channel<Unit>(Channel.BUFFERED)
    val citySelected: Flow<Unit> = _citySelected.receiveAsFlow()

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun onRetry() {
        retryCount.update { it + 1 }
    }

    fun onCityClick(city: City) {
        viewModelScope.launch {
            settingsRepository.setLastCity(city)
            _citySelected.send(Unit)
        }
    }

    fun onToggleFavorite(city: City) {
        viewModelScope.launch {
            if (city.id in uiState.value.favoriteIds) {
                cityRepository.removeFavorite(city.id)
            } else {
                cityRepository.addFavorite(city)
            }
        }
    }

    /** Réponse de l'utilisateur à la demande d'autorisation de localisation. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            location.value = false to LocationProblem.PERMISSION_DENIED
            return
        }
        viewModelScope.launch {
            location.value = true to null
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    location.value = false to null
                    onCityClick(
                        City(
                            id = City.CURRENT_LOCATION_ID,
                            name = "",
                            region = null,
                            country = null,
                            latitude = result.latitude,
                            longitude = result.longitude,
                            timezone = null,
                        ),
                    )
                }
                LocationResult.PermissionDenied -> location.value = false to LocationProblem.PERMISSION_DENIED
                LocationResult.LocationDisabled -> location.value = false to LocationProblem.LOCATION_DISABLED
                LocationResult.Unavailable -> location.value = false to LocationProblem.UNAVAILABLE
            }
        }
    }

    fun onLocationProblemShown() {
        location.update { (isLocating, _) -> isLocating to null }
    }

    companion object {
        const val DEBOUNCE_MS = 300L
    }
}
