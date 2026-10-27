package com.example.nimbus.ui.settings

import com.example.nimbus.MainDispatcherRule
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.fakes.FakeSettingsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    @Test
    fun reglagesParDefaut() = runTest {
        val viewModel = SettingsViewModel(repository)
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.settings.collect {} }

        assertEquals(Settings(), viewModel.settings.value)
    }

    @Test
    fun chaqueChoix_estEnregistre() = runTest {
        val viewModel = SettingsViewModel(repository)
        backgroundScope.launch(mainDispatcherRule.testDispatcher) { viewModel.settings.collect {} }

        viewModel.onTemperatureUnitChange(TemperatureUnit.FAHRENHEIT)
        viewModel.onWindSpeedUnitChange(WindSpeedUnit.MS)
        viewModel.onThemeModeChange(ThemeMode.DARK)

        with(viewModel.settings.value) {
            assertEquals(TemperatureUnit.FAHRENHEIT, temperatureUnit)
            assertEquals(WindSpeedUnit.MS, windSpeedUnit)
            assertEquals(ThemeMode.DARK, themeMode)
        }
    }
}
