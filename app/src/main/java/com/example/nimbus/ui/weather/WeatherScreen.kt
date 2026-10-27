package com.example.nimbus.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.ui.components.CurrentWeatherCard
import com.example.nimbus.ui.components.DailyList
import com.example.nimbus.ui.components.HourlyRow
import com.example.nimbus.ui.components.LastUpdatedText
import com.example.nimbus.ui.components.WeatherDetailsRow
import com.example.nimbus.ui.theme.NimbusTheme

/**
 * Écran d'accueil "sans état" : il reçoit un [WeatherUiState] et des actions,
 * il ne fait qu'assembler les composants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.cityName) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CurrentWeatherCard(
                    temperature = state.temperature,
                    condition = state.condition,
                    feelsLike = state.feelsLike,
                    min = state.min,
                    max = state.max,
                )
            }
            item { WeatherDetailsRow(wind = state.wind, humidity = state.humidity) }
            item { HourlyRow(hours = state.hourly) }
            item { DailyList(days = state.daily) }
            item { LastUpdatedText(lastUpdated = state.lastUpdated) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeatherScreenPreview() {
    NimbusTheme {
        WeatherScreen(
            state = sampleWeatherUiState,
            onSearchClick = {},
            onSettingsClick = {},
        )
    }
}
