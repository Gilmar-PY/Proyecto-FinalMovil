@file:OptIn(ExperimentalMaterial3Api::class)

package com.tuempresa.quecocino.ui.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.data.Bebida
import com.tuempresa.quecocino.data.Receta
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.data.UserRepository
import com.tuempresa.quecocino.ui.common.ImagenReceta
import kotlinx.coroutines.launch

@Composable
fun RecetaDetalleScreen(navController: NavController, recetaId: String) {
    val context = LocalContext.current
    var receta by remember { mutableStateOf<Receta?>(null) }
    var bebida by remember { mutableStateOf<Bebida?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(recetaId) {
        RecetaRepository.cargarPlatosYBebidas(
            onSuccess = { data ->
                receta = data.platos.find { it.id == recetaId }
                bebida = data.bebidas.find { it.id == recetaId }
                error = receta == null && bebida == null
                loading = false
            },
            onError = { e ->
                Log.e("RecetaDetalle", "Error al cargar datos", e)
                error = true
                loading = false
            }
        )
    }

    when {
        loading -> LoadingScreen()
        error   -> ErrorScreen()
        receta != null -> RecetaDetalleContent(receta!!, navController)
        bebida != null -> BebidaDetalleContent(bebida!!, navController)
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No se pudo cargar la receta.\nVerifica tu conexión o intenta de nuevo.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecetaDetalleContent(receta: Receta, navController: NavController) {
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var isFav by remember { mutableStateOf(false) }

    LaunchedEffect(receta.id) {
        isFav = UserRepository.isRecipeFavorite(uid, receta.id)
        UserRepository.recordView(uid, receta.id, "Receta")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receta.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (isFav)
                                UserRepository.removeFavoriteRecipe(uid, receta.id)
                            else
                                UserRepository.addFavoriteRecipe(uid, receta.id)
                            isFav = !isFav
                        }
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFav) "Quitar favorito" else "Marcar como favorito",
                            tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                ImagenReceta(
                    imagen = receta.imagen,
                    contentDescription = receta.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    "Receta para ${receta.porciones} persona${if (receta.porciones > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(receta.ingredientes_detalle) { ing ->
                Text("• $ing", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("Preparación", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(receta.pasos) { paso ->
                Text(paso, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
