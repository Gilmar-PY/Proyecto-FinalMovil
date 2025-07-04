@file:OptIn(ExperimentalMaterial3Api::class)
package com.tuempresa.quecocino.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.R
import com.tuempresa.quecocino.data.Bebida
import com.tuempresa.quecocino.data.Receta
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.data.IngredienteBusqueda
import com.tuempresa.quecocino.ui.common.ImagenReceta
import kotlinx.coroutines.launch

@Composable
fun HomeDashboardScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Usuario"
    val userPhotoUrl = user?.photoUrl?.toString() ?: ""

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var platos by remember { mutableStateOf(listOf<Receta>()) }
    var bebidas by remember { mutableStateOf(listOf<Bebida>()) }
    var mostrarTodasPlatos by remember { mutableStateOf(false) }
    var mostrarTodasBebidas by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        RecetaRepository.cargarPlatosYBebidas(
            onSuccess = { data ->
                platos = data.platos
                bebidas = data.bebidas
                loading = false
            },
            onError = { e ->
                Log.e("HomeDashboard", "Error cargando recetas", e)
                errorMensaje = "Error al cargar recetas"
                loading = false
            }
        )
    }

    val ingredientes = remember(platos) {
        platos.flatMap { it.ingredientes_busqueda }
            .distinctBy { it.nombre }
            .sortedBy { it.nombre }
    }

    val saludo = getGreeting()
    val recetasRecomendadas = if (mostrarTodasPlatos) platos else recomendarRecetas(platos)
    val bebidasRecomendadas = if (mostrarTodasBebidas) bebidas else recomendarRecetasBebidas(bebidas)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                // Header del drawer mejorado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF9000), Color(0xFFFF7043))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        if (userPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Foto perfil",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape),
                                fallback = painterResource(R.drawable.logo),
                                error = painterResource(R.drawable.logo)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("Mi Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("perfil")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                    label = { Text("Mis Favoritos") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("favorites")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F7F7))
        ) {
            item {
                // Header mejorado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color(0xFF333333)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = saludo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    if (userPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Foto perfil",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .shadow(4.dp, CircleShape),
                            fallback = painterResource(R.drawable.logo),
                            error = painterResource(R.drawable.logo)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF9000), Color(0xFFFF7043))
                                    )
                                )
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Buscador mejorado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { navController.navigate("search?ingredient=") },
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFFFF9000),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Buscar platos por ingredientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF9000))
                        }
                    }
                }
                errorMensaje != null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(errorMensaje!!, color = Color.Red)
                        }
                    }
                }
                else -> {
                    // Ingredientes populares con lógica del segundo código
                    if (ingredientes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Ingredientes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )

                            LazyRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(ingredientes) { ingrediente ->
                                    IngredientCard(
                                        ingrediente = ingrediente,
                                        onClick = {
                                            navController.navigate("search?ingredient=${ingrediente.nombre}")
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Platos recomendados
                    if (recetasRecomendadas.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Platos recomendados",
                                seeAllText = if (mostrarTodasPlatos) "Ver menos" else "Ver todos",
                                onSeeAllClick = { mostrarTodasPlatos = !mostrarTodasPlatos },
                                color = Color(0xFFFF6B6B)
                            )

                            LazyRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recetasRecomendadas) { receta ->
                                    RecipeCard(
                                        receta = receta,
                                        onClick = { navController.navigate("detalle/${receta.id}") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Bebidas recomendadas
                    if (bebidasRecomendadas.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Bebidas peruanas",
                                seeAllText = if (mostrarTodasBebidas) "Ver menos" else "Ver todos",
                                onSeeAllClick = { mostrarTodasBebidas = !mostrarTodasBebidas },
                                color = Color(0xFF4DD0E1)
                            )

                            LazyRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(bebidasRecomendadas) { bebida ->
                                    BebidaCard(
                                        bebida = bebida,
                                        onClick = { navController.navigate("bebida_detalle/${bebida.id}") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingrediente: IngredienteBusqueda,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(88.dp)           // ANCHO FIJO (ajusta a tu gusto)
            .height(120.dp)         // ALTO FIJO (ajusta a tu gusto)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),     // padding menor para aprovechar espacio
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImagenReceta(
                imagen = ingrediente.imagen,
                contentDescription = ingrediente.nombre,
                modifier = Modifier
                    .size(48.dp)                     // tamaño de la imagen fijo
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ingrediente.nombre,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF333333),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun SectionHeader(
    title: String,
    seeAllText: String,
    onSeeAllClick: () -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Text(
            text = seeAllText,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
private fun RecipeCard(
    receta: Receta,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImagenReceta(
                imagen = receta.imagen,
                contentDescription = receta.nombre,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = receta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9000)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver receta", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BebidaCard(
    bebida: Bebida,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImagenReceta(
                imagen = bebida.imagen,
                contentDescription = bebida.nombre,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = bebida.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4DD0E1)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver bebida", color = Color.White)
                }
            }
        }
    }
}

// Helpers mantienen la misma funcionalidad
fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 6..11 -> "¡Buenos días!"
        in 12..17 -> "¡Buenas tardes!"
        else -> "¡Buenas noches!"
    }
}

fun recomendarRecetas(recetas: List<Receta>, cantidad: Int = 4): List<Receta> = recetas.take(cantidad)
fun recomendarRecetasBebidas(bebidas: List<Bebida>, cantidad: Int = 4): List<Bebida> = bebidas.take(cantidad)
