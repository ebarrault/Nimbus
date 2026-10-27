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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fait le lien entre le backend (repositories) et l'écran météo.
 *
 * 1. Choisit la ville à afficher : celle choisie en dernier (recherche, favoris, "Ma position"),
 *    sinon le 1er favori, sinon Paris.
 * 2. Observe sa météo en cache et la transforme en [WeatherUiState].
 * 3. Lance un rafraîchissement réseau au démarrage, au changement de ville ou d'unités,
 *    et quand l'utilisateur le demande.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val cityRepository: CityRepository,
    settingsRepository: SettingsRepository,
    private val uiMapper: WeatherUiMapper,
) : ViewModel() {

    /** Ce qu'il faut télécharger : quelle ville, dans quelles unités. */
    private data class Request(
        val city: City,
        val temperatureUnit: TemperatureUnit,
        val windSpeedUnit: WindSpeedUnit,
    )

    private data class Selection(val request: Request, val isFavorite: Boolean)

    private data class RefreshStatus(
        val isRefreshing: Boolean = false,
        val error: DataError? = null,
    )

    private val selection: Flow<Selection> =
        combine(cityRepository.observeFavorites(), settingsRepository.settings) { favorites, settings ->
            val city = settings.lastCity ?: favorites.firstOrNull() ?: DEFAULT_CITY
            Selection(
                request = Request(city, settings.temperatureUnit, settings.windSpeedUnit),
                isFavorite = favorites.any { it.id == city.id },
            )
        }.distinctUntilChanged()

    /** Ajouter un favori ne change pas la requête : pas de nouvel appel réseau. */
    private val request: Flow<Request> = selection.map { it.request }.distinctUntilChanged()

    private val refreshStatus = MutableStateFlow(RefreshStatus())
    private var current: Selection? = null

    /** L'état observé par l'écran. */
    val uiState: StateFlow<WeatherUiState> = selection
        .flatMapLatest { sel ->
            val city = sel.request.city
            combine(weatherRepository.observeWeather(city.id), refreshStatus) { weather, status ->
                val base = if (weather == null) {
                    // Rien en cache : chargement, ou erreur si le téléchargement a échoué
                    WeatherUiState(cityName = city.name, isLoading = status.error == null)
                } else {
                    uiMapper.map(city, weather).copy(isRefreshing = status.isRefreshing)
                }
                base.copy(
                    error = status.error,
                    isFavorite = sel.isFavorite,
                    isCurrentLocation = city.id == City.CURRENT_LOCATION_ID,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeatherUiState(isLoading = true),
        )

    init {
        viewModelScope.launch { selection.collect { current = it } }
        // À chaque changement de ville ou d'unités : rafraîchir (le repository respecte la règle des 30 min)
        viewModelScope.launch {
            request.collectLatest { req -> refresh(req, force = false) }
        }
    }

    /** Glisser vers le bas : on force l'appel réseau. */
    fun onRefresh() {
        val req = current?.request ?: return
        viewModelScope.launch { refresh(req, force = true) }
    }

    /** Bouton "Réessayer" de l'écran d'erreur. */
    fun onRetry() = onRefresh()

    /** L'écran a affiché le message d'erreur : on l'efface pour ne pas le réafficher. */
    fun onErrorShown() {
        refreshStatus.update { it.copy(error = null) }
    }

    /** Étoile de la barre du haut : ajoute ou retire la ville affichée des favoris. */
    fun onToggleFavorite() {
        val sel = current ?: return
        val city = sel.request.city
        if (city.id == City.CURRENT_LOCATION_ID) return // la position change : pas de favori
        viewModelScope.launch {
            if (sel.isFavorite) cityRepository.removeFavorite(city.id) else cityRepository.addFavorite(city)
        }
    }

    private suspend fun refresh(req: Request, force: Boolean) {
        refreshStatus.value = RefreshStatus(isRefreshing = true)
        val result = weatherRepository.refresh(req.city, req.temperatureUnit, req.windSpeedUnit, force)
        refreshStatus.value = RefreshStatus(error = (result as? DataResult.Error)?.error)
    }

    companion object {
        /** Ville affichée tant qu'aucune autre n'a été choisie. */
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
