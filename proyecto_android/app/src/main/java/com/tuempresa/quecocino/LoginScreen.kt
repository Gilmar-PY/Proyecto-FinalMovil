package com.tuempresa.quecocino.ui.login

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tuempresa.quecocino.R
import com.tuempresa.quecocino.data.UserRepository           // ← REPO
import kotlinx.coroutines.launch                             // corrutinas

@Composable
fun LoginScreen(
    googleSignInClient: GoogleSignInClient,
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit
) {
    /* ---------- Estados ---------- */
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    /* ---------- Scope para corrutinas ---------- */
    val scope = rememberCoroutineScope()

    /* ---------- Launcher de Google ---------- */
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential).addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    val firebaseUser = auth.currentUser!!
                    // Guardar/actualizar perfil en Firestore
                    scope.launch {
                        try {
                            UserRepository.upsertUser(firebaseUser)
                            isLoading = false
                            errorMessage = null
                            onLoginSuccess()                       // ← navegamos
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "No se pudo guardar tu perfil: ${e.message}"
                            Log.e("LoginScreen", "Firestore error", e)
                        }
                    }
                } else {
                    isLoading = false
                    errorMessage = "Error al iniciar sesión con Firebase: ${res.exception?.message}"
                    Log.e("LoginScreen", "Firebase sign-in failed", res.exception)
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Error al iniciar sesión con Google: ${e.statusCode}"
            Log.e("LoginScreen", "Google sign-in failed: ${e.statusCode}", e)
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error inesperado: ${e.message}"
            Log.e("LoginScreen", "Unexpected error", e)
        }
    }

    /* ---------- UI ---------- */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /* --- Logo temporal --- */
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo QueCocino",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp)) // Si quieres mantener los bordes redondeados
            )

            Spacer(Modifier.height(24.dp))

            Text("¡Bienvenido a QueCocino!", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Encuentra las mejores recetas basadas en los ingredientes que tienes",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            /* --- Botón Google --- */
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    launcher.launch(googleSignInClient.signInIntent)
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Google", modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Iniciar sesión con Google")
            }

            if (isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            errorMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}
