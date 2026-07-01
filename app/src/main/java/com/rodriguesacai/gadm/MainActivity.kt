package com.rodriguesacai.gadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.gadm.ui.GadmMobileTheme
import com.rodriguesacai.gadm.ui.UpGestorApp

/**
 * Entrada segura do GADM Mobile.
 *
 * A tela administrativa abre em modo operacional local mesmo quando o Firebase
 * ainda não está configurado. Assim, uma falha de inicialização remota nunca
 * impede o gestor de abrir o aplicativo.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseReady = FirebaseBootstrap.initialize(applicationContext)
        setContent {
            GadmMobileTheme {
                UpGestorApp(firebaseReady = firebaseReady)
            }
        }
    }
}
