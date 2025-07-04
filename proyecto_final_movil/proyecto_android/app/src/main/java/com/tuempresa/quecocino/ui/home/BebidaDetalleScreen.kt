@file:OptIn(ExperimentalMaterial3Api::class)
package com.tuempresa.quecocino.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.data.Bebida
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.data.UserRepository
import com.tuempresa.quecocino.ui.common.ImagenReceta
import kotlinx.coroutines.launch

@Composable
fun BebidaDetalleScreen(navController: NavController, bebidaId: String) {
    var bebida by remember { mutableStateOf<Bebida?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(bebidaId) {
        RecetaRepository.cargarPlatosYBebidas(
            onSuccess = { data ->
                bebida = data.bebidas.find { it.id == bebidaId }
                error = bebida == null
                loading = false
            },
            onError = {
                error = true
                loading = false
            }
        )
    }

    when {
        loading -> LoadingScreen()
        error -> ErrorScreen()
        bebida != null -> BebidaDetalleContent(bebida!!, navController)
    }
}

@Composable
fun BebidaDetalleContent(bebida: Bebida, navController: NavController) {
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var isFav by remember { mutableStateOf(false) }

    LaunchedEffect(bebida.id) {
        isFav = UserRepository.isBeverageFavorite(uid, bebida.id)
        UserRepository.recordView(uid, bebida.id, "Bebida")

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bebida.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            if (isFav) UserRepository.removeFavoriteBeverage(uid, bebida.id)
                            else UserRepository.addFavoriteBeverage(uid, bebida.id)
                            isFav = !isFav
                        }
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFav) "Quitar favorito" else "Agregar favorito",
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
                    imagen = bebida.imagen,
                    contentDescription = bebida.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    "Receta para ${bebida.porciones} vaso${if (bebida.porciones > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(bebida.ingredientes_detalle) { ingrediente ->
                Text("• $ingrediente", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("Preparación", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            items(bebida.pasos) { paso ->
                Text(paso, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
