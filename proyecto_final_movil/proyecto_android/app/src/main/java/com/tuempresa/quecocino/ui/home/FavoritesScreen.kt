@file:OptIn(ExperimentalMaterial3Api::class)

package com.tuempresa.quecocino.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.data.Bebida
import com.tuempresa.quecocino.data.Receta
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.data.UserRepository
import com.tuempresa.quecocino.ui.common.ImagenReceta
import kotlinx.coroutines.launch

sealed class FavoriteItem {
    data class Recipe(val receta: Receta) : FavoriteItem()
    data class Beverage(val bebida: Bebida) : FavoriteItem()
}

@Composable
fun FavoritesScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var viewFilter by remember { mutableStateOf(Filter.ALL) }
    var items by remember { mutableStateOf(emptyList<FavoriteItem>()) }
    var loading by remember { mutableStateOf(true) }
    var trigger by remember { mutableStateOf(0) }

    LaunchedEffect(uid, trigger) {
        loading = true
        val recIds = UserRepository.getFavoritesRecipes(uid)
        val bevIds = UserRepository.getFavoritesBeverages(uid)
        val recipes = recIds.mapNotNull { RecetaRepository.getRecetaById(it) }.map { FavoriteItem.Recipe(it) }
        val beverages = bevIds.mapNotNull { RecetaRepository.getBebidaById(it) }.map { FavoriteItem.Beverage(it) }
        items = recipes + beverages
        loading = false
    }

    // Definir las listas filtradas fuera del scope local
    val recipes = items.filterIsInstance<FavoriteItem.Recipe>()
    val beverages = items.filterIsInstance<FavoriteItem.Beverage>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F7))
    ) {
        // Header personalizado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mis Favoritos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFFFF9000),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Contador y filtros mejorados
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = when (viewFilter) {
                    Filter.ALL -> "Todos (${items.size})"
                    Filter.RECIPES -> "Recetas (${recipes.size})"
                    Filter.BEVERAGES -> "Bebidas (${beverages.size})"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tabs personalizados
            TabRow(
                selectedTabIndex = viewFilter.ordinal,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFF9000)
            ) {
                Filter.values().forEachIndexed { idx, fl ->
                    Tab(
                        selected = viewFilter.ordinal == idx,
                        onClick = { viewFilter = fl },
                        text = {
                            Text(
                                text = fl.label,
                                color = if (viewFilter.ordinal == idx) Color(0xFFFF9000) else Color.Gray,
                                fontWeight = if (viewFilter.ordinal == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9000))
            }
        } else {
            val display = when (viewFilter) {
                Filter.ALL -> items
                Filter.RECIPES -> recipes
                Filter.BEVERAGES -> beverages
            }

            if (display.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tienes favoritos aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "¡Explora y guarda tus recetas favoritas!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(display) { fav ->
                        FavoriteCard(fav, navController) {
                            trigger++
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    fav: FavoriteItem,
    navController: NavController,
    onRemoved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val (img, name, isRecipe, route) = when (fav) {
        is FavoriteItem.Recipe ->
            Quadruple(fav.receta.imagen, fav.receta.nombre, true, "detalle/${fav.receta.id}")
        is FavoriteItem.Beverage ->
            Quadruple(fav.bebida.imagen, fav.bebida.nombre, false, "bebida_detalle/${fav.bebida.id}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen con fondo redondeado
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                ImagenReceta(
                    imagen = img,
                    contentDescription = name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(route) },
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9000)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Ver receta",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            IconButton(
                onClick = {
                    scope.launch {
                        if (isRecipe)
                            UserRepository.removeFavoriteRecipe(uid, (fav as FavoriteItem.Recipe).receta.id)
                        else
                            UserRepository.removeFavoriteBeverage(uid, (fav as FavoriteItem.Beverage).bebida.id)
                        onRemoved()
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFEBEE))
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Quitar favorito",
                    tint = Color(0xFFE53E3E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private enum class Filter(val label: String) {
    ALL("Todos"), RECIPES("Platos"), BEVERAGES("Bebidas")
}

// Reemplazo simple del no existente Quadruple
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)