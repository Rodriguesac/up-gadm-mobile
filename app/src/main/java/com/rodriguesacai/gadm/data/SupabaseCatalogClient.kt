package com.rodriguesacai.gadm.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente mínimo do Supabase usado pelo GADM.
 * A autenticação continua por PIN e a service-role jamais fica dentro do APK.
 */
class SupabaseCatalogClient {

    suspend fun signIn(pin: String): GadmUser {
        val response = postCatalog(JSONObject().apply {
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
        postCatalog(JSONObject().apply {
            put("action", "change_pin")
            put("new_pin", newPin)
        })
    }

    suspend fun listProducts(): List<GadmProduct> {
        requireSession()
        val response = postCatalog(JSONObject().apply { put("action", "list_products") })
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
        postCatalog(JSONObject().apply {
            put("action", "toggle_product")
            put("product_id", productId)
            put("paused", paused)
        })
    }

    suspend fun listPixChanges(): List<PixChangeAdminRequest> {
        requireSession()
        val response = postPix(JSONObject().apply { put("action", "list") })
        val rows = response.optJSONArray("requests") ?: return emptyList()
        return buildList {
            for (index in 0 until rows.length()) {
                val item = rows.optJSONObject(index) ?: continue
                val received = if (item.isNull("cashReceivedAmount")) null else item.optDouble("cashReceivedAmount")
                add(
                    PixChangeAdminRequest(
                        id = item.optString("id"),
                        orderId = item.optString("orderId"),
                        orderCode = item.optString("orderCode"),
                        customerName = item.optString("customerName", "Cliente"),
                        amount = item.optDouble("amount", 0.0),
                        cashExpected = item.optDouble("cashExpected", 0.0),
                        cashReceivedAmount = received,
                        cashReceivedAt = item.optString("cashReceivedAt").takeIf { it.isNotBlank() && it != "null" },
                        status = item.optString("status"),
                        pixKeyType = item.optString("pixKeyType"),
                        pixKey = item.optString("pixKey"),
                        recipientName = item.optString("recipientName"),
                        bank = item.optString("bank"),
                        pixSentAt = item.optString("pixSentAt").takeIf { it.isNotBlank() && it != "null" },
                        pixReference = item.optString("pixReference"),
                        createdAt = item.optString("createdAt"),
                        updatedAt = item.optString("updatedAt")
                    )
                )
            }
        }
    }

    suspend fun markPixChangeSent(id: String, reference: String = "") {
        requireSession()
        postPix(JSONObject().apply {
            put("action", "mark_sent")
            put("id", id)
            if (reference.isNotBlank()) put("reference", reference)
        })
    }

    fun clearSession() {
        sessionToken = ""
    }

    private fun requireSession() {
        check(sessionToken.isNotBlank()) { "Sessão do GADM expirada. Entre novamente." }
    }

    private suspend fun postCatalog(payload: JSONObject, authenticated: Boolean = true): JSONObject =
        post(CATALOG_ENDPOINT, payload, authenticated)

    private suspend fun postPix(payload: JSONObject): JSONObject =
        post(PIX_ENDPOINT, payload, authenticated = true)

    private suspend fun post(endpoint: String, payload: JSONObject, authenticated: Boolean): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
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
                        .ifBlank { "Falha ao acessar o Supabase ($status)." }
                    if (status == 401) sessionToken = ""
                    error(message)
                }
                response
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        @Volatile
        private var sessionToken: String = ""
        private const val CATALOG_ENDPOINT = "https://jgjmntezfjuyuxhcnvhd.supabase.co/functions/v1/gadm-catalog"
        private const val PIX_ENDPOINT = "https://jgjmntezfjuyuxhcnvhd.supabase.co/functions/v1/pix-change-admin"
    }
}
