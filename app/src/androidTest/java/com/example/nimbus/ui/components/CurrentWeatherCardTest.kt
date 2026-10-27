package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nimbus.R
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CurrentWeatherCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme {
                CurrentWeatherCard(
                    temperature = "18°",
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    feelsLike = "16°",
                    min = "12°",
                    max = "20°",
                )
            }
        }
    }

    @Test
    fun afficheLaTemperature() {
        composeTestRule.onNodeWithText("18°").assertIsDisplayed()
    }

    @Test
    fun afficheLeLibelleDeLaCondition() {
        composeTestRule.onNodeWithText(str(R.string.weather_partly_cloudy)).assertIsDisplayed()
    }

    @Test
    fun afficheRessentiMinEtMax() {
        composeTestRule
            .onNodeWithText(str(R.string.feels_like_min_max, "16°", "12°", "20°"))
            .assertIsDisplayed()
    }
}
