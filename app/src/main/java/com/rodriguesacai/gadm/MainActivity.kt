package com.rodriguesacai.gadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.gadm.ui.GadmApp
import com.rodriguesacai.gadm.ui.theme.GadmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GadmTheme {
                GadmApp()
            }
        }
    }
}
