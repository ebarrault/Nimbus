package com.example.nimbus.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.domain.repository.CityRepository
import com.example.nimbus.domain.repository.SettingsRepository
import com.example.nimbus.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fait le lien entre le backend (repositories) et l'écran météo.
 *
 * 1. Choisit la ville à afficher (dernière ville vue, sinon 1er favori, sinon Paris).
 * 2. Observe sa météo en cache et la transforme en [WeatherUiState].
 * 3. Lance un rafraîchissement réseau au démarrage, au changement de ville ou d'unité,
 *    et quand l'utilisateur le demande.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    cityRepository: CityRepository,
    settingsRepository: SettingsRepository,
    private val uiMapper: WeatherUiMapper,
) : ViewModel() {

    /** Ce qu'il faut télécharger : quelle ville, dans quelles unités. */
    private data class Request(
        val city: City,
        val temperatureUnit: TemperatureUnit,
        val windSpeedUnit: WindSpeedUnit,
    )

    private data class RefreshStatus(
        val isRefreshing: Boolean = false,
        val error: DataError? = null,
    )

    private val request: Flow<Request> =
        combine(cityRepository.observeFavorites(), settingsRepository.settings) { favorites, settings ->
            val city = favorites.firstOrNull { it.id == settings.lastCityId }
                ?: favorites.firstOrNull()
                ?: DEFAULT_CITY
            Request(city, settings.temperatureUnit, settings.windSpeedUnit)
        }.distinctUntilChanged()

    private val refreshStatus = MutableStateFlow(RefreshStatus())
    private var lastRequest: Request? = null

    /** L'état observé par l'écran. */
    val uiState: StateFlow<WeatherUiState> = request
        .flatMapLatest { req ->
            combine(weatherRepository.observeWeather(req.city.id), refreshStatus) { weather, status ->
                if (weather == null) {
                    // Rien en cache : chargement, ou erreur si le téléchargement a échoué
                    WeatherUiState(
                        cityName = req.city.name,
                        isLoading = status.error == null,
                        error = status.error,
                    )
                } else {
                    uiMapper.map(req.city, weather).copy(
                        isRefreshing = status.isRefreshing,
                        error = status.error,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeatherUiState(isLoading = true),
        )

    init {
        // À chaque changement de ville ou d'unités : rafraîchir (le repository respecte la règle des 30 min)
        viewModelScope.launch {
            request.collectLatest { req ->
                lastRequest = req
                refresh(req, force = false)
            }
        }
    }

    /** Glisser vers le bas : on force l'appel réseau. */
    fun onRefresh() {
        val req = lastRequest ?: return
        viewModelScope.launch { refresh(req, force = true) }
    }

    /** Bouton "Réessayer" de l'écran d'erreur. */
    fun onRetry() = onRefresh()

    /** L'écran a affiché le message d'erreur : on l'efface pour ne pas le réafficher. */
    fun onErrorShown() {
        refreshStatus.update { it.copy(error = null) }
    }

    private suspend fun refresh(req: Request, force: Boolean) {
        refreshStatus.value = RefreshStatus(isRefreshing = true)
        val result = weatherRepository.refresh(req.city, req.temperatureUnit, req.windSpeedUnit, force)
        refreshStatus.value = RefreshStatus(error = (result as? DataResult.Error)?.error)
    }

    companion object {
        /** Ville affichée tant qu'il n'y a aucun favori. */
        val DEFAULT_CITY = City(
            id = 2988507,
            name = "Paris",
            region = "Île-de-France",
            country = "France",
            latitude = 48.85341,
            longitude = 2.3488,
            timezone = "Europe/Paris",
        )
    }
}
