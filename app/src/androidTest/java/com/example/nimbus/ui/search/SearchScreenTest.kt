package com.example.nimbus.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.nimbus.R
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pauFr = City(1, "Pau", "Nouvelle-Aquitaine", "France", 43.3, -0.36, null)
    private val pauEs = City(2, "Pau", "Catalogne", "Espagne", 42.3, 3.1, null)

    private var state by mutableStateOf(SearchUiState())
    private var typed = ""
    private var clickedCity: City? = null
    private var toggledCity: City? = null
    private var retried = false
    private var myLocationClicked = false
    private var backClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                SearchScreen(
                    state = state,
                    onQueryChange = { typed = it },
                    onRetry = { retried = true },
                    onCityClick = { clickedCity = it },
                    onToggleFavorite = { toggledCity = it },
                    onMyLocationClick = { myLocationClicked = true },
                    onLocationProblemShown = {},
                    onBack = { backClicked = true },
                )
            }
        }
    }

    @Test
    fun auDepart_afficheLAide() {
        composeTestRule.onNodeWithText(str(R.string.search_hint)).assertIsDisplayed()
    }

    @Test
    fun saisie_estTransmise() {
        composeTestRule.onNodeWithText(str(R.string.search_placeholder)).performTextInput("Pau")
        assertEquals("Pau", typed)
    }

    @Test
    fun resultats_homonymesDistinguesParRegionEtPays() {
        state = SearchUiState(query = "Pau", results = SearchResults.Found(listOf(pauFr, pauEs)))

        composeTestRule.onNodeWithText("Nouvelle-Aquitaine, France").assertIsDisplayed()
        composeTestRule.onNodeWithText("Catalogne, Espagne").assertIsDisplayed()
    }

    @Test
    fun clicSurUnResultat_choisitLaVille() {
        state = SearchUiState(query = "Pau", results = SearchResults.Found(listOf(pauFr, pauEs)))

        composeTestRule.onNodeWithText("Catalogne, Espagne").performClick()

        assertEquals(pauEs, clickedCity)
    }

    @Test
    fun etoile_ajouteOuRetireDesFavoris() {
        state = SearchUiState(
            query = "Pau",
            results = SearchResults.Found(listOf(pauFr, pauEs)),
            favoriteIds = setOf(pauFr.id),
        )

        // pauFr est en favori (étoile pleine), pauEs non
        composeTestRule.onNodeWithContentDescription(str(R.string.action_remove_favorite)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(str(R.string.action_add_favorite)).performClick()

        assertEquals(pauEs, toggledCity)
    }

    @Test
    fun aucunResultat_message() {
        state = SearchUiState(query = "zzzz", results = SearchResults.Empty)

        composeTestRule.onNodeWithText(str(R.string.search_no_result, "zzzz")).assertIsDisplayed()
    }

    @Test
    fun erreur_messageEtReessayer() {
        state = SearchUiState(query = "Pau", results = SearchResults.Failed(DataError.NETWORK))

        composeTestRule.onNodeWithText(str(R.string.error_network)).assertIsDisplayed()
        composeTestRule.onNodeWithText(str(R.string.retry)).performClick()

        assertTrue(retried)
    }

    @Test
    fun boutonMaPosition() {
        composeTestRule.onNodeWithText(str(R.string.my_location)).performClick()
        assertTrue(myLocationClicked)
    }

    @Test
    fun boutonRetour() {
        composeTestRule.onAllNodesWithContentDescription(str(R.string.action_back)).onFirst().performClick()
        assertTrue(backClicked)
    }
}
