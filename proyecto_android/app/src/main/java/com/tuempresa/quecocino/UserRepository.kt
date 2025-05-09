package com.tuempresa.quecocino.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {

    private val users = FirebaseFirestore.getInstance().collection("users")

    /**
     * Crea / actualiza el documento del usuario y
     * devuelve los datos almacenados.
     */
    suspend fun upsertUser(user: FirebaseUser): Map<String, Any?> {
        val doc = users.document(user.uid)
        val snap = doc.get().await()

        val data = mapOf(
            "uid"       to user.uid,
            "name"      to (user.displayName ?: ""),
            "email"     to (user.email ?: ""),
            "photoUrl"  to (user.photoUrl?.toString() ?: ""),
            "createdAt" to Timestamp.now()
        )

        if (!snap.exists()) doc.set(data).await()
        return snap.data ?: data          // ← lo que haya quedado en Firestore
    }
}
