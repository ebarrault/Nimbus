package com.example.nimbus.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.repository.CityRepository
import com.example.nimbus.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<City> = emptyList(),
    val isLoading: Boolean = true,
    /** Ville qu'on vient de supprimer : l'écran propose "Annuler". */
    val recentlyDeleted: City? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** Ce qu'il faut pour annuler une suppression : la ville et l'ordre d'avant. */
    private data class Deletion(val city: City, val previousOrder: List<Long>)

    private val deletion = MutableStateFlow<Deletion?>(null)

    val uiState: StateFlow<FavoritesUiState> =
        combine(cityRepository.observeFavorites(), deletion) { favorites, deleted ->
            FavoritesUiState(favorites = favorites, isLoading = false, recentlyDeleted = deleted?.city)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    private val _citySelected = Channel<Unit>(Channel.BUFFERED)
    /** Événement ponctuel : une ville a été choisie, l'écran doit revenir à la météo. */
    val citySelected: Flow<Unit> = _citySelected.receiveAsFlow()

    fun onCityClick(city: City) {
        viewModelScope.launch {
            settingsRepository.setLastCity(city)
            _citySelected.send(Unit)
        }
    }

    fun onDelete(city: City) {
        val order = uiState.value.favorites.map { it.id }
        deletion.value = Deletion(city, order)
        viewModelScope.launch { cityRepository.removeFavorite(city.id) }
    }

    /** "Annuler" : on remet la ville, puis l'ancien ordre. */
    fun onUndoDelete() {
        val deleted = deletion.value ?: return
        deletion.value = null
        viewModelScope.launch {
            cityRepository.addFavorite(deleted.city)
            cityRepository.reorderFavorites(deleted.previousOrder)
        }
    }

    fun onDeleteMessageShown() {
        deletion.value = null
    }

    fun onMoveUp(city: City) = move(city, -1)

    fun onMoveDown(city: City) = move(city, +1)

    private fun move(city: City, offset: Int) {
        val ids = uiState.value.favorites.map { it.id }.toMutableList()
        val from = ids.indexOf(city.id)
        val to = from + offset
        if (from == -1 || to !in ids.indices) return
        ids.add(to, ids.removeAt(from))
        viewModelScope.launch { cityRepository.reorderFavorites(ids) }
    }
}
