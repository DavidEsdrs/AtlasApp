package br.com.atlas.atlasapp.data.repository

import br.com.atlas.atlasapp.model.Route
import br.com.atlas.atlasapp.model.RouteCategory
import br.com.atlas.atlasapp.model.RoutePoint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RouteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val routesCollection = firestore.collection("routes")

    suspend fun createRoute(route: Route): Result<String> {
        return try {
            val documentRef = routesCollection.add(route).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouteById(routeId: String): Result<Route> {
        return try {
            val document = routesCollection.document(routeId).get().await()
            val route = document.toSafeRoute()
            if (route != null) {
                Result.success(route)
            } else {
                Result.failure(Exception("Rota não encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRoutes(): Result<List<Route>> {
        return try {
            val snapshot = routesCollection.get().await()
            val routes = snapshot.documents.mapNotNull { it.toSafeRoute() }
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoutesByCategory(category: String): Result<List<Route>> {
        return try {
            val snapshot = routesCollection
                .whereEqualTo("category", category)
                .get()
                .await()
            val routes = snapshot.documents.mapNotNull { it.toSafeRoute() }
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoute(routeId: String, route: Route): Result<Unit> {
        return try {
            routesCollection.document(routeId).set(route).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoute(routeId: String): Result<Unit> {
        return try {
            routesCollection.document(routeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoutesByCreator(creatorId: String): Result<List<Route>> {
        return try {
            val snapshot = routesCollection
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
            val routes = snapshot.documents.mapNotNull { it.toSafeRoute() }
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toSafeRoute(): Route? {
        val raw = data ?: return null

        val parsedCategory = parseCategory(raw["category"] as? String)
        val parsedPoints = parsePoints(raw["points"])
        val parsedImageUrl = parseRouteImageUrl(raw, parsedPoints)

        return Route(
            id = id,
            creatorId = raw["creatorId"] as? String ?: "",
            title = raw["title"] as? String ?: "",
            description = raw["description"] as? String ?: "",
            category = parsedCategory,
            imageUrl = parsedImageUrl,
            points = parsedPoints,
            estimatedDurationMinutes = (raw["estimatedDurationMinutes"] as? Number)?.toInt() ?: 0,
            rating = (raw["rating"] as? Number)?.toDouble() ?: 0.0,
            totalRatings = (raw["totalRatings"] as? Number)?.toInt() ?: 0,
            createdAt = (raw["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }

    private fun parseCategory(rawCategory: String?): RouteCategory {
        if (rawCategory.isNullOrBlank()) return RouteCategory.HISTORICAL

        return try {
            RouteCategory.valueOf(rawCategory.uppercase())
        } catch (_: IllegalArgumentException) {
            RouteCategory.HISTORICAL
        }
    }

    private fun parsePoints(rawPoints: Any?): List<RoutePoint> {
        val pointMaps = rawPoints as? List<*> ?: return emptyList()

        return pointMaps.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            RoutePoint(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                imageUrl = map["imageUrl"] as? String,
                order = (map["order"] as? Number)?.toInt() ?: 0
            )
        }
    }

    private fun parseRouteImageUrl(raw: Map<String, Any>, points: List<RoutePoint>): String? {
        val routeLevelImage = (raw["imageUrl"] as? String)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (routeLevelImage != null) return routeLevelImage

        return points
            .sortedBy { it.order }
            .firstNotNullOfOrNull { it.imageUrl }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
