# Architecture technique – Nimbus

## Principes

- **MVVM + flux de données unidirectionnel** : chaque écran observe un `UiState` (StateFlow) exposé par son ViewModel et lui envoie des événements.
- **Couches séparées** : `ui` → `domain` → `data`. L'UI ne connaît ni Retrofit ni Room.
- **Cache d'abord (offline-first)** : le repository émet d'abord les données locales, lance un rafraîchissement réseau, enregistre le résultat, et Room réémet la nouvelle valeur.
- **Injection de dépendances** avec Hilt.

```
┌─────────────── ui ───────────────┐
│ Composables  ←  UiState           │
│      │ événements    ▲            │
│      ▼               │            │
│           ViewModel               │
└────────────────┬─────────────────┘
                 ▼
┌───────────── domain ─────────────┐
│ UseCases : SearchCities,          │
│ GetWeather, Add/Remove/Reorder    │
│ Favorites, GetCurrentLocation     │
│ Modèles : City, Weather, Forecast │
└────────────────┬─────────────────┘
                 ▼
┌────────────── data ──────────────┐
│ WeatherRepository  CityRepository │
│   ├─ remote : API Open-Meteo      │
│   ├─ local  : Room (favoris,cache)│
│   └─ DataStore (réglages)         │
│ LocationProvider (Fused Location) │
└──────────────────────────────────┘
```

## Arborescence des packages

```
app/src/main/java/com/example/nimbus/
├── NimbusApp.kt                    # @HiltAndroidApp
├── MainActivity.kt
├── ui/
│   ├── navigation/NimbusNavHost.kt
│   ├── theme/
│   ├── weather/                    # WeatherScreen, WeatherViewModel, WeatherUiState
│   ├── search/                     # SearchScreen, SearchViewModel (debounce)
│   ├── favorites/                  # FavoritesScreen, FavoritesViewModel
│   ├── settings/                   # SettingsScreen, SettingsViewModel
│   └── components/                 # CurrentWeatherCard, HourlyRow, DailyList,
│                                   # WeatherIcon, ErrorState, LoadingState
├── domain/
│   ├── model/                      # City, CurrentWeather, HourlyForecast,
│   │                               # DailyForecast, WeatherCondition, Units
│   ├── usecase/
│   └── WeatherCodeMapper.kt        # code WMO → condition (libellé + icône)
├── data/
│   ├── remote/
│   │   ├── GeocodingApi.kt         # interface Retrofit
│   │   ├── ForecastApi.kt
│   │   └── dto/                    # classes @Serializable de la réponse JSON
│   ├── local/
│   │   ├── NimbusDatabase.kt
│   │   ├── FavoriteCityDao.kt
│   │   ├── WeatherCacheDao.kt
│   │   └── entity/
│   ├── preferences/SettingsRepository.kt   # DataStore
│   ├── location/LocationProvider.kt
│   ├── repository/                 # WeatherRepositoryImpl, CityRepositoryImpl
│   └── mapper/                     # DTO ↔ Entity ↔ modèle domaine
└── di/                             # NetworkModule, DatabaseModule, RepositoryModule
```

## Bibliothèques (prendre les dernières versions stables)

- AndroidX Core KTX, Lifecycle (ViewModel, runtime-compose)
- Jetpack Compose (BOM), Material 3, Material Icons Extended
- Navigation Compose
- Retrofit + OkHttp (logging interceptor en debug) **ou** Ktor Client
- kotlinx.serialization (convertisseur JSON)
- Room (runtime, ktx, compiler via KSP)
- Hilt (+ hilt-navigation-compose)
- DataStore Preferences
- Google Play Services Location (FusedLocationProviderClient)
- Optionnel : WorkManager (alerte pluie), Glance (widget)
- Tests : JUnit 4, kotlinx-coroutines-test, Turbine, MockK, MockWebServer, Compose UI Test

Utiliser un **version catalog** (`gradle/libs.versions.toml`) et **KSP**.

## Gestion des erreurs

- Le repository renvoie un type résultat (`sealed interface` : `Success`, `Error` avec cause réseau / serveur / format) au lieu de laisser remonter les exceptions.
- Le `UiState` porte : `isLoading`, données, `lastUpdated`, `error` éventuelle. Si une erreur survient alors que des données en cache existent, on garde les données et on affiche un message discret (Snackbar).

## Points d'attention

- **Recherche** : `debounce(300)` + `distinctUntilChanged()` + `flatMapLatest` sur le texte saisi pour annuler les requêtes obsolètes.
- **Localisation** : demander la permission au moment du besoin (`rememberLauncherForActivityResult`), gérer le refus et la localisation désactivée.
- **Fuseaux horaires** : utiliser `timezone=auto` côté API et `java.time` (`ZoneId`) pour l'affichage.
- **Rafraîchissement** : ne pas rappeler l'API si le cache a moins de 30 min, sauf actualisation manuelle.
- **Rotation / arrêt du processus** : l'état reste dans le ViewModel ; l'id de la ville passe par `SavedStateHandle`.
- **Crédit** : mentionner Open-Meteo dans l'écran « À propos » et le README.

## Tests

| Cible | Outil | Exemples |
|---|---|---|
| Conversion des codes météo | JUnit | code 61 → « Pluie faible » |
| Mappers DTO → domaine | JUnit | parsing d'une réponse JSON d'exemple |
| API | MockWebServer | réponse 200, 500, JSON invalide, délai dépassé |
| Repository | JUnit + MockK | cache récent → pas d'appel réseau ; erreur réseau → cache conservé |
| ViewModels | coroutines-test + Turbine | recherche avec debounce, états chargement / erreur |
| DAO | Room en mémoire (androidTest) | ajout, suppression, réordonnancement des favoris |
| UI | Compose UI Test | affichage de l'état d'erreur et du bouton « Réessayer » |
