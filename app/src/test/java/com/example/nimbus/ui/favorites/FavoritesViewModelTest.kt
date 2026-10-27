package com.example.nimbus.ui.favorites

import com.example.nimbus.MainDispatcherRule
import com.example.nimbus.domain.model.City
import com.example.nimbus.fakes.FakeCityRepository
import com.example.nimbus.fakes.FakeSettingsRepository
import com.example.nimbus.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cityRepository = FakeCityRepository()
    private val settingsRepository = FakeSettingsRepository()

    private val paris = TestData.paris
    private val pau = TestData.pau
    private val lyon = City(2996944, "Lyon", "Auvergne-Rhône-Alpes", "France", 45.75, 4.85, "Europe/Paris")

    private var citySelectedEvents = 0

    private fun TestScope.createViewModel(vararg favorites: City): FavoritesViewModel {
        cityRepository.favorites.value = favorites.toList()
        val viewModel = FavoritesViewModel(cityRepository, settingsRepository)
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.uiState.collect {} }
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.citySelected.collect { citySelectedEvents++ } }
        return viewModel
    }

    private fun FavoritesViewModel.names() = uiState.value.favorites.map { it.name }

    @Test
    fun afficheLesFavorisDansLOrdre() = runTest {
        val viewModel = createViewModel(paris, pau)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf("Paris", "Pau"), viewModel.names())
    }

    @Test
    fun clicSurUnFavori_leMemorise_etDemandeLeRetour() = runTest {
        val viewModel = createViewModel(paris, pau)

        viewModel.onCityClick(pau)

        assertEquals(pau, settingsRepository.settings.first().lastCity)
        assertEquals(1, citySelectedEvents)
    }

    @Test
    fun suppression_retireLaVille_etProposeDAnnuler() = runTest {
        val viewModel = createViewModel(paris, pau)

        viewModel.onDelete(paris)

        assertEquals(listOf("Pau"), viewModel.names())
        assertEquals(paris, viewModel.uiState.value.recentlyDeleted)
    }

    @Test
    fun annuler_remetLaVilleASaPlace() = runTest {
        val viewModel = createViewModel(paris, pau, lyon)
        viewModel.onDelete(pau) // celle du milieu

        viewModel.onUndoDelete()

        assertEquals(listOf("Paris", "Pau", "Lyon"), viewModel.names())
        assertNull(viewModel.uiState.value.recentlyDeleted)
    }

    @Test
    fun messageDeSuppressionAffiche_plusDAnnulationPossible() = runTest {
        val viewModel = createViewModel(paris, pau)
        viewModel.onDelete(paris)

        viewModel.onDeleteMessageShown()
        viewModel.onUndoDelete() // sans effet

        assertNull(viewModel.uiState.value.recentlyDeleted)
        assertEquals(listOf("Pau"), viewModel.names())
    }

    @Test
    fun monterEtDescendre() = runTest {
        val viewModel = createViewModel(paris, pau, lyon)

        viewModel.onMoveUp(lyon)
        assertEquals(listOf("Paris", "Lyon", "Pau"), viewModel.names())

        viewModel.onMoveDown(paris)
        assertEquals(listOf("Lyon", "Paris", "Pau"), viewModel.names())
    }

    @Test
    fun monterLePremier_ouDescendreLeDernier_neChangeRien() = runTest {
        val viewModel = createViewModel(paris, pau)

        viewModel.onMoveUp(paris)
        viewModel.onMoveDown(pau)

        assertEquals(listOf("Paris", "Pau"), viewModel.names())
    }
}
