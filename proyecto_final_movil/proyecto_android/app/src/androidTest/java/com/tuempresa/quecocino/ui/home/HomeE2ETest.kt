package com.tuempresa.quecocino.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tuempresa.quecocino.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BuscarPapaE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun buscarPapaYVerificarReceta() {
        // 1. Ve a la pestaña Buscar (abajo)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Buscar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Buscar").onFirst().performClick()

        // 2. Espera el campo de búsqueda con el hint “¿Qué ingredientes tienes hoy?”
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodesWithText("¿Qué ingredientes tienes hoy?").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Busca el único campo de texto editable en esa pantalla y escribe “papa”
        composeTestRule.onAllNodes(isEditable()).onFirst().performTextInput("papa")

        // 4. Espera a que salga la tarjeta o chip con texto “Papa”
        composeTestRule.waitUntil(timeoutMillis = 4_000) {
            composeTestRule.onAllNodesWithText("Papa").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Papa").onFirst().performClick()

        // 5. Espera los resultados, como “Lomo Saltado”
        composeTestRule.waitUntil(timeoutMillis = 7_000) {
            composeTestRule.onAllNodesWithText("Lomo Saltado").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Lomo Saltado").assertExists()

        // 6. Da click en el primer botón “Ver receta”
        composeTestRule.onAllNodesWithText("Ver receta").onFirst().performClick()

        // 7. (Opcional) Espera un identificador en la pantalla de detalle, por ejemplo el nombre de la receta
        composeTestRule.waitUntil(timeoutMillis = 4_000) {
            composeTestRule.onAllNodesWithText("Lomo Saltado").fetchSemanticsNodes().isNotEmpty()
         }
        composeTestRule.onNodeWithText("Lomo Saltado").assertExists()
    }
}
