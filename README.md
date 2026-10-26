# Nimbus

Application météo Android native (Kotlin, Jetpack Compose) : recherche de ville, météo actuelle, prévisions heure par heure et sur 7 jours, villes favorites, fonctionnement hors ligne.

Données météo fournies par l'API gratuite [Open-Meteo](https://open-meteo.com/) (aucune clé nécessaire).

## Ce que je dois faire

Développer, avec Android Studio, un prototype fonctionnel de l'application :

1. **Rechercher une ville** par son nom, avec des suggestions pendant la saisie.
2. **Afficher la météo actuelle** de la ville choisie : température, ressenti, description, icône, vent, humidité.
3. **Afficher les prévisions** heure par heure sur 24 h et jour par jour sur 7 jours (min / max, probabilité de pluie).
4. **Gérer des villes favorites** : ajouter, supprimer, réordonner, passer de l'une à l'autre.
5. **Utiliser la position du téléphone** (avec autorisation) pour afficher la météo locale.
6. **Fonctionner hors ligne** : garder en cache les dernières données, afficher leur date de mise à jour et un message clair en cas d'erreur réseau.
7. **Proposer des réglages** : unités (°C / °F, km/h / m/s), thème clair / sombre.
8. **En option** : actualisation par glisser vers le bas, widget d'écran d'accueil, fond qui change selon le temps, notification d'alerte pluie.
9. **Livrer** : code propre sur ce dépôt, quelques tests unitaires, captures d'écran dans ce README et un APK téléchargeable.

## Documentation

- [Cahier des charges](docs/CAHIER_DES_CHARGES.md)
- [Architecture technique](docs/ARCHITECTURE.md)
