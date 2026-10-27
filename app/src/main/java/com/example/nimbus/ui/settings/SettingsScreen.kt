package com.example.nimbus.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.ui.theme.NimbusTheme

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onTemperatureUnitChange = viewModel::onTemperatureUnitChange,
        onWindSpeedUnitChange = viewModel::onWindSpeedUnitChange,
        onThemeModeChange = viewModel::onThemeModeChange,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onWindSpeedUnitChange: (WindSpeedUnit) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            ChoiceSection(
                title = R.string.settings_temperature,
                options = listOf(
                    TemperatureUnit.CELSIUS to R.string.unit_celsius,
                    TemperatureUnit.FAHRENHEIT to R.string.unit_fahrenheit,
                ),
                selected = settings.temperatureUnit,
                onSelect = onTemperatureUnitChange,
            )
            HorizontalDivider()
            ChoiceSection(
                title = R.string.settings_wind,
                options = listOf(
                    WindSpeedUnit.KMH to R.string.unit_kmh,
                    WindSpeedUnit.MS to R.string.unit_ms,
                ),
                selected = settings.windSpeedUnit,
                onSelect = onWindSpeedUnitChange,
            )
            HorizontalDivider()
            ChoiceSection(
                title = R.string.settings_theme,
                options = listOf(
                    ThemeMode.SYSTEM to R.string.theme_system,
                    ThemeMode.LIGHT to R.string.theme_light,
                    ThemeMode.DARK to R.string.theme_dark,
                ),
                selected = settings.themeMode,
                onSelect = onThemeModeChange,
            )
            HorizontalDivider()
            AboutSection()
        }
    }
}

/** Un titre et une liste de boutons radio : un seul choix possible. */
@Composable
private fun <T> ChoiceSection(
    @StringRes title: Int,
    options: List<Pair<T, Int>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionTitle(title)
        Column(modifier = Modifier.selectableGroup()) {
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // toute la ligne est cliquable, et annoncée comme bouton radio (accessibilité)
                        .selectable(
                            selected = value == selected,
                            onClick = { onSelect(value) },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = value == selected, onClick = null)
                    Text(stringResource(label), modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** Crédit obligatoire de la licence Open-Meteo (CC BY 4.0). */
@Composable
private fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionTitle(R.string.settings_about)
        Text(
            text = stringResource(R.string.about_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        TextButton(
            onClick = { uriHandler.openUri("https://open-meteo.com/") },
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Text(stringResource(R.string.about_link))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    NimbusTheme {
        SettingsScreen(Settings(), {}, {}, {}, {})
    }
}
