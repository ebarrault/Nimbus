package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nimbus.R
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Rule
import org.junit.Test

class WeatherDetailsRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun afficheLeVentEtLHumiditeAvecLeursLibelles() {
        composeTestRule.setContent {
            NimbusTheme { WeatherDetailsRow(wind = "14 km/h", humidity = "62 %") }
        }

        composeTestRule.onNodeWithText("14 km/h").assertIsDisplayed()
        composeTestRule.onNodeWithText("62 %").assertIsDisplayed()
        composeTestRule.onNodeWithText(str(R.string.detail_wind)).assertIsDisplayed()
        composeTestRule.onNodeWithText(str(R.string.detail_humidity)).assertIsDisplayed()
    }
}
