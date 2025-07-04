package com.tuempresa.quecocino.data

import android.app.Application
import com.google.firebase.database.*
import com.google.gson.Gson
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        val database = FirebaseDatabase.getInstance().reference

        // Lista de nodos que quieres cachear completo
        val nodosParaCachear = listOf("Platos", "Bebidas")

        for (nodo in nodosParaCachear) {
            database.child(nodo).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) { /* Solo recorrer para cachear */ }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Si tienes más nodos o colecciones, agrega aquí
    }
}



object RecetaRepository {
    private val database = FirebaseDatabase.getInstance().reference

    fun cargarPlatosYBebidas(
        onSuccess: (PlatosYBebidas) -> Unit,
        onError: (Exception) -> Unit
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val gson = Gson()
                    val platos = snapshot.child("Platos").children.mapNotNull { child ->
                        val receta = gson.fromJson(gson.toJson(child.value), Receta::class.java)
                        receta?.copy(id = child.key ?: "")
                    }
                    val bebidas = snapshot.child("Bebidas").children.mapNotNull { child ->
                        val bebida = gson.fromJson(gson.toJson(child.value), Bebida::class.java)
                        bebida?.copy(id = child.key ?: "")
                    }
                    onSuccess(PlatosYBebidas(platos, bebidas))
                } catch (e: Exception) {
                    Log.e("RecetaRepository", "Parse error", e)
                    onError(e)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("RecetaRepository", "DB load cancelled", error.toException())
                onError(error.toException())
            }
        })
    }

    fun cargarRecetas(
        onResult: (List<Receta>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        cargarPlatosYBebidas(
            onSuccess = { onResult(it.platos) },
            onError = { onError(it) }
        )
    }

    suspend fun getRecetaById(id: String): Receta? = suspendCancellableCoroutine { cont ->
        database.child("Platos").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val json = Gson().toJson(snapshot.value)
                    val receta = Gson().fromJson(json, Receta::class.java)
                        ?.copy(id = snapshot.key ?: "")
                    cont.resume(receta)
                }
                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(error.toException())
                }
            })
    }

    suspend fun getBebidaById(id: String): Bebida? = suspendCancellableCoroutine { cont ->
        database.child("Bebidas").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val json = Gson().toJson(snapshot.value)
                    val bebida = Gson().fromJson(json, Bebida::class.java)
                        ?.copy(id = snapshot.key ?: "")
                    cont.resume(bebida)
                }
                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(error.toException())
                }
            })
    }
}
