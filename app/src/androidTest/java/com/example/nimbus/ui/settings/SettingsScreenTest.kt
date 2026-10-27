package com.example.nimbus.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.nimbus.R
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var settings by mutableStateOf(Settings())
    private var temperatureUnit: TemperatureUnit? = null
    private var windSpeedUnit: WindSpeedUnit? = null
    private var themeMode: ThemeMode? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                SettingsScreen(
                    settings = settings,
                    onTemperatureUnitChange = { temperatureUnit = it },
                    onWindSpeedUnitChange = { windSpeedUnit = it },
                    onThemeModeChange = { themeMode = it },
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun reglagesActuels_sontCoches() {
        composeTestRule.onNodeWithText(str(R.string.unit_celsius)).assertIsSelected()
        composeTestRule.onNodeWithText(str(R.string.unit_fahrenheit)).assertIsNotSelected()
        composeTestRule.onNodeWithText(str(R.string.unit_kmh)).assertIsSelected()
        composeTestRule.onNodeWithText(str(R.string.theme_system)).performScrollTo().assertIsSelected()
    }

    @Test
    fun changementDeReglage_suitLEtat() {
        settings = Settings(temperatureUnit = TemperatureUnit.FAHRENHEIT)

        composeTestRule.onNodeWithText(str(R.string.unit_fahrenheit)).assertIsSelected()
        composeTestRule.onNodeWithText(str(R.string.unit_celsius)).assertIsNotSelected()
    }

    @Test
    fun clicSurUnChoix_transmetLaValeur() {
        composeTestRule.onNodeWithText(str(R.string.unit_fahrenheit)).performClick()
        composeTestRule.onNodeWithText(str(R.string.unit_ms)).performClick()
        composeTestRule.onNodeWithText(str(R.string.theme_dark)).performScrollTo().performClick()

        assertEquals(TemperatureUnit.FAHRENHEIT, temperatureUnit)
        assertEquals(WindSpeedUnit.MS, windSpeedUnit)
        assertEquals(ThemeMode.DARK, themeMode)
    }

    @Test
    fun creditOpenMeteo_estAffiche() {
        composeTestRule.onNodeWithText(str(R.string.about_text)).performScrollTo().assertIsDisplayed()
    }
}
