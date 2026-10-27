package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nimbus.R
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import org.junit.Rule
import org.junit.Test

class LastUpdatedTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun afficheLaDateDeMiseAJour() {
        composeTestRule.setContent {
            NimbusTheme { LastUpdatedText(lastUpdated = "il y a 5 min") }
        }

        composeTestRule
            .onNodeWithText(str(R.string.last_updated, "il y a 5 min"))
            .assertIsDisplayed()
    }
}
