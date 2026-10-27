package com.example.nimbus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nimbus.ui.favorites.FavoritesRoute
import com.example.nimbus.ui.search.SearchRoute
import com.example.nimbus.ui.settings.SettingsRoute
import com.example.nimbus.ui.weather.WeatherRoute
import kotlinx.serialization.Serializable

/**
 * Les écrans de l'app. Chaque destination est une classe @Serializable :
 * la navigation est vérifiée à la compilation (pas de chaînes "weather", "search"…).
 */
@Serializable object WeatherDestination
@Serializable object SearchDestination
@Serializable object FavoritesDestination
@Serializable object SettingsDestination

@Composable
fun NimbusNavHost(navController: NavHostController = rememberNavController()) {
    /** Retour à l'écran météo, quel que soit le chemin (Favoris → Recherche → ville). */
    val backToWeather: () -> Unit = {
        navController.popBackStack<WeatherDestination>(inclusive = false)
    }

    NavHost(navController = navController, startDestination = WeatherDestination) {
        composable<WeatherDestination> {
            WeatherRoute(
                onSearchClick = { navController.navigate(SearchDestination) },
                onFavoritesClick = { navController.navigate(FavoritesDestination) },
                onSettingsClick = { navController.navigate(SettingsDestination) },
            )
        }
        composable<SearchDestination> {
            SearchRoute(
                onBack = { navController.navigateUp() },
                onCitySelected = backToWeather,
            )
        }
        composable<FavoritesDestination> {
            FavoritesRoute(
                onBack = { navController.navigateUp() },
                onCitySelected = backToWeather,
                onSearchClick = { navController.navigate(SearchDestination) },
            )
        }
        composable<SettingsDestination> {
            SettingsRoute(onBack = { navController.navigateUp() })
        }
    }
}
