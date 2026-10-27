package com.example.nimbus.ui.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.example.nimbus.R
import com.example.nimbus.domain.model.City
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val paris = City(1, "Paris", "Île-de-France", "France", 48.85, 2.35, null)
    private val pau = City(2, "Pau", "Nouvelle-Aquitaine", "France", 43.3, -0.36, null)

    private var state by mutableStateOf(FavoritesUiState(favorites = listOf(paris, pau), isLoading = false))
    private var clicked: City? = null
    private var deleted: City? = null
    private var undone = false
    private var movedUp: City? = null
    private var movedDown: City? = null
    private var searchClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                FavoritesScreen(
                    state = state,
                    onCityClick = { clicked = it },
                    onDelete = { deleted = it },
                    onUndoDelete = { undone = true },
                    onDeleteMessageShown = {},
                    onMoveUp = { movedUp = it },
                    onMoveDown = { movedDown = it },
                    onSearchClick = { searchClicked = true },
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun afficheLesFavoris() {
        composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pau").assertIsDisplayed()
    }

    @Test
    fun clicSurUnFavori() {
        composeTestRule.onNodeWithText("Pau").performClick()
        assertEquals(pau, clicked)
    }

    @Test
    fun premierNePeutPasMonter_dernierNePeutPasDescendre() {
        composeTestRule.onNodeWithContentDescription(str(R.string.move_up, "Paris")).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(str(R.string.move_down, "Pau")).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(str(R.string.move_down, "Paris")).assertIsEnabled()
    }

    @Test
    fun flechesDeReordonnancement() {
        composeTestRule.onNodeWithContentDescription(str(R.string.move_down, "Paris")).performClick()
        composeTestRule.onNodeWithContentDescription(str(R.string.move_up, "Pau")).performClick()

        assertEquals(paris, movedDown)
        assertEquals(pau, movedUp)
    }

    @Test
    fun glisserUneLigne_laSupprime() {
        composeTestRule.onNodeWithText("Paris").performTouchInput { swipeLeft() }
        composeTestRule.waitUntil(timeoutMillis = 5_000) { deleted != null }

        assertEquals(paris, deleted)
    }

    @Test
    fun apresSuppression_annulerEstPropose() {
        state = state.copy(favorites = listOf(pau), recentlyDeleted = paris)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText(str(R.string.undo)))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(str(R.string.undo)).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) { undone }
    }

    @Test
    fun listeVide_messageEtBoutonRecherche() {
        state = FavoritesUiState(favorites = emptyList(), isLoading = false)

        composeTestRule.onNodeWithText(str(R.string.favorites_empty)).assertIsDisplayed()
        composeTestRule.onNodeWithText(str(R.string.search_title)).performClick()

        assertTrue(searchClicked)
    }
}
