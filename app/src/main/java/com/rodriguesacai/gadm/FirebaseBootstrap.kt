package com.rodriguesacai.gadm

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

/**
 * Garante a criação da FirebaseApp padrão antes de qualquer ViewModel criar
 * GadmRepository/FirebaseFirestore. As opções são fornecidas pelo arquivo
 * res/values/firebase_options.xml gerado pelo instalador.
 */
object FirebaseBootstrap {
    private const val TAG = "GADM-Firebase"

    fun initialize(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                true
            } else {
                FirebaseApp.initializeApp(context) != null
            }
        } catch (error: Exception) {
            Log.e(TAG, "Firebase não pôde ser inicializado", error)
            false
        }
    }
}
