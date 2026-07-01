package com.rodriguesacai.gadm

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

/** Inicialização defensiva: Firebase é opcional para a primeira abertura da UI. */
object FirebaseBootstrap {
    private const val TAG = "GADM-Firebase"

    fun initialize(context: Context): Boolean = try {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            true
        } else {
            FirebaseApp.initializeApp(context) != null
        }
    } catch (error: Exception) {
        Log.w(TAG, "Firebase indisponível: iniciando GADM em modo local", error)
        false
    }
}
