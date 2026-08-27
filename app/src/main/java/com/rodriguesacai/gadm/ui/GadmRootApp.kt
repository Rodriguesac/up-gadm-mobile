package com.rodriguesacai.gadm.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.gadm.data.OnlineClient
import com.rodriguesacai.gadm.data.SupabaseCatalogClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val OnlinePurple = Color(0xFF4B0082)
private val OnlinePurpleSoft = Color(0xFFF4EDF8)
private val OnlineGreen = Color(0xFF72B51B)
private val OnlineText = Color(0xFF242934)
private val OnlineMuted = Color(0xFF77808D)
private val OnlineSurface = Color(0xFFF7F8FA)
private val OnlineBorder = Color(0xFFE7E8EC)

@Composable
fun GadmRootApp(vm: GadmViewModel = viewModel()) {
    var showOnline by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showOnline) { showOnline = false }

    if (showOnline) {
        OnlineClientsScreen(onBack = { showOnline = false })
        return
    }

    Box(Modifier.fillMaxSize()) {
        GadmV24App(vm)
        if (vm.state.user != null) {
            Button(
                onClick = { showOnline = true },
                colors = ButtonDefaults.buttonColors(containerColor = OnlinePurple, contentColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 18.dp)
            ) {
                Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Clientes online", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OnlineClientsScreen(onBack: () -> Unit) {
    val client = remember { SupabaseCatalogClient() }
    val scope = rememberCoroutineScope()
    var clients by remember { mutableStateOf<List<OnlineClient>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        runCatching { client.listOnlineClients() }
            .onSuccess {
                clients = it
                error = null
            }
            .onFailure { error = it.message ?: "Não foi possível atualizar os clientes online." }
        loading = false
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            load()
            delay(12_000)
        }
    }

    Column(Modifier.fillMaxSize().background(OnlineSurface)) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = OnlinePurple)
            }
            Column(Modifier.weight(1f)) {
                Text("Clientes online", color = OnlineText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Mapa de quem está no site agora", color = OnlineMuted, fontSize = 10.sp)
            }
            IconButton(onClick = { scope.launch { loading = true; load() } }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar", tint = OnlineMuted)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OnlinePurple),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(48.dp).background(Color.White.copy(alpha = .14f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.People, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (loading) "Atualizando…" else "${clients.size} online agora",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Sai do mapa após cerca de 90 s sem sinal",
                                color = Color.White.copy(alpha = .76f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            error?.let { message ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(message, color = Color(0xFFB33131), fontSize = 12.sp, modifier = Modifier.padding(15.dp))
                    }
                }
            }

            val mapped = clients.filter { it.latitude != null && it.longitude != null }
            item {
                Column {
                    Text("Mapa", color = OnlineText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(
                        if (mapped.isEmpty()) "Nenhuma localização disponível neste momento." else "${mapped.size} ponto(s) com localização",
                        color = OnlineMuted,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    if (mapped.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = OnlineMuted, modifier = Modifier.size(34.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Clientes online sem ponto de localização", color = OnlineMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        OnlineMap(mapped)
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegendDot(OnlineGreen, "Aparelho")
                    LegendDot(OnlinePurple, "IP aproximado")
                }
            }

            item {
                Text("Ativos agora", color = OnlineText, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            if (!loading && clients.isEmpty()) {
                item {
                    Text(
                        "Nenhum cliente com o site aberto agora.",
                        color = OnlineMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp)
                    )
                }
            }

            items(clients, key = { it.id }) { online ->
                OnlineClientCard(online)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, color = OnlineMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OnlineClientCard(client: OnlineClient) {
    val sourceLabel = when (client.locationSource) {
        "device" -> "Localização do aparelho"
        "ip" -> "Localização aproximada por IP"
        else -> "Sem localização"
    }
    val place = listOf(client.city, client.region).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Local não identificado" }
    val sourceColor = if (client.locationSource == "device") OnlineGreen else OnlinePurple

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(if (client.locationSource == "device") Color(0xFFF0F8E4) else OnlinePurpleSoft, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = sourceColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(client.name.ifBlank { "Cliente online" }, color = OnlineText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(place, color = OnlineMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(sourceLabel, color = sourceColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Página: ${prettyPage(client.page)}", color = OnlineMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun prettyPage(page: String): String = when {
    page.contains("montar-pedido", true) -> "Montando pedido"
    page.contains("checkout", true) -> "Finalizando pedido"
    page.contains("acompan", true) -> "Acompanhamento"
    page.contains("cardap", true) -> "Cardápio"
    page == "/" || page.isBlank() -> "Início"
    else -> page
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OnlineMap(clients: List<OnlineClient>) {
    val html = remember(clients) { buildMapHtml(clients) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://rodriguesacaiecia.netlify.app/", html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (webView.tag != html.hashCode()) {
                    webView.tag = html.hashCode()
                    webView.loadDataWithBaseURL("https://rodriguesacaiecia.netlify.app/", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxWidth().height(390.dp)
        )
    }
}

private fun buildMapHtml(clients: List<OnlineClient>): String {
    val points = JSONArray()
    clients.forEach { client ->
        val lat = client.latitude ?: return@forEach
        val lon = client.longitude ?: return@forEach
        points.put(
            JSONObject().apply {
                put("lat", lat)
                put("lon", lon)
                put("name", client.name.ifBlank { "Cliente online" })
                put("page", prettyPage(client.page))
                put("place", listOf(client.city, client.region).filter { it.isNotBlank() }.joinToString(" • "))
                put("source", client.locationSource)
                put("accuracy", client.accuracyM ?: JSONObject.NULL)
            }
        )
    }

    return """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
        <style>html,body,#map{height:100%;margin:0;background:#f1f3f5} .leaflet-popup-content{font-family:system-ui,sans-serif;font-size:12px;line-height:1.35} .leaflet-control-attribution{font-size:8px!important}</style>
        </head><body><div id="map"></div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>
        const points=$points;
        const map=L.map('map',{zoomControl:true});
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);
        const bounds=[];
        points.forEach(p=>{
          const exact=p.source==='device';
          const color=exact?'#72B51B':'#4B0082';
          const radius=exact?Math.max(30,Math.min(Number(p.accuracy)||80,500)):4500;
          L.circle([p.lat,p.lon],{radius,color,fillColor:color,fillOpacity:exact?.10:.07,weight:2}).addTo(map);
          L.circleMarker([p.lat,p.lon],{radius:8,color:'#fff',weight:3,fillColor:color,fillOpacity:1}).addTo(map)
            .bindPopup('<b>'+escapeHtml(p.name)+'</b><br>'+escapeHtml(p.place||'Local aproximado')+'<br>'+escapeHtml(p.page)+'<br><b>'+(exact?'Aparelho':'IP aproximado')+'</b>');
          bounds.push([p.lat,p.lon]);
        });
        function escapeHtml(v){return String(v||'').replace(/[&<>\"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#039;'}[c]));}
        if(bounds.length===1) map.setView(bounds[0], points[0].source==='device'?15:11);
        else if(bounds.length>1) map.fitBounds(bounds,{padding:[28,28],maxZoom:15});
        else map.setView([-20.47,-54.62],10);
        </script></body></html>
    """.trimIndent()
}
