package com.example.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.ui.weather.WeatherScreen
import com.example.nimbus.ui.weather.sampleWeatherUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NimbusTheme {
                // Temporaire : fausses données. Plus tard, NimbusNavHost() + ViewModel.
                WeatherScreen(
                    state = sampleWeatherUiState,
                    onSearchClick = {},
                    onSettingsClick = {},
                )
            }
        }
    }
}
