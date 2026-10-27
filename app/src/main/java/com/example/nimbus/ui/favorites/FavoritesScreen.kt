package com.example.nimbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.R
import com.example.nimbus.domain.model.City
import com.example.nimbus.ui.components.LoadingState
import com.example.nimbus.ui.theme.NimbusTheme

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onCitySelected: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.citySelected.collect { onCitySelected() }
    }
    FavoritesScreen(
        state = state,
        onCityClick = viewModel::onCityClick,
        onDelete = viewModel::onDelete,
        onUndoDelete = viewModel::onUndoDelete,
        onDeleteMessageShown = viewModel::onDeleteMessageShown,
        onMoveUp = viewModel::onMoveUp,
        onMoveDown = viewModel::onMoveDown,
        onSearchClick = onSearchClick,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onCityClick: (City) -> Unit,
    onDelete: (City) -> Unit,
    onUndoDelete: () -> Unit,
    onDeleteMessageShown: () -> Unit,
    onMoveUp: (City) -> Unit,
    onMoveDown: (City) -> Unit,
    onSearchClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = state.recentlyDeleted?.let { stringResource(R.string.favorite_deleted, it.name) }
    val undoLabel = stringResource(R.string.undo)

    // Suppression : message avec "Annuler"
    LaunchedEffect(deletedMessage) {
        if (deletedMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onUndoDelete() else onDeleteMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
            state.isLoading -> LoadingState(contentModifier)
            state.favorites.isEmpty() -> EmptyFavorites(onSearchClick, contentModifier)
            else -> LazyColumn(modifier = contentModifier) {
                itemsIndexed(state.favorites, key = { _, city -> city.id }) { index, city ->
                    SwipeToDelete(onDelete = { onDelete(city) }) {
                        FavoriteRow(
                            city = city,
                            canMoveUp = index > 0,
                            canMoveDown = index < state.favorites.lastIndex,
                            onClick = { onCityClick(city) },
                            onMoveUp = { onMoveUp(city) },
                            onMoveDown = { onMoveDown(city) },
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

/** Glisser la ligne vers la gauche ou la droite pour la supprimer. */
@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        content()
    }
}

@Composable
private fun FavoriteRow(
    city: City,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(city.name) },
        supportingContent = { Text(listOfNotNull(city.region, city.country).joinToString(", ")) },
        trailingContent = {
            Row {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.move_up, city.name))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.move_down, city.name))
                }
            }
        },
    )
}

@Composable
private fun EmptyFavorites(onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.favorites_empty),
            textAlign = TextAlign.Center,
        )
        Button(onClick = onSearchClick) {
            Text(stringResource(R.string.search_title))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    NimbusTheme {
        FavoritesScreen(
            state = FavoritesUiState(
                favorites = listOf(
                    City(1, "Paris", "Île-de-France", "France", 48.85, 2.35, null),
                    City(2, "Pau", "Nouvelle-Aquitaine", "France", 43.3, -0.36, null),
                ),
                isLoading = false,
            ),
            onCityClick = {}, onDelete = {}, onUndoDelete = {}, onDeleteMessageShown = {},
            onMoveUp = {}, onMoveDown = {}, onSearchClick = {}, onBack = {},
        )
    }
}
