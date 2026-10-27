package com.example.nimbus.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.nimbus.domain.model.LocationResult
import com.example.nimbus.domain.repository.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Position du téléphone via les services Google (Fused Location Provider). */
class FusedLocationProvider(
    private val context: Context,
) : LocationProvider {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(): LocationResult {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return LocationResult.PermissionDenied

        val locationManager = context.getSystemService(LocationManager::class.java)
        if (locationManager == null || !LocationManagerCompat.isLocationEnabled(locationManager)) {
            return LocationResult.LocationDisabled
        }

        return try {
            val location = client.awaitCurrentLocation()
            if (location != null) {
                LocationResult.Success(location.latitude, location.longitude)
            } else {
                LocationResult.Unavailable
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LocationResult.Unavailable
        }
    }

    /**
     * Les services Google renvoient une "Task" (callbacks) : on la transforme en fonction suspend.
     * L'autorisation est vérifiée dans getCurrentLocation() juste avant l'appel.
     */
    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.awaitCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }
}
