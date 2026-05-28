package br.com.atlas.atlasapp.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,

    val points: Int = 0,
    val badges: List<String> = emptyList(),

    val createdRoutes: List<String> = emptyList(),
    val completedRoutes: List<String> = emptyList()
)