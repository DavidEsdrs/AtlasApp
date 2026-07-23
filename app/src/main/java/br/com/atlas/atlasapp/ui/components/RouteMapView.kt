package br.com.atlas.atlasapp.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import br.com.atlas.atlasapp.model.RoutePoint
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@Composable
fun RouteMapView(
    points: List<RoutePoint>,
    highlightedPointIndex: Int? = null,
    modifier: Modifier = Modifier,
    enableMyLocation: Boolean = false,
    onMapClick: ((LatLng) -> Unit)? = null,
    pendingPoint: LatLng? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasLocationPermission by remember(enableMyLocation) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    DisposableEffect(enableMyLocation, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition(points)
    }

    LaunchedEffect(points) {
        val validPoints = points.filter { it.latitude != 0.0 || it.longitude != 0.0 }
        if (validPoints.isNotEmpty()) {
            cameraPositionState.moveToRouteBounds(validPoints)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = enableMyLocation && hasLocationPermission),
        uiSettings = MapUiSettings(myLocationButtonEnabled = true),
        onMapClick = { latLng -> onMapClick?.invoke(latLng) }
    ) {
        val validPoints = points.filter { it.latitude != 0.0 || it.longitude != 0.0 }

        if (validPoints.size >= 2) {
            Polyline(points = validPoints.map { LatLng(it.latitude, it.longitude) })
        }

        pendingPoint?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Novo ponto",
                icon = BitmapDescriptorFactory.fromBitmap(createCustomPin(AndroidColor.parseColor("#2D7FF9")))
            )
        }

        validPoints.forEachIndexed { index, point ->
            val isHighlighted = highlightedPointIndex == index
            val markerColor = when {
                isHighlighted -> Color(0xFF2D7FF9)
                index == 0 -> Color(0xFF2EAD4A)
                index == validPoints.lastIndex -> Color(0xFFE14B4B)
                else -> Color(0xFFF2A23A)
            }
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = point.title,
                snippet = point.description,
                icon = BitmapDescriptorFactory.fromBitmap(createCustomPin(markerColor.toArgb()))
            )
        }
    }
}

private fun defaultCameraPosition(points: List<RoutePoint>): CameraPosition {
    val firstPoint = points.firstOrNull { it.latitude != 0.0 || it.longitude != 0.0 }
    return if (firstPoint != null) {
        CameraPosition.fromLatLngZoom(LatLng(firstPoint.latitude, firstPoint.longitude), 13f)
    } else {
        CameraPosition.fromLatLngZoom(LatLng(-8.05, -34.9), 10f)
    }
}

private suspend fun CameraPositionState.moveToRouteBounds(points: List<RoutePoint>) {
    val boundsBuilder = LatLngBounds.Builder()
    points.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }

    val bounds = boundsBuilder.build()
    animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
}

private fun createCustomPin(pinColor: Int): Bitmap {
    val width = 96
    val height = 128
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = pinColor
        style = Paint.Style.FILL
    }

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val pinPath = Path().apply {
        moveTo(width / 2f, height - 8f)
        cubicTo(
            width * 0.18f, height * 0.62f,
            width * 0.12f, height * 0.34f,
            width / 2f, height * 0.16f
        )
        cubicTo(
            width * 0.88f, height * 0.34f,
            width * 0.82f, height * 0.62f,
            width / 2f, height - 8f
        )
        close()
    }

    canvas.drawPath(pinPath, outerPaint)
    canvas.drawPath(pinPath, strokePaint)
    canvas.drawCircle(width / 2f, height * 0.39f, 18f, innerPaint)

    return bitmap
}