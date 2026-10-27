package com.example.nimbus.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.nimbus.R
import com.example.nimbus.ui.theme.NimbusTheme

/** "Mis à jour il y a 5 min", discret, en bas de l'écran. */
@Composable
fun LastUpdatedText(
    lastUpdated: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.last_updated, lastUpdated),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun LastUpdatedTextPreview() {
    NimbusTheme {
        LastUpdatedText(lastUpdated = "il y a 5 min")
    }
}
