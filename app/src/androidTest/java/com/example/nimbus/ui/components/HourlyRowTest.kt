package com.example.nimbus.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.example.nimbus.R
import com.example.nimbus.str
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.ui.weather.sampleHourly
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HourlyRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NimbusTheme { HourlyRow(hours = sampleHourly) }
        }
    }

    @Test
    fun afficheLeTitreEtLaPremiereHeure() {
        composeTestRule.onNodeWithText(str(R.string.hourly_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleHourly.first().label).assertIsDisplayed()
    }

    @Test
    fun laDerniereHeureEstAccessibleEnFaisantDefiler() {
        val derniere = sampleHourly.last().label

        // La LazyRow est le seul élément qui défile : on la fait défiler jusqu'à la dernière heure
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText(derniere))

        composeTestRule.onNodeWithText(derniere).assertIsDisplayed()
    }
}
