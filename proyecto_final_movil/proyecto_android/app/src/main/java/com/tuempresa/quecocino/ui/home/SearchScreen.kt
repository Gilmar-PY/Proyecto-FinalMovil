@file:OptIn(ExperimentalMaterial3Api::class)
package com.tuempresa.quecocino.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tuempresa.quecocino.data.Receta
import com.tuempresa.quecocino.data.Bebida
import com.tuempresa.quecocino.data.IngredienteBusqueda
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.ui.common.ImagenReceta

@Composable
fun SearchScreen(navController: NavController, initialIngredient: String?) {
    var platos by remember { mutableStateOf(emptyList<Receta>()) }
    var bebidas by remember { mutableStateOf(emptyList<Bebida>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var ingredientesSeleccionados by remember { mutableStateOf(setOf<IngredienteBusqueda>()) }

    LaunchedEffect(Unit) {
        RecetaRepository.cargarPlatosYBebidas(
            onSuccess = { data ->
                platos = data.platos
                bebidas = data.bebidas
                loading = false

                initialIngredient?.takeIf(String::isNotBlank)?.let { nombre ->
                    val ing = (platos.flatMap { it.ingredientes_busqueda } + bebidas.flatMap { it.ingredientes_busqueda })
                        .firstOrNull { it.nombre == nombre }
                    ing?.let { ingredientesSeleccionados = setOf(it) }
                }
            },
            onError = {
                Log.e("SearchScreen", "Error cargando datos", it)
                error = true
                loading = false
            }
        )
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Error cargando recetas.\nRevisa tu conexión.",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val allIngredients = remember(platos, bebidas) {
        (platos.flatMap { it.ingredientes_busqueda } + bebidas.flatMap { it.ingredientes_busqueda })
            .distinctBy { it.nombre }
            .sortedBy { it.nombre }
    }

    val filteredIngredients = remember(searchText, allIngredients) {
        if (searchText.isBlank()) allIngredients
        else allIngredients.filter { it.nombre.contains(searchText, ignoreCase = true) }
    }

    val resultados: List<Any> = remember(ingredientesSeleccionados, platos, bebidas) {
        val recs = if (ingredientesSeleccionados.isEmpty()) emptyList() else platos.filter { receta ->
            ingredientesSeleccionados.all { sel ->
                receta.ingredientes_busqueda.any { it.nombre == sel.nombre }
            }
        }

        val bevs = if (ingredientesSeleccionados.isEmpty()) emptyList() else bebidas.filter { bebida ->
            ingredientesSeleccionados.all { sel ->
                bebida.ingredientes_busqueda.any { it.nombre == sel.nombre }
            }
        }

        (recs + bevs) as List<Any>
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("¿Qué ingredientes tienes hoy?", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF9000))
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Busca ingredientes...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (ingredientesSeleccionados.isNotEmpty()) {
                LazyRow(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    item {
                        AssistChip(
                            onClick = { ingredientesSeleccionados = emptySet() },
                            label = { Text("Limpiar todo") },
                            leadingIcon = {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar selección", tint = Color(0xFFFF6B6B))
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE0F7FA)),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    items(ingredientesSeleccionados.toList()) { sel ->
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { ingredientesSeleccionados -= sel },
                            color = Color.Black,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                ImagenReceta(imagen = sel.imagen, contentDescription = sel.nombre, modifier = Modifier.size(22.dp))
                                Text(sel.nombre, color = Color.White, modifier = Modifier.padding(start = 6.dp))
                                Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = Color.White, modifier = Modifier.size(18.dp).padding(start = 6.dp))
                            }
                        }
                    }
                }
            }

            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredIngredients) { ing ->
                    val sel = ingredientesSeleccionados.contains(ing)
                    IngredientCard(
                        ingrediente = ing,
                        onClick = {
                            ingredientesSeleccionados =
                                if (sel) ingredientesSeleccionados - ing
                                else ingredientesSeleccionados + ing
                        },
                        seleccionado = sel // Este argumento es opcional, sigue leyendo abajo.
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (ingredientesSeleccionados.isNotEmpty()) {
                Text("Resultados: ${resultados.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (resultados.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron recetas o bebidas con esos ingredientes 🥲", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(resultados) { item ->
                            RecetaOBebidaCard(item, navController)
                        }
                    }
                }
            }
        }
    }
}

// Componente para mostrar una receta o bebida en la lista de resultados
@Composable
fun RecetaOBebidaCard(item: Any, navController: NavController) {
    val nombre: String
    val imagen: String
    val porciones: Int
    val id: String
    val esBebida = item is Bebida

    when (item) {
        is Receta -> {
            nombre = item.nombre
            imagen = item.imagen
            porciones = item.porciones
            id = item.id
        }
        is Bebida -> {
            nombre = item.nombre
            imagen = item.imagen
            porciones = item.porciones
            id = item.id
        }
        else -> return
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            ImagenReceta(imagen = imagen, contentDescription = nombre, modifier = Modifier.size(70.dp).padding(8.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium)
                if (porciones > 0) {
                    Text(if (esBebida) "Para $porciones vaso(s)" else "Para $porciones persona(s)", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(
                onClick = {
                    if (esBebida)
                        navController.navigate("bebida_detalle/$id")
                    else
                        navController.navigate("detalle/$id")
                },
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Ver receta", color = Color.White)
            }
        }
    }
}

@Composable
fun IngredientCard(
    ingrediente: IngredienteBusqueda,
    onClick: () -> Unit,
    seleccionado: Boolean = false // Opcional, para resaltar si está seleccionado
) {
    Card(
        modifier = Modifier
            .width(88.dp)
            .height(120.dp)
            .clickable { onClick() }
            .then(
                if (seleccionado)
                    Modifier.background(Color(0xFFFFF3E0))
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = if (seleccionado) Color(0xFFFFEBEE) else Color.White),
        shape = RoundedCornerShape(16.dp),
        border = if (seleccionado) BorderStroke(2.dp, Color(0xFFFF6B6B)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImagenReceta(
                imagen = ingrediente.imagen,
                contentDescription = ingrediente.nombre,
                modifier = Modifier
                    .size(48.dp)
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
