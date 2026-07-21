package br.com.atlas.atlasapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import br.com.atlas.atlasapp.model.CheckIn
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CheckInRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val checkInsCollection = firestore.collection("checkIns")

    suspend fun createCheckIn(checkIn: CheckIn): Result<String> {
        return try {
            val documentRef = checkInsCollection.add(checkIn).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCheckInsByUser(userId: String): Result<List<CheckIn>> {
        return try {
            val snapshot = checkInsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val checkIns = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CheckIn::class.java)?.copy(id = doc.id)
            }
            Result.success(checkIns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCheckInsByRoute(routeId: String): Result<List<CheckIn>> {
        return try {
            val snapshot = checkInsCollection
                .whereEqualTo("routeId", routeId)
                .get()
                .await()
            val checkIns = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CheckIn::class.java)?.copy(id = doc.id)
            }
            Result.success(checkIns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCheckInsByRoutePoint(pointId: String): Result<List<CheckIn>> {
        return try {
            val snapshot = checkInsCollection
                .whereEqualTo("pointId", pointId)
                .get()
                .await()
            val checkIns = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CheckIn::class.java)?.copy(id = doc.id)
            }
            Result.success(checkIns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserCheckInCount(userId: String): Result<Int> {
        return try {
            val checkInsResult = getCheckInsByUser(userId)
            if (checkInsResult.isSuccess) {
                Result.success(checkInsResult.getOrNull()?.size ?: 0)
            } else {
                Result.failure(Exception("Erro ao buscar check-ins"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCheckInsByUserAndRoute(userId: String, routeId: String): Result<List<CheckIn>> {
        return try {
            val snapshot = checkInsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("routeId", routeId)
                .get()
                .await()
            val checkIns = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CheckIn::class.java)?.copy(id = doc.id)
            }
            Result.success(checkIns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
