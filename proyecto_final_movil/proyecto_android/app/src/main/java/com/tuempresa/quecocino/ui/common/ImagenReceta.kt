package com.tuempresa.quecocino.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.tuempresa.quecocino.R

@Composable
fun ImagenReceta(imagen: String, contentDescription: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isUrl = imagen.startsWith("http", ignoreCase = true)
    if (isUrl) {
        AsyncImage(
            model = imagen,
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = painterResource(id = R.drawable.logo),
            error = painterResource(id = R.drawable.logo)
        )
    } else {
        val imagenId = context.resources.getIdentifier(imagen, "drawable", context.packageName)
        androidx.compose.foundation.Image(
            painter = painterResource(id = if (imagenId != 0) imagenId else R.drawable.logo),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}