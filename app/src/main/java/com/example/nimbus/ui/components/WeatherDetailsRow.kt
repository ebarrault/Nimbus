package com.example.nimbus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.nimbus.R
import com.example.nimbus.ui.theme.NimbusTheme

/** Ligne "Vent / Humidité" sous la température. */
@Composable
fun WeatherDetailsRow(
    wind: String,
    humidity: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        DetailItem(Icons.Outlined.Air, stringResource(R.string.detail_wind), wind)
        DetailItem(Icons.Outlined.WaterDrop, stringResource(R.string.detail_humidity), humidity)
    }
}

/** Un détail : icône, valeur, libellé. Privé car utilisé seulement ici. */
@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null) // le libellé est déjà affiché
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherDetailsRowPreview() {
    NimbusTheme {
        WeatherDetailsRow(wind = "14 km/h", humidity = "62 %")
    }
}
