package com.rodriguesacai.gadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rodriguesacai.gadm.ui.GadmViewModel
import com.rodriguesacai.gadm.ui.UpGestorApp
import com.rodriguesacai.gadm.ui.theme.GadmTheme

/**
 * Ponto único de entrada do GADM Mobile.
 *
 * A tela operacional ativa é UpGestorApp. GadmApp/FirebaseGateway pertenciam
 * à arquitetura antiga e não devem ser compilados junto com esta versão.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: GadmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseBootstrap.initialize(applicationContext)
        setContent {
            GadmTheme {
                UpGestorApp(viewModel = viewModel)
            }
        }
    }
}
