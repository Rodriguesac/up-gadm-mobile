package com.rodriguesacai.gadm.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente mínimo do catálogo Supabase usado pelo GADM.
 *
 * A autenticação continua por PIN, mas a conferência do hash acontece somente
 * no Edge Function. A service-role jamais fica dentro do APK.
 */
class SupabaseCatalogClient {
    @Volatile
    private var sessionToken: String = ""

    suspend fun signIn(pin: String): GadmUser {
        val response = post(JSONObject().apply {
            put("action", "login")
            put("pin", pin)
        }, authenticated = false)

        sessionToken = response.getString("session")
        val user = response.getJSONObject("user")
        return GadmUser(
            id = user.optString("id", "master"),
            name = user.optString("name", "Gestor"),
            role = user.optString("role", "ADMIN"),
            active = user.optBoolean("active", true)
        )
    }

    suspend fun changePin(newPin: String) {
        requireSession()
        post(JSONObject().apply {
            put("action", "change_pin")
            put("new_pin", newPin)
        })
    }

    suspend fun listProducts(): List<GadmProduct> {
        requireSession()
        val response = post(JSONObject().apply { put("action", "list_products") })
        val rows = response.optJSONArray("products") ?: return emptyList()
        return buildList {
            for (index in 0 until rows.length()) {
                val item = rows.optJSONObject(index) ?: continue
                add(
                    GadmProduct(
                        id = item.optString("id"),
                        name = item.optString("name", "Produto"),
                        category = item.optString("category", ""),
                        price = item.optDouble("price", 0.0),
                        stock = item.optDouble("stock", 0.0),
                        active = item.optBoolean("active", true),
                        paused = item.optBoolean("paused", false),
                        raw = emptyMap()
                    )
                )
            }
        }
    }

    suspend fun toggleProduct(productId: String, paused: Boolean) {
        requireSession()
        post(JSONObject().apply {
            put("action", "toggle_product")
            put("product_id", productId)
            put("paused", paused)
        })
    }

    fun clearSession() {
        sessionToken = ""
    }

    private fun requireSession() {
        check(sessionToken.isNotBlank()) { "Sessão do GADM expirada. Entre novamente." }
    }

    private suspend fun post(payload: JSONObject, authenticated: Boolean = true): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 18_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                if (authenticated && sessionToken.isNotBlank()) {
                    setRequestProperty("x-gadm-session", sessionToken)
                }
            }

            try {
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = runCatching { JSONObject(text.ifBlank { "{}" }) }.getOrElse { JSONObject() }
                if (status !in 200..299) {
                    val message = response.optString("message")
                        .ifBlank { response.optString("detail") }
                        .ifBlank { response.optString("error") }
                        .ifBlank { "Falha ao acessar o catálogo ($status)." }
                    if (status == 401) sessionToken = ""
                    error(message)
                }
                response
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        private const val ENDPOINT = "https://jgjmntezfjuyuxhcnvhd.supabase.co/functions/v1/gadm-catalog"
    }
}
