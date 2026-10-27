package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.nimbus.R
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.ui.weather.sampleDaily
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DailyListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme { DailyList(days = sampleDaily) }
        }
    }

    @Test
    fun afficheLeTitre() {
        composeTestRule.onNodeWithText(str(R.string.daily_title)).assertIsDisplayed()
    }

    @Test
    fun afficheAujourdhui_puisLesSixJoursSuivants() {
        composeTestRule.onNodeWithText(str(R.string.today)).assertIsDisplayed()
        sampleDaily.filterNot { it.isToday }.forEach { day ->
            composeTestRule.onAllNodesWithText(day.dayLabel).assertCountEquals(1)
        }
    }

    @Test
    fun afficheMinMaxEtRisqueDePluieDuPremierJour() {
        val premier = sampleDaily.first()
        composeTestRule.onNodeWithText(str(R.string.min_max, premier.min, premier.max)).assertIsDisplayed()
        composeTestRule.onNodeWithText(premier.rainChance).assertIsDisplayed()
    }
}
