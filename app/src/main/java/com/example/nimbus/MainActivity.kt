package com.example.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.ui.weather.WeatherRoute
import dagger.hilt.android.AndroidEntryPoint

/** @AndroidEntryPoint : Hilt peut fournir les ViewModels aux écrans de cette activité. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NimbusTheme {
                // Plus tard : NimbusNavHost() pour passer d'un écran à l'autre
                WeatherRoute()
            }
        }
    }
}
