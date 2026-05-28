package br.com.atlas.atlasapp.model

data class Review(
    val id: String = "",

    val routeId: String = "",
    val userId: String = "",

    val rating: Int = 0,
    val comment: String = "",

    val createdAt: Long = System.currentTimeMillis()
)