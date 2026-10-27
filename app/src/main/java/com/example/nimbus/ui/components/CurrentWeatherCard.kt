package com.example.nimbus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.WeatherCondition
import com.example.nimbus.ui.theme.NimbusTheme

/** Bloc principal : icône, grosse température, description, ressenti / min / max. */
@Composable
fun CurrentWeatherCard(
    temperature: String,
    condition: WeatherCondition,
    feelsLike: String,
    min: String,
    max: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WeatherIcon(condition = condition, size = 64.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = temperature,
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = stringResource(condition.labelRes()),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.feels_like_min_max, feelsLike, min, max),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentWeatherCardPreview() {
    NimbusTheme {
        CurrentWeatherCard(
            temperature = "18°",
            condition = WeatherCondition.PARTLY_CLOUDY,
            feelsLike = "16°",
            min = "12°",
            max = "20°",
        )
    }
}
