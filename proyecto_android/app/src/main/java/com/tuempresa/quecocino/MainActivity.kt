package com.tuempresa.quecocino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.tuempresa.quecocino.ui.home.HomeScreen
import com.tuempresa.quecocino.ui.login.LoginScreen
import com.tuempresa.quecocino.ui.theme.QueCocinoTheme

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            QueCocinoTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            googleSignInClient = googleSignInClient,
                            auth = auth,
                            onLoginSuccess = { navController.navigate("home") }
                        )
                    }
                    composable("home") { HomeScreen() }
                }
            }
        }
    }
}
