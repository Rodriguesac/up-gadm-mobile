package com.rodriguesacai.gadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.gadm.ui.GadmMobileTheme
import com.rodriguesacai.gadm.ui.GestorGeralApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseBootstrap.initialize(applicationContext)
        setContent {
            GadmMobileTheme {
                GestorGeralApp()
            }
        }
    }
}
