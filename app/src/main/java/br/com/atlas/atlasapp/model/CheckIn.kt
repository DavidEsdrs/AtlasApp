package br.com.atlas.atlasapp.model

data class CheckIn(
    val id: String = "",

    val userId: String = "",
    val routeId: String = "",
    val pointId: String = "",

    val timestamp: Long = System.currentTimeMillis()
)