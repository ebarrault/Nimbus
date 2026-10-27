package com.example.nimbus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import com.example.nimbus.ui.weather.DailyUi
import com.example.nimbus.ui.weather.sampleDaily

/**
 * Carte "7 prochains jours".
 * Une simple Column suffit (7 lignes) : pas besoin de LazyColumn,
 * et l'écran entier est déjà une LazyColumn.
 */
@Composable
fun DailyList(
    days: List<DailyUi>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.daily_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            days.forEachIndexed { index, day ->
                DailyItem(day)
                if (index < days.lastIndex) HorizontalDivider()
            }
        }
    }
}

/** Un jour : nom, icône, risque de pluie, min / max. */
@Composable
private fun DailyItem(
    day: DailyUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (day.isToday) stringResource(R.string.today) else day.dayLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(96.dp),
        )
        WeatherIcon(condition = day.condition)
        Text(
            text = day.rainChance,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp),
        )
        Spacer(Modifier.weight(1f)) // pousse le min / max à droite
        Text(
            text = stringResource(R.string.min_max, day.min, day.max),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyListPreview() {
    NimbusTheme {
        DailyList(days = sampleDaily)
    }
}
