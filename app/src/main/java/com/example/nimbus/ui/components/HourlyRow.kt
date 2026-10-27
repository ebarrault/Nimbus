package com.example.nimbus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.ui.theme.NimbusTheme
import com.example.nimbus.ui.weather.HourlyUi
import com.example.nimbus.ui.weather.sampleHourly

/** Carte "Prochaines 24 h" avec une bande horizontale qui défile. */
@Composable
fun HourlyRow(
    hours: List<HourlyUi>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.hourly_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(hours) { hour ->
                HourlyItem(hour)
            }
        }
    }
}

/** Une heure : libellé, icône, température. */
@Composable
private fun HourlyItem(
    hour: HourlyUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = hour.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WeatherIcon(condition = hour.condition)
        Text(text = hour.temperature, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun HourlyRowPreview() {
    NimbusTheme {
        HourlyRow(hours = sampleHourly)
    }
}
