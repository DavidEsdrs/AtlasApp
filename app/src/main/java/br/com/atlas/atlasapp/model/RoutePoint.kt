package br.com.atlas.atlasapp.model

data class RoutePoint(
    val id: String = "",

    val title: String = "",
    val description: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val imageUrl: String? = null,

    val order: Int = 0
)