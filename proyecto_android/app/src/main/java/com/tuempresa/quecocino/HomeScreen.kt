package com.tuempresa.quecocino.ui.home

import android.net.Uri                                    // ← import
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter             // ← import
import com.google.firebase.auth.FirebaseAuth              // opcional, para leer currentUser
import com.tuempresa.quecocino.R

/* ---------- Modelo ---------- */
data class Recipe(val id: Int,val title: String, val ingredientes: Int)
data class Recetas(val id: Int,val nombre: String, val platos: Int)
data class Jugos(val id: Int,val jugonombre: String, val jugos: Int)
/* ---------- Pantalla Home ---------- */
@Composable
fun HomeScreen(auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val user = auth.currentUser
    val userName = user?.displayName ?: "Invitado"
    val photoUrl = user?.photoUrl            // Uri? | puede ser null

    Scaffold(
        topBar = { HomeTopBar(userName, photoUrl) }, // ← usamos la versión con foto
        bottomBar = { BottomBarUI() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SearchBar()
            Spacer(Modifier.height(16.dp))
            RecipeSection(
                recipes = listOf(
                    Recipe(1, "Pollo" ,R.drawable.pollo),
                    Recipe(2,  "Cebolla",R.drawable.cebolla),
                    Recipe(3, "Arroz", R.drawable.arroz)
                ),
                title = "Recetas más recomendadas"
            )

            RecetasSection(
                recipes = listOf(
                    Recetas(1, "Aji de gallina" ,R.drawable.ajidegallina),
                    Recetas(2,  "Papa a la Huancaina",R.drawable.papa_huancaina),
                    Recetas(3, "Seco de pollo", R.drawable.seco_pollo)
                )
            )
            JugosSection(
                title = "Jugos",
                recipes = listOf(
                    Jugos(1, "Jugo de naranja" ,R.drawable.jugo_naranja),
                    Jugos(2,  "Jugo verde",R.drawable.jugoverde),
                    Jugos(3, "Jugo fresa", R.drawable.jugofresa)
                )
            )
        }
    }
}

/* ---------- TopBar con avatar ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(userName: String, photoUrl: Uri?) {
    TopAppBar(
        title = { Text("Buenas tardes, $userName") },
        navigationIcon = {
            IconButton(onClick = { /* TODO: abrir drawer */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            if (photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }
        }
    )
}

/* ---------- SearchBar ---------- */
@Composable
private fun SearchBar() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value = searchText.text,
        onValueChange = { searchText = TextFieldValue(it) },
        placeholder = { Text("Buscar por ingredientes…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        singleLine = true
    )
}

/* ---------- Sección de ingredientes ---------- */
@Composable
private fun RecipeSection(title: String, recipes: List<Recipe>) {
    LazyRow(
        state = rememberLazyListState(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { RecipeCard(it) }
    }
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)

}

/* ---------- Sección de recetas ---------- */
@Composable
private fun RecetasSection(recipes: List<Recetas>) {
    LazyRow(
        state = rememberLazyListState(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { RecetasCard(it) }
    }
    Spacer(Modifier.height(8.dp))

}

/* ---------- Sección de jugos ---------- */
@Composable
private fun JugosSection(title: String,recipes: List<Jugos>) {

    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyRow(
        state = rememberLazyListState(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { JugosCard(it) }
    }

}

/* ---------- Card ingredientes---------- */
@Composable
private fun RecipeCard(recipe: Recipe) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(160.dp)
            .wrapContentHeight()
            .clickable { /* ver a detalle */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            // Imagen en la parte superior
            Image(
                painter = painterResource(id = recipe.ingredientes),
                contentDescription = "Imagen de ${recipe.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Spacer(Modifier.height(8.dp))

            // Título
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 8.dp)
            )

        }

    }
}

/* ---------- Card Platos---------- */
@Composable
private fun RecetasCard(recipe: Recetas) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(250.dp)
            .height(150.dp)
            .clickable { /* ver detalle */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(id = recipe.platos),
                contentDescription = "Imagen de ${recipe.nombre}",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.width(12.dp))

            // Título y botón a la derecha
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = recipe.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { /* ver recetas*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF57C00),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Ver receta", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

    }
}

/* ---------- Card Jugos---------- */
@Composable
private fun JugosCard(recipe: Jugos) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(250.dp)
            .height(150.dp)
            .clickable { /* ver detalle */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(id = recipe.jugos),
                contentDescription = "Imagen de ${recipe.jugonombre}",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.width(12.dp))

            // Título y botón a la derecha
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = recipe.jugonombre,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { /* ver recetas*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF57C00),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Ver receta", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

    }
}

@Composable
fun BottomBarUI() {
    NavigationBar {
        NavigationBarItem(
            selected = true,  // puedes controlar cuál está activo
            onClick = { /* sin lógica */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* sin lógica */ },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ingredientes") },
            label = { Text("Ingredientes") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* sin lógica */ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Persona") },
            label = { Text("Persona") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* sin lógica */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notificaciones") },
            label = { Text("Notificaciones") }
        )
    }
}

