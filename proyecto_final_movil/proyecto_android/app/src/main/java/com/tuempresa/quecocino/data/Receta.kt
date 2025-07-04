package com.tuempresa.quecocino.data

data class IngredienteBusqueda(
    val nombre: String,
    val imagen: String
)

data class Receta(
    val id: String,
    val nombre: String,
    val ingredientes_busqueda: List<IngredienteBusqueda>,
    val ingredientes_detalle: List<String>,
    val porciones: Int,
    val pasos: List<String>,
    val imagen: String
)

data class Bebida(
    val id: String,
    val tipo: String,
    val nombre: String,
    val ingredientes_busqueda: List<IngredienteBusqueda>,
    val ingredientes_detalle: List<String>,
    val porciones: Int,
    val pasos: List<String>,
    val imagen: String
)

data class PlatosYBebidas(
    val platos: List<Receta>,
    val bebidas: List<Bebida>
)
