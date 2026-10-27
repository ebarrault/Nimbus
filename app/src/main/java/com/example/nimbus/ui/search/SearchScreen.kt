package com.example.nimbus.ui.search

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.City
import com.example.nimbus.ui.components.ErrorState
import com.example.nimbus.ui.components.LoadingState
import com.example.nimbus.ui.components.messageRes
import com.example.nimbus.ui.theme.NimbusTheme

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onCitySelected: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Une ville choisie (ou "Ma position" trouvée) : on revient à l'écran météo
    LaunchedEffect(Unit) {
        viewModel.citySelected.collect { onCitySelected() }
    }

    // La permission n'est demandée qu'au moment où l'utilisateur touche "Ma position"
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onLocationPermissionResult(granted) }

    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::onRetry,
        onCityClick = viewModel::onCityClick,
        onToggleFavorite = viewModel::onToggleFavorite,
        onMyLocationClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onLocationProblemShown = viewModel::onLocationProblemShown,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onCityClick: (City) -> Unit,
    onToggleFavorite: (City) -> Unit,
    onMyLocationClick: () -> Unit,
    onLocationProblemShown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val problemMessage = state.locationProblem?.let {
        stringResource(
            when (it) {
                LocationProblem.PERMISSION_DENIED -> R.string.location_permission_denied
                LocationProblem.LOCATION_DISABLED -> R.string.location_disabled
                LocationProblem.UNAVAILABLE -> R.string.location_unavailable
            },
        )
    }
    LaunchedEffect(problemMessage) {
        if (problemMessage != null) {
            snackbarHostState.showSnackbar(problemMessage)
            onLocationProblemShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            OutlinedButton(
                onClick = onMyLocationClick,
                enabled = !state.isLocating,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null)
                Text(
                    text = stringResource(if (state.isLocating) R.string.locating else R.string.my_location),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            when (val results = state.results) {
                SearchResults.Idle -> CenteredText(stringResource(R.string.search_hint))
                SearchResults.Loading -> LoadingState()
                SearchResults.Empty -> CenteredText(stringResource(R.string.search_no_result, state.query.trim()))
                is SearchResults.Failed -> ErrorState(
                    message = stringResource(results.error.messageRes()),
                    onRetry = onRetry,
                )
                is SearchResults.Found -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(results.cities, key = { it.id }) { city ->
                        CityRow(
                            city = city,
                            isFavorite = city.id in state.favoriteIds,
                            onClick = { onCityClick(city) },
                            onToggleFavorite = { onToggleFavorite(city) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** Une suggestion : "Pau" puis "Nouvelle-Aquitaine, France" pour distinguer les homonymes. */
@Composable
private fun CityRow(
    city: City,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(city.name) },
        supportingContent = {
            Text(listOfNotNull(city.region, city.country).joinToString(", "))
        },
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.action_remove_favorite else R.string.action_add_favorite,
                    ),
                )
            }
        },
    )
}

@Composable
private fun CenteredText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    NimbusTheme {
        SearchScreen(
            state = SearchUiState(
                query = "Pau",
                results = SearchResults.Found(
                    listOf(
                        City(1, "Pau", "Nouvelle-Aquitaine", "France", 43.3, -0.36, null),
                        City(2, "Pau", "Catalogne", "Espagne", 42.3, 3.1, null),
                    ),
                ),
                favoriteIds = setOf(1),
            ),
            onQueryChange = {}, onRetry = {}, onCityClick = {}, onToggleFavorite = {},
            onMyLocationClick = {}, onLocationProblemShown = {}, onBack = {},
        )
    }
}
