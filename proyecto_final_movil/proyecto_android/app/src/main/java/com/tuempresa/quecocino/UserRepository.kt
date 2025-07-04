package com.tuempresa.quecocino.data

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ActivityEntry(
    val itemId: String = "",
    val type: String = "",       // "Receta" o "Bebida"
    val action: String = "",     // "VISTA" o "FAVORITO"
    val timestamp: Timestamp? = null
)

object UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    // 1. Registro inicial de usuario
    suspend fun upsertUser(user: FirebaseUser) {
        val doc = users.document(user.uid)
        if (!doc.get().await().exists()) {
            doc.set(mapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "createdAt" to Timestamp.now()
            )).await()
        }
    }

    // 2. Favoritos – Recetas
    suspend fun addFavoriteRecipe(uid: String, recipeId: String) {
        users.document(uid)
            .collection("favorites_recipes")
            .document(recipeId)
            .set(mapOf("addedAt" to Timestamp.now()))
            .await()
        recordActivity(uid, recipeId, "Receta", "FAVORITO")
    }

    suspend fun removeFavoriteRecipe(uid: String, recipeId: String) {
        users.document(uid)
            .collection("favorites_recipes")
            .document(recipeId)
            .delete()
            .await()
    }

    suspend fun isRecipeFavorite(uid: String, recipeId: String): Boolean =
        users.document(uid)
            .collection("favorites_recipes")
            .document(recipeId)
            .get().await().exists()

    suspend fun getFavoritesRecipes(uid: String): List<String> =
        users.document(uid)
            .collection("favorites_recipes")
            .get().await()
            .documents.map { it.id }

    suspend fun getFavoritesRecipesCount(uid: String): Int =
        users.document(uid)
            .collection("favorites_recipes")
            .get().await().size()

    // 3. Favoritos – Bebidas
    suspend fun addFavoriteBeverage(uid: String, beverageId: String) {
        users.document(uid)
            .collection("favorites_beverages")
            .document(beverageId)
            .set(mapOf("addedAt" to Timestamp.now()))
            .await()
        recordActivity(uid, beverageId, "Bebida", "FAVORITO")
    }

    suspend fun removeFavoriteBeverage(uid: String, beverageId: String) {
        users.document(uid)
            .collection("favorites_beverages")
            .document(beverageId)
            .delete()
            .await()
    }

    suspend fun isBeverageFavorite(uid: String, beverageId: String): Boolean =
        users.document(uid)
            .collection("favorites_beverages")
            .document(beverageId)
            .get().await().exists()

    suspend fun getFavoritesBeverages(uid: String): List<String> =
        users.document(uid)
            .collection("favorites_beverages")
            .get().await()
            .documents.map { it.id }

    suspend fun getFavoritesBeveragesCount(uid: String): Int =
        users.document(uid)
            .collection("favorites_beverages")
            .get().await().size()

    // 4. Registro de actividad (vistas o favoritos)
    private suspend fun recordActivity(uid: String, itemId: String, type: String, action: String) {
        users.document(uid)
            .collection("activity")
            .add(mapOf(
                "itemId" to itemId,
                "type" to type,
                "action" to action,
                "timestamp" to FieldValue.serverTimestamp()
            )).await()
    }

    suspend fun recordView(uid: String, itemId: String, type: String) {
        recordActivity(uid, itemId, type, "VISTA")
    }

    suspend fun getRecentActivity(uid: String, limit: Long = 10): List<ActivityEntry> {
        val snapshot = users.document(uid)
            .collection("activity")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(ActivityEntry::class.java) }
    }
}
