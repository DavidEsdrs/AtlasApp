package br.com.atlas.atlasapp.data.repository

import br.com.atlas.atlasapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPointsToUser(userId: String, points: Int): Result<Unit> {
        return try {
            val userResult = getUserById(userId)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val updatedUser = user.copy(points = user.points + points)
                usersCollection.document(userId).set(updatedUser).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBadgeToUser(userId: String, badgeId: String): Result<Unit> {
        return try {
            val userResult = getUserById(userId)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                if (!user.badges.contains(badgeId)) {
                    val updatedUser = user.copy(badges = user.badges + badgeId)
                    usersCollection.document(userId).set(updatedUser).await()
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCompletedRoute(userId: String, routeId: String): Result<Unit> {
        return try {
            val userResult = getUserById(userId)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                if (!user.completedRoutes.contains(routeId)) {
                    val updatedUser = user.copy(completedRoutes = user.completedRoutes + routeId)
                    usersCollection.document(userId).set(updatedUser).await()
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
