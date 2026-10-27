package com.example.nimbus.ui.weather

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.nimbus.R
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeatherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var searchClicked = false
    private var settingsClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                WeatherScreen(
                    state = sampleWeatherUiState,
                    onSearchClick = { searchClicked = true },
                    onSettingsClick = { settingsClicked = true },
                )
            }
        }
    }

    @Test
    fun afficheLeNomDeLaVilleDansLaBarreDuHaut() {
        composeTestRule.onNodeWithText(sampleWeatherUiState.cityName).assertIsDisplayed()
    }

    @Test
    fun clicSurRecherche_appelleOnSearchClick() {
        composeTestRule.onNodeWithContentDescription(str(R.string.action_search)).performClick()
        assertTrue(searchClicked)
    }

    @Test
    fun clicSurReglages_appelleOnSettingsClick() {
        composeTestRule.onNodeWithContentDescription(str(R.string.action_settings)).performClick()
        assertTrue(settingsClicked)
    }

    @Test
    fun laDateDeMiseAJourEstAccessibleEnBasDeLEcran() {
        val texte = str(R.string.last_updated, sampleWeatherUiState.lastUpdated)

        // Deux éléments défilent : la LazyColumn de l'écran (verticale) et la LazyRow
        // des heures (horizontale). On cible celui qui défile verticalement.
        composeTestRule
            .onNode(hasScrollAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText(texte))

        composeTestRule.onNodeWithText(texte).assertIsDisplayed()
    }
}
