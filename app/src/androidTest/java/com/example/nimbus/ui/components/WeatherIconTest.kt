package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.nimbus.R
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Rule
import org.junit.Test

class WeatherIconTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun icone_aUneDescriptionPourLAccessibilite() {
        composeTestRule.setContent {
            NimbusTheme { WeatherIcon(condition = WeatherCondition.RAIN_LIGHT) }
        }

        composeTestRule
            .onNodeWithContentDescription(str(R.string.weather_rain_light))
            .assertIsDisplayed()
    }
}
