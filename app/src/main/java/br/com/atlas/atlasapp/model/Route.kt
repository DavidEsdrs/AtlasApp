package br.com.atlas.atlasapp.model

data class Route(
    val id: String = "",
    val creatorId: String = "",

    val title: String = "",
    val description: String = "",

    val category: RouteCategory = RouteCategory.HISTORICAL,

    val points: List<RoutePoint> = emptyList(),

    val imageUrl: String? = null,

    val estimatedDurationMinutes: Int = 0,

    val rating: Double = 0.0,
    val totalRatings: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)