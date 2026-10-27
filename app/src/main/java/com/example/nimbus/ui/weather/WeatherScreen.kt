package com.example.nimbus.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.ui.components.CurrentWeatherCard
import com.example.nimbus.ui.components.DailyList
import com.example.nimbus.ui.components.ErrorState
import com.example.nimbus.ui.components.HourlyRow
import com.example.nimbus.ui.components.LastUpdatedText
import com.example.nimbus.ui.components.LoadingState
import com.example.nimbus.ui.components.WeatherDetailsRow
import com.example.nimbus.ui.components.messageRes
import com.example.nimbus.ui.theme.NimbusTheme

/**
 * Écran "avec état" : récupère le ViewModel (fourni par Hilt), observe son état
 * et transmet tout à l'écran "sans état" ci-dessous.
 */
@Composable
fun WeatherRoute(
    onSearchClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WeatherScreen(
        state = state,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onErrorShown = viewModel::onErrorShown,
        onToggleFavorite = viewModel::onToggleFavorite,
        onSearchClick = onSearchClick,
        onFavoritesClick = onFavoritesClick,
        onSettingsClick = onSettingsClick,
    )
}

/**
 * Écran "sans état" : il reçoit un [WeatherUiState] et des actions, il ne fait qu'afficher.
 * Trois cas : météo disponible, erreur sans données, chargement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onErrorShown: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.error?.let { stringResource(it.messageRes()) }

    // Erreur alors que des données sont affichées : message discret en bas (Snackbar)
    LaunchedEffect(errorMessage, state.hasWeather) {
        if (errorMessage != null && state.hasWeather) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (state.isCurrentLocation) stringResource(R.string.my_location) else state.cityName)
                },
                navigationIcon = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.AutoMirrored.Outlined.List, contentDescription = stringResource(R.string.favorites_title))
                    }
                },
                actions = {
                    if (!state.isCurrentLocation && state.cityName.isNotEmpty()) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(
                                    if (state.isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite,
                                ),
                            )
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            state.hasWeather -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = contentModifier,
            ) {
                WeatherContent(state)
            }

            errorMessage != null -> ErrorState(
                message = errorMessage,
                onRetry = onRetry,
                modifier = contentModifier,
            )

            else -> LoadingState(modifier = contentModifier)
        }
    }
}

@Composable
private fun WeatherContent(state: WeatherUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
        state.minutesSinceUpdate?.let { minutes ->
            item { LastUpdatedText(lastUpdated = updatedAgoLabel(minutes)) }
        }
    }
}

/** "à l'instant", "il y a 5 minutes", "il y a 2 heures" (accordé au pluriel). */
@Composable
fun updatedAgoLabel(minutes: Long): String = when {
    minutes < 1 -> stringResource(R.string.updated_just_now)
    minutes < 60 -> pluralStringResource(R.plurals.updated_minutes_ago, minutes.toInt(), minutes.toInt())
    else -> {
        val hours = (minutes / 60).toInt()
        pluralStringResource(R.plurals.updated_hours_ago, hours, hours)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeatherScreenPreview() {
    NimbusTheme {
        WeatherScreen(sampleWeatherUiState, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenErrorPreview() {
    NimbusTheme {
        WeatherScreen(WeatherUiState(cityName = "Paris", error = DataError.NETWORK), {}, {}, {}, {}, {}, {}, {})
    }
}
