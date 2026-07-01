package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AppSection(val label: String, val glyph: String) {
    DASHBOARD("Início", "⌂"),
    ORDERS("Pedidos", "▤"),
    KITCHEN("Cozinha", "⌁"),
    TOWER("Torre", "⌖"),
    MORE("Mais", "⋯")
}

private enum class OrderStatus(
    val label: String,
    val compact: String,
    val accent: Color,
    val soft: Color
) {
    RECEIVED("Recebido", "Recebidos", GadmBlue, GadmSoftBlue),
    CONFIRMED("Confirmado", "Confirmados", GadmYellow, GadmSoftOrange),
    PREPARING("Em preparo", "Em preparo", Color(0xFFFF8B24), GadmSoftOrange),
    READY("Pronto", "Prontos", GadmSuccess, GadmSoftLime),
    ON_ROUTE("Em rota", "Em rota", Color(0xFF7D5CE7), Color(0xFFF0EDFF)),
    FINISHED("Finalizado", "Finalizados", GadmMuted, Color(0xFFF0F2F5)),
    CANCELED("Cancelado", "Cancelados", GadmDanger, GadmSoftDanger)
}

private data class Order(
    val id: Int,
    val customer: String,
    val phone: String,
    val neighborhood: String,
    val address: String,
    val time: String,
    val items: Int,
    val value: String,
    val payment: String,
    val status: OrderStatus,
    val notes: String,
    val prepMinutes: Int,
    val contents: List<String>
)

private data class Driver(
    val name: String,
    val district: String,
    val distance: String,
    val vehicle: String,
    val payment: String,
    val avatar: String,
    val available: Boolean = true
)

private fun initialOrders() = listOf(
    Order(2847, "Juliana Martins", "(62) 99925-6844", "Vila América", "Rua T-63, 215 · Jardim América", "14:25", 4, "R$ 45,90", "PIX", OrderStatus.RECEIVED, "Sem cebola e sem granola.", 0, listOf("Açaí 700 ml · Morango, banana, granola", "Água mineral 500 ml")),
    Order(2846, "Lucas Oliveira", "(62) 98123-4567", "Jardim América", "Av. T-9, 443 · Jardim América", "14:24", 2, "R$ 67,80", "Cartão", OrderStatus.CONFIRMED, "Ligar ao chegar na portaria.", 0, listOf("Açaí 700 ml · Leite ninho, morango", "Cupuaçu 500 ml · Granola")),
    Order(2845, "Larissa Mota", "(62) 99611-2200", "Setor Bueno", "Rua 89, 70 · Setor Bueno", "14:23", 3, "R$ 89,70", "PIX", OrderStatus.PREPARING, "Prioridade: cliente aguardando no local.", 8, listOf("Açaí 1 L · Banana, granola, paçoca", "Açaí 500 ml · Leite em pó")),
    Order(2844, "Carlos Eduardo", "(62) 99840-2011", "Parque Amazônia", "Rua C-109, 61 · Parque Amazônia", "14:20", 4, "R$ 35,80", "Dinheiro", OrderStatus.READY, "Troco para R$ 50,00.", 0, listOf("Açaí 500 ml · Morango, flocos", "Água mineral 500 ml")),
    Order(2843, "Renata Silva", "(62) 99003-0090", "Jardim Goiás", "Av. E, 888 · Jardim Goiás", "14:18", 4, "R$ 52,40", "Cartão", OrderStatus.ON_ROUTE, "Entregar pela entrada lateral.", 0, listOf("Açaí 700 ml · Kiwi, leite condensado", "Cupuaçu 300 ml")),
    Order(2842, "Marcos Vinícius", "(62) 99131-8120", "Alto da Glória", "Rua 12, 131 · Alto da Glória", "14:16", 2, "R$ 31,90", "PIX", OrderStatus.FINISHED, "Pedido entregue sem ocorrência.", 0, listOf("Açaí 500 ml · Paçoca, leite ninho")),
    Order(2841, "Beatriz Alves", "(62) 99115-0202", "Setor Marista", "Rua 1128, 11 · Marista", "14:12", 3, "R$ 76,50", "Cartão", OrderStatus.READY, "Confirmar interfone 204.", 0, listOf("Açaí 1 L · Nutella, banana", "Açaí 300 ml · Granola")),
    Order(2840, "Thiago Mendes", "(62) 99212-0010", "Setor Oeste", "Rua 4, 340 · Setor Oeste", "14:08", 5, "R$ 102,60", "Dinheiro", OrderStatus.PREPARING, "Pedido grande para empresa.", 12, listOf("4x Açaí 500 ml variados", "2x Água mineral"))
)

private fun initialDrivers() = listOf(
    Driver("Bruno Alves", "Setor Bueno", "1,2 km", "Moto", "Dinheiro · R$ 120,00", "BA"),
    Driver("Silvia Lima", "Jardim América", "1,8 km", "Moto", "Cartão · Máquina OK", "SL"),
    Driver("João Santos", "Vila Nova", "2,1 km", "Bicicleta", "Dinheiro · R$ 80,00", "JS"),
    Driver("Amanda Ferreira", "St. Campinas", "2,4 km", "Moto", "Cartão · Máquina OK", "AF")
)

/**
 * Interface operacional completa e navegável. Ela não depende de dados remotos
 * para abrir: o gestor pode revisar o fluxo com dados locais e o Firebase pode
 * ser conectado ao repositório sem trocar a camada de interface.
 */
@Composable
fun UpGestorApp(firebaseReady: Boolean) {
    var authenticated by rememberSaveable { mutableStateOf(false) }
    var activeSectionName by rememberSaveable { mutableStateOf(AppSection.DASHBOARD.name) }
    var selectedOrderId by remember { mutableStateOf<Int?>(null) }
    var orders by remember { mutableStateOf(initialOrders()) }
    var drivers by remember { mutableStateOf(initialDrivers()) }
    var snackbar by remember { mutableStateOf<String?>(null) }

    val activeSection = AppSection.valueOf(activeSectionName)
    val updateOrder: (Int, OrderStatus) -> Unit = { id, newStatus ->
        orders = orders.map { order -> if (order.id == id) order.copy(status = newStatus) else order }
        snackbar = "Pedido #$id atualizado para ${newStatus.label.lowercase()}."
    }

    if (!authenticated) {
        LoginScreen(onEnter = { authenticated = true })
        return
    }

    val selected = orders.firstOrNull { it.id == selectedOrderId }
    if (selected != null) {
        OrderDetailScreen(
            order = selected,
            onBack = { selectedOrderId = null },
            onStatusChange = { updateOrder(selected.id, it) },
            onFeedback = { snackbar = it }
        )
        return
    }

    Scaffold(
        containerColor = GadmSurface,
        bottomBar = {
            BottomBar(
                active = activeSection,
                onNavigate = { activeSectionName = it.name },
                onQuickAdd = { snackbar = "Novo pedido: cadastro rápido aberto." }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSection) {
                AppSection.DASHBOARD -> DashboardScreen(
                    orders = orders,
                    firebaseReady = firebaseReady,
                    onOpenOrders = { activeSectionName = AppSection.ORDERS.name },
                    onOpenKitchen = { activeSectionName = AppSection.KITCHEN.name },
                    onOpenTower = { activeSectionName = AppSection.TOWER.name },
                    onOpenOrder = { selectedOrderId = it }
                )
                AppSection.ORDERS -> OrdersScreen(
                    orders = orders,
                    onOpen = { selectedOrderId = it },
                    onQuickAction = { id, status -> updateOrder(id, status) }
                )
                AppSection.KITCHEN -> KitchenScreen(
                    orders = orders,
                    onOpen = { selectedOrderId = it },
                    onStatusChange = updateOrder
                )
                AppSection.TOWER -> TowerScreen(
                    orders = orders,
                    drivers = drivers,
                    onOpen = { selectedOrderId = it },
                    onAssign = { driver, order ->
                        drivers = drivers.map { if (it.name == driver.name) it.copy(available = false) else it }
                        updateOrder(order.id, OrderStatus.ON_ROUTE)
                        snackbar = "${driver.name} recebeu o pedido #${order.id}."
                    }
                )
                AppSection.MORE -> OperationsScreen(
                    firebaseReady = firebaseReady,
                    onAction = { snackbar = it }
                )
            }

            snackbar?.let { message ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GadmNavy)
                        .clickable { snackbar = null }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(message, color = GadmWhite, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(onEnter: () -> Unit) {
    var cpf by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberData by rememberSaveable { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GadmSurface)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(34.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(GadmLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 34.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("GADM", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = 1.sp)
                    Text("MOBILE", color = GadmLime, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 3.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Gestão inteligente para o seu delivery", color = GadmMuted, fontSize = 15.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(42.dp))
            Text("ACESSO ADMINISTRATIVO", color = GadmMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            LoginField(label = "CPF", value = cpf, placeholder = "000.000.000-00", glyph = "♙", onChange = { cpf = it })
            Spacer(Modifier.height(14.dp))
            LoginField(label = "SENHA", value = password, placeholder = "Digite sua senha", glyph = "◉", onChange = { password = it })
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.clickable { rememberData = !rememberData },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (rememberData) GadmLime else GadmWhite)
                            .border(1.dp, if (rememberData) GadmLime else GadmBorder, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) { if (rememberData) Text("✓", color = GadmNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(8.dp))
                    Text("Lembrar meus dados", color = GadmMuted, fontSize = 12.sp)
                }
                Text("Esqueci minha senha", color = GadmBlue, fontSize = 12.sp, modifier = Modifier.clickable { })
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "ENTRAR", onClick = onEnter)
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = GadmBorder)
                Text("  ou  ", color = GadmMuted, fontSize = 12.sp)
                Divider(modifier = Modifier.weight(1f), color = GadmBorder)
            }
            Spacer(Modifier.height(18.dp))
            OutlineAction(text = "Entrar com biometria", glyph = "◌", onClick = onEnter)
        }
        Text("Versão 2.2.0 · Admin Premium", color = GadmMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 36.dp))
    }
}

@Composable
private fun LoginField(label: String, value: String, placeholder: String, glyph: String, onChange: (String) -> Unit) {
    Column {
        Text(label, color = GadmMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = GadmMuted, fontSize = 14.sp) },
            trailingIcon = { Text(glyph, color = GadmMuted, fontSize = 20.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GadmLime,
                unfocusedBorderColor = GadmBorder,
                focusedContainerColor = GadmWhite,
                unfocusedContainerColor = GadmWhite,
                cursorColor = GadmLime
            )
        )
    }
}

@Composable
private fun DashboardScreen(
    orders: List<Order>,
    firebaseReady: Boolean,
    onOpenOrders: () -> Unit,
    onOpenKitchen: () -> Unit,
    onOpenTower: () -> Unit,
    onOpenOrder: (Int) -> Unit
) {
    val received = orders.count { it.status == OrderStatus.RECEIVED }
    val confirmed = orders.count { it.status == OrderStatus.CONFIRMED }
    val preparing = orders.count { it.status == OrderStatus.PREPARING }
    val ready = orders.count { it.status == OrderStatus.READY }
    val route = orders.count { it.status == OrderStatus.ON_ROUTE }
    val finished = orders.count { it.status == OrderStatus.FINISHED }
    val cancelled = orders.count { it.status == OrderStatus.CANCELED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header(title = "Dashboard Admin", subtitle = "Segunda-feira, 19 de maio", badge = if (firebaseReady) "Online" else "Modo local") }
        item {
            Text("Olá, Admin! 👋", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
        }
        item {
            MetricGrid(
                listOf(
                    Metric("$received", "Recebidos", "▤", GadmBlue, GadmSoftBlue, onOpenOrders),
                    Metric("$confirmed", "Confirmados", "◌", GadmYellow, GadmSoftOrange, onOpenOrders),
                    Metric("$preparing", "Em preparo", "⌁", Color(0xFFFF8B24), GadmSoftOrange, onOpenKitchen),
                    Metric("$ready", "Prontos", "✓", GadmSuccess, GadmSoftLime, onOpenKitchen),
                    Metric("$route", "Em rota", "⌖", Color(0xFF7D5CE7), Color(0xFFF0EDFF), onOpenTower),
                    Metric("$finished", "Finalizados", "◉", GadmMuted, Color(0xFFF0F2F5), onOpenOrders)
                )
            )
        }
        item {
            if (cancelled > 0) {
                SmallAlert("$cancelled cancelados hoje", "Revisar ocorrências e motivo do cancelamento.", GadmDanger, GadmSoftDanger)
            }
        }
        item { RevenueCard() }
        item { SectionTitle("Ações rápidas", "Operação do dia") }
        item {
            QuickActions(
                onOrders = onOpenOrders,
                onKitchen = onOpenKitchen,
                onTower = onOpenTower,
                onOpenOrder = onOpenOrder,
                firstOrder = orders.firstOrNull()?.id
            )
        }
        item { SectionTitle("Acompanhar agora", "Pedidos com ação pendente") }
        items(orders.filter { it.status in setOf(OrderStatus.RECEIVED, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY) }.take(4), key = { it.id }) {
            OrderCard(order = it, onClick = { onOpenOrder(it.id) }, compact = true)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

private data class Metric(val value: String, val label: String, val glyph: String, val color: Color, val tint: Color, val onClick: () -> Unit)

@Composable
private fun MetricGrid(metrics: List<Metric>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { metric ->
                    MetricCard(metric, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricCard(metric: Metric, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { metric.onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GadmWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GadmBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(metric.value, color = metric.color, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                GlyphBadge(metric.glyph, metric.color, metric.tint)
            }
            Spacer(Modifier.height(4.dp))
            Text(metric.label, color = GadmNavy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(5.dp))
            Text("Ver todos ›", color = GadmBlue, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RevenueCard() {
    SurfaceCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("Resumo do dia", color = GadmNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Atualizado às 14:32", color = GadmMuted, fontSize = 11.sp)
                Spacer(Modifier.height(14.dp))
                Text("Faturamento do dia", color = GadmMuted, fontSize = 12.sp)
                Text("R$ 4.785,60", color = GadmNavy, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("↗ 18,6% em relação a ontem", color = GadmSuccess, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            MiniChart()
        }
        Spacer(Modifier.height(16.dp))
        Divider(color = GadmBorder)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryMetric("Pedidos do dia", "128")
            SummaryMetric("Entregadores", "12")
            SummaryMetric("Tempo médio", "34 min")
        }
    }
}

@Composable
private fun MiniChart() {
    Column(modifier = Modifier.width(112.dp), verticalArrangement = Arrangement.Bottom) {
        Spacer(Modifier.height(19.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            listOf(18, 32, 25, 48, 39, 56, 72).forEachIndexed { index, h ->
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (index == 6) GadmLime else GadmSoftLime)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = GadmNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(title, color = GadmMuted, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

@Composable
private fun QuickActions(
    onOrders: () -> Unit,
    onKitchen: () -> Unit,
    onTower: () -> Unit,
    onOpenOrder: (Int) -> Unit,
    firstOrder: Int?
) {
    val actions = listOf(
        QuickAction("Novo pedido", "＋", { firstOrder?.let(onOpenOrder) }),
        QuickAction("Pedidos", "▤", onOrders),
        QuickAction("Cozinha", "⌁", onKitchen),
        QuickAction("Torre", "⌖", onTower),
        QuickAction("Chat cliente", "◌", { }),
        QuickAction("Estoque", "◇", { }),
        QuickAction("Cardápio", "▧", { }),
        QuickAction("Relatórios", "⌁", { })
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        actions.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { action ->
                    Card(
                        modifier = Modifier.weight(1f).clickable { action.onClick() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GadmWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GadmBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 11.dp, horizontal = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(action.glyph, color = GadmBlue, fontSize = 21.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(action.label, color = GadmNavy, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

private data class QuickAction(val label: String, val glyph: String, val onClick: () -> Unit)

@Composable
private fun OrdersScreen(
    orders: List<Order>,
    onOpen: (Int) -> Unit,
    onQuickAction: (Int, OrderStatus) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val filterLabels = listOf("Todos") + OrderStatus.values().map { it.compact }
    val filtered = if (filter == "Todos") orders else orders.filter { it.status.compact == filter }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Pedidos", subtitle = "Operação em tempo real", trailing = "⌕  ⌇")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterLabels.forEach { label ->
                val selected = filter == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) GadmSoftBlue else GadmWhite)
                        .border(1.dp, if (selected) GadmBlue else GadmBorder, RoundedCornerShape(12.dp))
                        .clickable { filter = label }
                        .padding(horizontal = 11.dp, vertical = 8.dp)
                ) {
                    Text(label, color = if (selected) GadmBlue else GadmMuted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Ordenar: mais recentes", color = GadmMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(filtered, key = { it.id }) { order ->
                OrderCard(order = order, onClick = { onOpen(order.id) }, onQuickAction = onQuickAction)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    compact: Boolean = false,
    onQuickAction: ((Int, OrderStatus) -> Unit)? = null
) {
    SurfaceCard(modifier = Modifier.clickable { onClick() }, padding = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(4.dp).height(if (compact) 88.dp else 104.dp).background(order.status.accent))
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("#${order.id}", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    StatusPill(order.status)
                }
                Spacer(Modifier.height(5.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(order.customer, color = GadmNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("⌖ ${order.neighborhood}", color = GadmMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(order.value, color = if (order.status == OrderStatus.ON_ROUTE) GadmBlue else GadmSuccess, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("▧ ${order.items} itens   ◷ ${order.time}   ${order.payment}", color = GadmMuted, fontSize = 10.sp, maxLines = 1)
                    if (!compact && onQuickAction != null) {
                        val next = nextStatus(order.status)
                        if (next != null) {
                            Text(
                                text = "${next.label} ›",
                                color = GadmBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GadmSoftBlue)
                                    .clickable { onQuickAction(order.id, next) }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailScreen(
    order: Order,
    onBack: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onFeedback: (String) -> Unit
) {
    val next = nextStatus(order.status)
    Column(modifier = Modifier.fillMaxSize().background(GadmSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = GadmNavy, fontSize = 33.sp, modifier = Modifier.clickable { onBack() }.padding(end = 11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido #${order.id}", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                Text("Atualizado agora", color = GadmMuted, fontSize = 11.sp)
            }
            Text("⋯", color = GadmNavy, fontSize = 24.sp)
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(order.status.accent)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(order.status.label.uppercase(), color = if (order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PREPARING) GadmNavy else GadmWhite, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
            item {
                SurfaceCard {
                    SectionTitle("Cliente", "Via WhatsApp")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlyphBadge("♙", GadmBlue, GadmSoftBlue)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.customer, color = GadmNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(order.phone, color = GadmMuted, fontSize = 12.sp)
                        }
                        Text("☎", color = GadmBlue, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Divider(color = GadmBorder)
                    Spacer(Modifier.height(12.dp))
                    Text("ENDEREÇO DE ENTREGA", color = GadmMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("⌖ ${order.address}", color = GadmNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Goiânia · GO · CEP 74255-220", color = GadmMuted, fontSize = 11.sp)
                    Text("Ver no mapa ›", color = GadmBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp).clickable { onFeedback("Mapa do pedido aberto.") })
                }
            }
            item {
                SurfaceCard {
                    SectionTitle("Itens do pedido (${order.items})", "")
                    order.contents.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${index + 1}x  $item", color = GadmNavy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Adicionais conforme montagem do cliente", color = GadmMuted, fontSize = 10.sp)
                            }
                            Text(if (index == 0) "R$ 32,90" else "R$ 28,90", color = GadmNavy, fontSize = 12.sp)
                        }
                        if (index != order.contents.lastIndex) Spacer(Modifier.height(11.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider(color = GadmBorder)
                    Spacer(Modifier.height(9.dp))
                    MoneyLine("Subtotal", "R$ 65,80")
                    MoneyLine("Taxa de entrega", "R$ 6,00")
                    Spacer(Modifier.height(3.dp))
                    MoneyLine("Total do pedido", order.value, emphasized = true)
                }
            }
            item {
                SurfaceCard {
                    SectionTitle("Pagamento", "")
                    MoneyLine("Forma de pagamento", order.payment)
                    MoneyLine("Troco para", if (order.payment == "Dinheiro") "R$ 50,00" else "—")
                    if (order.payment == "Dinheiro") MoneyLine("Troco", "R$ 14,20", emphasisColor = GadmYellow)
                    Spacer(Modifier.height(12.dp))
                    Text("OBSERVAÇÕES DO CLIENTE", color = GadmMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.7.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(order.notes, color = GadmNavy, fontSize = 12.sp)
                }
            }
            item { Timeline(status = order.status) }
            item {
                SurfaceCard {
                    Text("Ações inteligentes", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.height(11.dp))
                    if (next != null) {
                        PrimaryButton(
                            text = when (next) {
                                OrderStatus.CONFIRMED -> "CONFIRMAR PEDIDO"
                                OrderStatus.PREPARING -> "INICIAR PREPARO"
                                OrderStatus.READY -> "FINALIZAR PREPARO"
                                OrderStatus.ON_ROUTE -> "ENVIAR PARA TORRE"
                                OrderStatus.FINISHED -> "FINALIZAR ENTREGA"
                                else -> "ATUALIZAR PEDIDO"
                            },
                            onClick = { onStatusChange(next) }
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlineAction("Corrigir pedido", "⌕", Modifier.weight(1f)) { onFeedback("Edição do pedido aberta.") }
                        OutlineAction("Priorizar", "★", Modifier.weight(1f)) { onFeedback("Pedido #${order.id} priorizado na cozinha.") }
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlineAction("Chamar cliente", "☎", Modifier.weight(1f)) { onFeedback("Ligação para ${order.customer} preparada.") }
                        DangerAction("Cancelar", Modifier.weight(1f)) { onStatusChange(OrderStatus.CANCELED) }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun KitchenScreen(
    orders: List<Order>,
    onOpen: (Int) -> Unit,
    onStatusChange: (Int, OrderStatus) -> Unit
) {
    val preparing = orders.filter { it.status == OrderStatus.PREPARING }
    val urgent = preparing.filter { it.prepMinutes >= 10 }
    val ready = orders.filter { it.status == OrderStatus.READY }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Cozinha / Produção", subtitle = "Fila e tempo de preparo", trailing = "⌇")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KitchenTopChip("Visão geral", "${preparing.size + ready.size}", GadmBlue, GadmSoftBlue, Modifier.weight(1f))
            KitchenTopChip("Prioritários", "${urgent.size}", GadmDanger, GadmSoftDanger, Modifier.weight(1f))
            KitchenTopChip("Prontos", "${ready.size}", GadmSuccess, GadmSoftLime, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            KitchenLane(
                title = "EM PREPARO",
                count = preparing.size,
                color = GadmBlue,
                tint = GadmSoftBlue,
                orders = preparing,
                buttonText = "DETALHES",
                onOpen = onOpen,
                onAction = { }
            )
            KitchenLane(
                title = "PRIORITÁRIOS",
                count = urgent.size,
                color = GadmDanger,
                tint = GadmSoftDanger,
                orders = urgent.ifEmpty { preparing.take(1) },
                buttonText = "INICIAR AGORA",
                onOpen = onOpen,
                onAction = { onStatusChange(it.id, OrderStatus.PREPARING) }
            )
            KitchenLane(
                title = "PRONTOS",
                count = ready.size,
                color = GadmSuccess,
                tint = GadmSoftLime,
                orders = ready,
                buttonText = "ENVIAR P/ TORRE",
                onOpen = onOpen,
                onAction = { onStatusChange(it.id, OrderStatus.ON_ROUTE) }
            )
        }
        Spacer(Modifier.height(12.dp))
        SurfaceCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionTitle("Indicadores da cozinha", "Atualizado agora")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("Em preparo", "${preparing.size}")
                SummaryMetric("Prioritários", "${urgent.size}")
                SummaryMetric("Prontos", "${ready.size}")
                SummaryMetric("Tempo médio", "39 min")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Toque em um pedido para abrir os detalhes, alterar status ou chamar o cliente.", color = GadmMuted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 18.dp))
    }
}

@Composable
private fun KitchenTopChip(title: String, value: String, color: Color, tint: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .24f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(title, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun KitchenLane(
    title: String,
    count: Int,
    color: Color,
    tint: Color,
    orders: List<Order>,
    buttonText: String,
    onOpen: (Int) -> Unit,
    onAction: (Order) -> Unit
) {
    Column(
        modifier = Modifier
            .width(205.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GadmWhite)
            .border(1.dp, GadmBorder, RoundedCornerShape(18.dp))
            .padding(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            Box(modifier = Modifier.clip(CircleShape).background(tint).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("$count", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(152.dp), contentAlignment = Alignment.Center) {
                Text("Sem pedidos", color = GadmMuted, fontSize = 12.sp)
            }
        } else {
            orders.take(3).forEach { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onOpen(order.id) },
                    colors = CardDefaults.cardColors(containerColor = tint),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("#${order.id}", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(order.customer, color = GadmNavy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                        Text("${order.items} itens · ${order.time}", color = GadmMuted, fontSize = 10.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (order.status == OrderStatus.PREPARING) "%02d:%02d".format(order.prepMinutes, 32) else "✓ PRONTO",
                            color = color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (order.status == OrderStatus.PREPARING) 22.sp else 15.sp
                        )
                        if (order.prepMinutes >= 10) Text("⚠ URGENTE", color = GadmDanger, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(9.dp))
                                .background(color)
                                .clickable { onAction(order) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(buttonText, color = GadmWhite, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TowerScreen(
    orders: List<Order>,
    drivers: List<Driver>,
    onOpen: (Int) -> Unit,
    onAssign: (Driver, Order) -> Unit
) {
    val ready = orders.filter { it.status == OrderStatus.READY }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(title = "Torre / Logística", subtitle = "Despacho e entregadores", trailing = "♧")
        SurfaceCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pedidos prontos para envio (${ready.size})", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text("Atualizado 14:32", color = GadmMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(10.dp))
            DispatchMap(ready)
            Spacer(Modifier.height(8.dp))
            OutlineAction("VER NO MAPA COMPLETO", "⌖", Modifier.fillMaxWidth()) { }
        }
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionTitle("Entregadores disponíveis (${drivers.count { it.available }})", "Ordenar ▾")
            drivers.forEach { driver ->
                DriverCard(driver = driver, readyOrder = ready.firstOrNull(), onAssign = { order -> onAssign(driver, order) })
                Spacer(Modifier.height(8.dp))
            }
            Text("Ver todos entregadores ›", color = GadmBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 6.dp))
            SurfaceCard {
                SectionTitle("Painel de operação", "")
                OperationalGrid()
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DispatchMap(orders: List<Order>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F4F8))
            .border(1.dp, GadmBorder, RoundedCornerShape(16.dp))
    ) {
        listOf("ST. BUENO", "MARISTA", "JD. GOIÁS", "CAMPINAS", "SETOR OESTE").forEachIndexed { index, name ->
            Text(
                name,
                color = GadmMuted.copy(alpha = .65f),
                fontSize = 9.sp,
                modifier = Modifier.padding(start = (14 + (index % 3) * 72).dp, top = (18 + (index / 2) * 52).dp)
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.Center).background(GadmBorder))
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).align(Alignment.Center).background(GadmBorder))
        orders.take(3).forEachIndexed { index, order ->
            val color = listOf(GadmYellow, GadmBlue, GadmSuccess)[index % 3]
            Box(
                modifier = Modifier
                    .padding(start = (38 + index * 74).dp, top = (64 + (index % 2) * 42).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text("#${order.id}", color = if (color == GadmYellow) GadmNavy else GadmWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(32.dp).clip(CircleShape).background(GadmWhite).border(1.dp, GadmBorder, CircleShape), contentAlignment = Alignment.Center) {
            Text("⌾", color = GadmBlue, fontSize = 18.sp)
        }
    }
}

@Composable
private fun DriverCard(driver: Driver, readyOrder: Order?, onAssign: (Order) -> Unit) {
    SurfaceCard(padding = 11.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(GadmSoftBlue), contentAlignment = Alignment.Center) {
                Text(driver.avatar, color = GadmBlue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(driver.name, color = GadmNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.width(5.dp))
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (driver.available) GadmSuccess else GadmMuted))
                }
                Text("${driver.distance} · ${driver.district} · ${driver.vehicle}", color = GadmMuted, fontSize = 10.sp)
                Text(driver.payment, color = GadmMuted, fontSize = 10.sp)
            }
            if (driver.available && readyOrder != null) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(GadmLime).clickable { onAssign(readyOrder) }.padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) { Text("ATRIBUIR", color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp) }
            } else {
                Text(if (driver.available) "LIVRE" else "EM ROTA", color = GadmMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OperationsScreen(firebaseReady: Boolean, onAction: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(title = "Painel operacional", subtitle = if (firebaseReady) "Sincronização ativa" else "Modo local seguro", trailing = "⚙")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SurfaceCard {
                SectionTitle("Estoque", "Ver todos ›")
                StockRow("Leite Ninho", "1,2 kg restantes", "BAIXO", GadmYellow, GadmSoftOrange)
                StockRow("Morango", "0,4 kg restantes", "CRÍTICO", GadmDanger, GadmSoftDanger)
                StockRow("Granola", "5,3 kg restantes", "OK", GadmSuccess, GadmSoftLime)
                StockRow("Creme de avelã", "1,1 kg restantes", "BAIXO", GadmYellow, GadmSoftOrange)
            }
            Spacer(Modifier.height(12.dp))
            SurfaceCard {
                SectionTitle("Ações rápidas", "")
                OperationalGrid(onAction)
            }
            Spacer(Modifier.height(12.dp))
            SurfaceCard {
                SectionTitle("Atendimento", "Ver todos ›")
                ChatRow("Gabriela Souza", "Quero adicionar mais um item", "16:23", "GS", 2)
                ChatRow("Lucas Oliveira", "Qual o tempo de entrega?", "14:31", "LO", 1)
                ChatRow("Renata Silva", "Entrega código: 6789, obrigada!", "14:20", "RS", 0)
            }
            Spacer(Modifier.height(12.dp))
            SurfaceCard {
                SectionTitle("Alertas", "")
                AlertRow("Estoque de morango crítico", "Agora", GadmDanger)
                AlertRow("12 pedidos prontos aguardando entrega", "2 min", GadmYellow)
                AlertRow("Tempo médio da cozinha acima da meta", "5 min", GadmBlue)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun StockRow(name: String, detail: String, tag: String, color: Color, tint: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphBadge("◇", color, tint, size = 34.dp)
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = GadmNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = GadmMuted, fontSize = 11.sp)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(tint).padding(horizontal = 8.dp, vertical = 5.dp)) {
            Text(tag, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun OperationalGrid(onAction: (String) -> Unit = {}) {
    val actions = listOf(
        "Estoque baixo" to "⚠",
        "Chat atendimento" to "◌",
        "Som novo pedido" to "◖",
        "Taxa de entrega" to "⌖",
        "Item pausado" to "Ⅱ",
        "Mais relatórios" to "⌁"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, glyph) ->
                    Card(
                        modifier = Modifier.weight(1f).clickable { onAction(label) },
                        shape = RoundedCornerShape(13.dp),
                        colors = CardDefaults.cardColors(containerColor = GadmSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GadmBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(glyph, color = GadmBlue, fontSize = 18.sp)
                            Spacer(Modifier.height(5.dp))
                            Text(label, color = GadmNavy, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(name: String, message: String, time: String, initials: String, unread: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(GadmSoftLime), contentAlignment = Alignment.Center) {
            Text(initials, color = GadmNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = GadmNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(message, color = GadmMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, color = GadmMuted, fontSize = 10.sp)
            if (unread > 0) Box(modifier = Modifier.padding(top = 4.dp).size(18.dp).clip(CircleShape).background(GadmLime), contentAlignment = Alignment.Center) {
                Text("$unread", color = GadmNavy, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun AlertRow(title: String, time: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(9.dp))
        Text(title, color = GadmNavy, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(time, color = GadmMuted, fontSize = 10.sp)
    }
}

@Composable
private fun Timeline(status: OrderStatus) {
    SurfaceCard {
        SectionTitle("Linha do tempo", "")
        val steps = listOf(OrderStatus.RECEIVED, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.ON_ROUTE)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEachIndexed { index, step ->
                val currentIndex = steps.indexOf(status).coerceAtLeast(0)
                val done = index <= currentIndex
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(if (done) GadmLime else GadmSurface).border(1.dp, if (done) GadmLime else GadmBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (done) "✓" else "·", color = if (done) GadmNavy else GadmMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(step.label.replace(" ", "\n"), color = if (done) GadmNavy else GadmMuted, fontSize = 8.sp, textAlign = TextAlign.Center, lineHeight = 9.sp, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String, badge: String? = null, trailing: String = "⌕  ☷") {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(GadmWhite).border(1.dp, GadmBorder, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
            Text("☰", color = GadmNavy, fontSize = 17.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(subtitle, color = GadmMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (badge != null) {
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GadmSoftLime).padding(horizontal = 7.dp, vertical = 5.dp)) {
                Text(badge, color = GadmSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(7.dp))
        }
        Text(trailing, color = GadmNavy, fontSize = 19.sp)
    }
}

@Composable
private fun SectionTitle(title: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = GadmNavy, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        if (action.isNotBlank()) Text(action, color = GadmBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, padding: androidx.compose.ui.unit.Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GadmWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GadmBorder)
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

@Composable
private fun SmallAlert(title: String, detail: String, color: Color, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(tint).border(1.dp, color.copy(alpha = .28f), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphBadge("!", color, GadmWhite)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(detail, color = GadmMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun GlyphBadge(glyph: String, color: Color, tint: Color, size: androidx.compose.ui.unit.Dp = 38.dp) {
    Box(modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(tint), contentAlignment = Alignment.Center) {
        Text(glyph, color = color, fontSize = (size.value * 0.48f).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusPill(status: OrderStatus) {
    Box(modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(status.soft).padding(horizontal = 8.dp, vertical = 5.dp)) {
        Text(status.label.uppercase(), color = status.accent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MoneyLine(label: String, value: String, emphasized: Boolean = false, emphasisColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (emphasized) GadmNavy else GadmMuted, fontSize = if (emphasized) 14.sp else 12.sp, fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.Normal)
        Text(value, color = emphasisColor ?: if (emphasized) GadmSuccess else GadmNavy, fontSize = if (emphasized) 15.sp else 12.sp, fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.Medium)
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy)
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    }
}

@Composable
private fun OutlineAction(text: String, glyph: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(43.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GadmWhite)
            .border(1.dp, GadmBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$glyph  $text", color = GadmBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DangerAction(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(43.dp).clip(RoundedCornerShape(12.dp)).background(GadmSoftDanger).border(1.dp, GadmDanger.copy(alpha = .25f), RoundedCornerShape(12.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("×  $text", color = GadmDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomBar(active: AppSection, onNavigate: (AppSection) -> Unit, onQuickAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).background(GadmWhite).border(1.dp, GadmBorder),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppSection.values().forEachIndexed { index, section ->
            if (index == 2) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).background(GadmLime).border(4.dp, GadmSurface, CircleShape).clickable { onQuickAdd() },
                    contentAlignment = Alignment.Center
                ) { Text("+", color = GadmNavy, fontSize = 29.sp, fontWeight = FontWeight.Light) }
            }
            NavItem(section, active == section) { onNavigate(section) }
        }
    }
}

@Composable
private fun NavItem(section: AppSection, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(49.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(section.glyph, color = if (selected) GadmLime else GadmMuted, fontSize = 19.sp)
        Text(section.label, color = if (selected) GadmNavy else GadmMuted, fontSize = 9.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun nextStatus(status: OrderStatus): OrderStatus? = when (status) {
    OrderStatus.RECEIVED -> OrderStatus.CONFIRMED
    OrderStatus.CONFIRMED -> OrderStatus.PREPARING
    OrderStatus.PREPARING -> OrderStatus.READY
    OrderStatus.READY -> OrderStatus.ON_ROUTE
    OrderStatus.ON_ROUTE -> OrderStatus.FINISHED
    OrderStatus.FINISHED, OrderStatus.CANCELED -> null
}
