package br.com.atlas.atlasapp.data.repository

import br.com.atlas.atlasapp.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    suspend fun createReview(review: Review): Result<String> {
        return try {
            val documentRef = reviewsCollection.add(review).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsByRoute(routeId: String): Result<List<Review>> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("routeId", routeId)
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAverageRatingByRoute(routeId: String): Result<Double> {
        return try {
            val reviewsResult = getReviewsByRoute(routeId)
            if (reviewsResult.isSuccess) {
                val reviews = reviewsResult.getOrNull() ?: emptyList()
                val average = if (reviews.isNotEmpty()) {
                    reviews.map { it.rating }.average()
                } else {
                    0.0
                }
                Result.success(average)
            } else {
                Result.failure(Exception("Erro ao buscar reviews"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
