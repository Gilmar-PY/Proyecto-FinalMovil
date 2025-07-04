package com.tuempresa.quecocino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.ui.home.*
import com.tuempresa.quecocino.ui.login.LoginScreen
import com.tuempresa.quecocino.ui.theme.QueCocinoTheme
import kotlinx.coroutines.delay

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
    object Favorites : Screen("favorites", "Favoritos", Icons.Default.Favorite)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.Person)
    object Login : Screen("login", "Login", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            QueCocinoTheme {
                MainScreen(googleSignInClient, auth)
            }
        }
    }
}

@Composable
fun MainScreen(
    googleSignInClient: GoogleSignInClient,
    auth: FirebaseAuth
) {
    val navController = rememberNavController()
    val isLogged = auth.currentUser != null
    var showSplash by remember { mutableStateOf(true) }
    val startRoute = if (isLogged) Screen.Home.route else Screen.Login.route

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreenView()
    } else {
        val navBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStack?.destination?.route

        Scaffold(
            bottomBar = {
                if (currentRoute != Screen.Login.route) {
                    NavigationBar {
                        listOf(
                            Screen.Home,
                            Screen.Search,
                            Screen.Favorites,
                            Screen.Perfil
                        ).forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(navController, googleSignInClient, auth)
                }
                composable(Screen.Home.route) {
                    HomeDashboardScreen(navController)
                }
                composable(
                    route = Screen.Search.route + "?ingredient={ingredient}",
                    arguments = listOf(navArgument("ingredient") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    })
                ) { entry ->
                    val ingredient = entry.arguments?.getString("ingredient")
                    SearchScreen(navController, initialIngredient = ingredient)
                }
                composable(
                    route = "detalle/{recetaId}",
                    arguments = listOf(navArgument("recetaId") { type = NavType.StringType })
                ) {
                    RecetaDetalleScreen(navController, it.arguments?.getString("recetaId") ?: "")
                }
                composable(
                    route = "bebida_detalle/{bebidaId}",
                    arguments = listOf(navArgument("bebidaId") { type = NavType.StringType })
                ) {
                    BebidaDetalleScreen(navController, it.arguments?.getString("bebidaId") ?: "")
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(navController)
                }
                composable(Screen.Perfil.route) {
                    PerfilScreen(navController)
                }
            }
        }
    }
}

@Composable
fun SplashScreenView() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
