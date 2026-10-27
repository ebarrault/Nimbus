package com.example.nimbus.ui.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.plural
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeatherScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var state by mutableStateOf(sampleWeatherUiState)
    private var searchClicked = false
    private var settingsClicked = false
    private var retryClicked = false
    private var errorShown = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                WeatherScreen(
                    state = state,
                    onRetry = { retryClicked = true },
                    onRefresh = {},
                    onErrorShown = { errorShown = true },
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
        val minutes = sampleWeatherUiState.minutesSinceUpdate!!.toInt()
        val texte = str(R.string.last_updated, plural(R.plurals.updated_minutes_ago, minutes, minutes))

        // Deux éléments défilent : la LazyColumn de l'écran (verticale) et la LazyRow
        // des heures (horizontale). On cible celui qui défile verticalement.
        composeTestRule
            .onNode(hasScrollAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText(texte))

        composeTestRule.onNodeWithText(texte).assertIsDisplayed()
    }

    @Test
    fun chargementSansDonnees_afficheLIndicateur() {
        state = WeatherUiState(cityName = "Paris", isLoading = true)

        composeTestRule.onNodeWithContentDescription(str(R.string.loading)).assertIsDisplayed()
    }

    @Test
    fun erreurSansDonnees_afficheLeMessageEtReessayer() {
        state = WeatherUiState(cityName = "Paris", error = DataError.NETWORK)

        composeTestRule.onNodeWithText(str(R.string.error_network)).assertIsDisplayed()
        composeTestRule.onNodeWithText(str(R.string.retry)).performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun erreurAvecDonnees_gardeLaMeteo_etAfficheUnMessage() {
        state = sampleWeatherUiState.copy(error = DataError.NETWORK)

        // la météo reste affichée…
        composeTestRule.onNodeWithText(sampleWeatherUiState.wind).assertIsDisplayed()
        // …et le message apparaît dans la Snackbar, puis l'écran prévient le ViewModel
        composeTestRule.waitUntil(timeoutMillis = 10_000) { errorShown }
    }
}
