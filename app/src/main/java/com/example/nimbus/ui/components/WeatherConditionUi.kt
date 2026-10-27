package com.example.nimbus.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dehaze
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nimbus.R
import com.example.nimbus.domain.model.WeatherCondition

/** Libellé de la condition, pris dans strings.xml (français ou anglais selon le téléphone). */
@StringRes
fun WeatherCondition.labelRes(): Int = when (this) {
    WeatherCondition.CLEAR -> R.string.weather_clear
    WeatherCondition.MAINLY_CLEAR -> R.string.weather_mainly_clear
    WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
    WeatherCondition.OVERCAST -> R.string.weather_overcast
    WeatherCondition.FOG -> R.string.weather_fog
    WeatherCondition.DRIZZLE -> R.string.weather_drizzle
    WeatherCondition.FREEZING_DRIZZLE -> R.string.weather_freezing_drizzle
    WeatherCondition.RAIN_LIGHT -> R.string.weather_rain_light
    WeatherCondition.RAIN_MODERATE -> R.string.weather_rain_moderate
    WeatherCondition.RAIN_HEAVY -> R.string.weather_rain_heavy
    WeatherCondition.FREEZING_RAIN -> R.string.weather_freezing_rain
    WeatherCondition.SNOW -> R.string.weather_snow
    WeatherCondition.SNOW_GRAINS -> R.string.weather_snow_grains
    WeatherCondition.RAIN_SHOWERS -> R.string.weather_rain_showers
    WeatherCondition.SNOW_SHOWERS -> R.string.weather_snow_showers
    WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
    WeatherCondition.THUNDERSTORM_HAIL -> R.string.weather_thunderstorm_hail
    WeatherCondition.UNKNOWN -> R.string.weather_unknown
}

/** Icône associée à la condition. */
fun WeatherCondition.icon(): ImageVector = when (this) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> Icons.Outlined.WbSunny
    WeatherCondition.PARTLY_CLOUDY -> Icons.Outlined.WbCloudy
    WeatherCondition.OVERCAST -> Icons.Outlined.Cloud
    WeatherCondition.FOG -> Icons.Outlined.Dehaze
    WeatherCondition.DRIZZLE, WeatherCondition.FREEZING_DRIZZLE -> Icons.Outlined.Grain
    WeatherCondition.RAIN_LIGHT, WeatherCondition.RAIN_MODERATE, WeatherCondition.RAIN_HEAVY,
    WeatherCondition.FREEZING_RAIN, WeatherCondition.RAIN_SHOWERS -> Icons.Outlined.Umbrella
    WeatherCondition.SNOW, WeatherCondition.SNOW_GRAINS, WeatherCondition.SNOW_SHOWERS -> Icons.Outlined.AcUnit
    WeatherCondition.THUNDERSTORM, WeatherCondition.THUNDERSTORM_HAIL -> Icons.Outlined.FlashOn
    WeatherCondition.UNKNOWN -> Icons.Outlined.QuestionMark
}

/** Icône météo réutilisable, avec sa description pour l'accessibilité. */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Icon(
        imageVector = condition.icon(),
        contentDescription = stringResource(condition.labelRes()),
        modifier = modifier.size(size),
    )
}
