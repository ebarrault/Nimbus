# Cahier des charges – Nimbus

## 1. Contexte

Nimbus est une application météo Android native. Le sujet est volontairement classique : il permet de couvrir les briques essentielles d'une app mobile moderne (appels réseau, cache local, permissions, gestion des erreurs, interface soignée) dans un périmètre maîtrisé.

## 2. Objectifs

- **Utilisateur** : connaître en un coup d'œil le temps qu'il fait et qu'il fera, ici ou dans ses villes favorites, même avec une connexion instable.
- **Projet** : produire un prototype propre en Kotlin / Jetpack Compose, avec une architecture claire et testée.

## 3. Périmètre

### Inclus (version de base)
- Recherche de ville avec suggestions
- Météo actuelle + prévisions 24 h et 7 jours
- Villes favorites
- Météo de la position actuelle
- Cache hors ligne
- Réglages des unités et du thème

### Optionnel
- Actualisation par glisser vers le bas
- Widget d'écran d'accueil (ville principale)
- Fond / animation selon la météo
- Notification « pluie prévue dans l'heure » (tâche planifiée)

### Exclu
- Compte utilisateur, serveur propre
- Cartes radar
- Version iOS

## 4. Source de données

API Open-Meteo, gratuite, sans clé, en HTTPS :

- **Géocodage** (recherche de ville) : `https://geocoding-api.open-meteo.com/v1/search?name={texte}&count=10&language=fr&format=json`
  → nom, pays, région, latitude, longitude, fuseau horaire.
- **Prévisions** : `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day&hourly=temperature_2m,precipitation_probability,weather_code&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code,sunrise,sunset&timezone=auto&forecast_days=7`
- Le champ `weather_code` (codes WMO) est converti en libellé français et en icône par l'application.
- Unités : paramètres `temperature_unit` et `wind_speed_unit` de l'API, selon les réglages.

## 5. User stories

| ID | En tant qu'utilisateur, je veux… | afin de… | Priorité |
|---|---|---|---|
| US1 | chercher une ville par son nom et voir des suggestions | trouver la bonne ville (homonymes : afficher région et pays) | Base |
| US2 | voir la météo actuelle d'une ville | savoir le temps qu'il fait maintenant | Base |
| US3 | voir les prévisions sur 24 h et sur 7 jours | m'organiser | Base |
| US4 | ajouter une ville en favori et la retrouver au lancement | ne pas la rechercher à chaque fois | Base |
| US5 | supprimer et réordonner mes favoris | garder une liste utile | Base |
| US6 | voir la météo de ma position | avoir la météo locale sans rien taper | Base |
| US7 | consulter les dernières données sans connexion, avec leur heure de mise à jour | être informé même hors réseau | Base |
| US8 | choisir mes unités et le thème | avoir un affichage qui me convient | Base |
| US9 | actualiser en glissant vers le bas | forcer une mise à jour | Optionnel |
| US10 | avoir un widget sur l'écran d'accueil | voir la météo sans ouvrir l'app | Optionnel |

## 6. Écrans

1. **Accueil / météo** – ville en titre, température en grand, description et icône, ressenti, vent, humidité, bande horizontale des prochaines 24 h, liste des 7 jours, heure de dernière mise à jour. Navigation entre favoris par swipe horizontal (pager) ou liste déroulante.
2. **Recherche** – champ texte avec délai avant requête (debounce ~300 ms), liste de suggestions « Ville, région, pays », bouton pour ajouter en favori.
3. **Favoris** – liste réordonnable par glisser-déposer, suppression par swipe avec possibilité d'annuler.
4. **Réglages** – unités de température et de vent, thème (système / clair / sombre), à propos (crédit Open-Meteo).

États à gérer sur chaque écran : chargement, contenu, vide, erreur (avec bouton « Réessayer »).

## 7. Données locales

**FavoriteCity**
| Champ | Type |
|---|---|
| id | Long (auto) |
| name, region, country | String |
| latitude, longitude | Double |
| timezone | String |
| position | Int (ordre d'affichage) |

**WeatherCache** (une ligne par ville)
| Champ | Type |
|---|---|
| cityId | Long (clé) |
| json ou colonnes du dernier résultat | String / champs |
| fetchedAt | Instant |

**Réglages** (DataStore) : unité de température, unité de vent, thème, dernière ville affichée.

## 8. Règles métier

- Les données en cache sont affichées immédiatement, puis rafraîchies en arrière-plan si elles ont plus de 30 minutes.
- Sans réseau : afficher le cache avec la mention « Mis à jour il y a X » ; s'il n'y a pas de cache, afficher un état d'erreur explicite.
- La recherche ne part qu'à partir de 2 caractères.
- La position n'est demandée qu'au moment où l'utilisateur choisit « Ma position » ; en cas de refus, l'app reste utilisable avec la recherche.
- Les heures sont affichées dans le fuseau de la ville consultée.

## 9. Exigences non fonctionnelles

- **Plateforme** : Android 8.0 (API 26) minimum, cible la dernière API stable.
- **Réseau** : HTTPS uniquement, délais d'attente définis, erreurs réseau et erreurs de format gérées sans crash.
- **Permissions** : `INTERNET`, `ACCESS_COARSE_LOCATION` (localisation approximative suffisante), `POST_NOTIFICATIONS` seulement pour l'option alerte pluie.
- **Performance** : premier affichage depuis le cache en moins d'une seconde ; pas d'appel réseau sur le thread principal.
- **Accessibilité** : descriptions sur les icônes météo, contrastes suffisants, police système respectée.
- **Langue** : français, chaînes dans `strings.xml`.
- **Confidentialité** : aucune donnée personnelle envoyée, seulement des coordonnées à l'API météo.
- **Qualité** : pas de logique dans les composables, tests unitaires sur la conversion des codes météo, le repository et les ViewModels.

## 10. Critères d'acceptation

- [ ] Je trouve une ville et j'affiche sa météo actuelle et ses prévisions 24 h / 7 jours.
- [ ] J'ajoute, supprime et réordonne des favoris ; ils sont toujours là après redémarrage.
- [ ] « Ma position » affiche la météo locale, et l'app reste utilisable si je refuse l'autorisation.
- [ ] En mode avion, je vois les dernières données avec leur heure de mise à jour.
- [ ] Changer d'unité met à jour l'affichage.
- [ ] Pas de crash à la rotation, en mode sombre, ni en cas d'erreur réseau.
- [ ] Au moins 10 tests unitaires passent (`./gradlew test`).

## 11. Livrables

- Dépôt GitHub public : code, README, cahier des charges, architecture
- Captures d'écran et courte vidéo / GIF dans le README
- APK de debug dans la section Releases
