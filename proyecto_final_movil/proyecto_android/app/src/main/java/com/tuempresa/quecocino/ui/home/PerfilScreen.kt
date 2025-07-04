@file:OptIn(ExperimentalMaterial3Api::class)
package com.tuempresa.quecocino.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.tuempresa.quecocino.R
import com.tuempresa.quecocino.data.ActivityEntry
import com.tuempresa.quecocino.data.UserRepository
import com.tuempresa.quecocino.data.RecetaRepository
import com.tuempresa.quecocino.ui.common.ImagenReceta
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerfilScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "Usuario anónimo"
    val email = user?.email ?: ""
    val photoUrl = user?.photoUrl?.toString()
    val context = LocalContext.current

    var recent by remember {
        mutableStateOf<List<Triple<ActivityEntry, String?, String?>>>(emptyList())
    }
    val scope = rememberCoroutineScope()
    var recFavs by remember { mutableStateOf(0) }
    var bevFavs by remember { mutableStateOf(0) }

    LaunchedEffect(uid) {
        scope.launch {
            recFavs = UserRepository.getFavoritesRecipes(uid).size
            bevFavs = UserRepository.getFavoritesBeverages(uid).size
            val raw = UserRepository.getRecentActivity(uid, limit = 10)
            recent = raw.map { entry ->
                if (entry.type == "Receta") {
                    val r = RecetaRepository.getRecetaById(entry.itemId)
                    Triple(entry, r?.nombre, r?.imagen)
                } else {
                    val b = RecetaRepository.getBebidaById(entry.itemId)
                    Triple(entry, b?.nombre, b?.imagen)
                }
            }
        }
    }

    val totalFavs = recFavs + bevFavs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F7))
            .verticalScroll(rememberScrollState())
    ) {
        // Header simple sin gradiente
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFFF9000),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeader(name, email, photoUrl)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Estadísticas con mejor espaciado
            Text(
                text = "Mis Estadísticas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStat(
                    count = totalFavs,
                    label = "Total favoritos",
                    iconColor = Color(0xFFFF9000),
                    icon = Icons.Outlined.Favorite,
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    count = recFavs,
                    label = "Recetas favoritas",
                    iconColor = Color(0xFFE53935),
                    icon = Icons.Outlined.LocalDining,
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    count = bevFavs,
                    label = "Bebidas favoritas",
                    iconColor = Color(0xFF26C6DA),
                    icon = Icons.Outlined.LocalBar,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actividad reciente más compacta
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (recent.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Aún no hay actividad",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                "¡Explora recetas para ver tu actividad aquí!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recent) { (act, title, imgUrl) ->
                            RecentActivityItem(act, title, imgUrl)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de cerrar sesión
            Button(
                onClick = {
                    cerrarSesion(context, navController)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Icon(
                    Icons.Filled.PowerSettingsNew,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Cerrar Sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }


            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileHeader(name: String, email: String, photoUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto perfil",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.logo),
                    error = painterResource(R.drawable.logo)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF9000), Color(0xFFFF7043))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(
    count: Int,
    label: String,
    iconColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontSize = 20.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun RecentActivityItem(act: ActivityEntry, title: String?, imgUrl: String?) {
    val timeText = act.timestamp?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!imgUrl.isNullOrBlank()) {
                ImagenReceta(
                    imagen = imgUrl,
                    contentDescription = title ?: "",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title ?: "${act.type} desconocido",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${act.action.lowercase()} • $timeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun cerrarSesion(context: Context, navController: NavController) {
    // Cierra sesión de Firebase
    FirebaseAuth.getInstance().signOut()
    // Cierra sesión de Google
    GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    ).signOut().addOnCompleteListener {
        // Cuando termine, navega a login y limpia el backstack
        navController.navigate("login") {
            popUpTo("home") { inclusive = true }
        }
    }
}