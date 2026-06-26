package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.gadm.data.GadmUiState
import com.rodriguesacai.gadm.data.OperationalOrder
import com.rodriguesacai.gadm.data.OrderItem
import com.rodriguesacai.gadm.data.OrderStatus
import com.rodriguesacai.gadm.ui.theme.Accent
import com.rodriguesacai.gadm.ui.theme.Ink
import com.rodriguesacai.gadm.ui.theme.Success
import com.rodriguesacai.gadm.ui.theme.Warning
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GadmTab(val label: String) {
    DASHBOARD("Visão geral"),
    PEDIDOS("Pedidos"),
    COZINHA("Cozinha"),
    TORRE("Torre"),
    AJUSTES("Ajustes")
}

@Composable
fun GadmApp(viewModel: GadmViewModel = viewModel()) {
    val state = viewModel.state
    var selectedTab by remember { mutableStateOf(GadmTab.DASHBOARD) }
    var selectedOrder by remember { mutableStateOf<OperationalOrder?>(null) }

    if (state.loading) {
        LoadingScreen()
        return
    }

    if (state.currentUser == null) {
        LoginScreen(
            busy = state.busy,
            error = state.error,
            onLogin = viewModel::signIn,
            onDismissError = viewModel::clearMessages
        )
        return
    }

    val current = selectedOrder?.let { selected -> state.orders.firstOrNull { it.id == selected.id } ?: selected }
    if (current != null) {
        OrderDetailScreen(
            state = state,
            order = current,
            onBack = { selectedOrder = null },
            onStatus = { target, note -> viewModel.updateStatus(current, target, note) },
            onAssign = { name, id -> viewModel.assignDriver(current, name, id) },
            onPriority = { priority -> viewModel.setPriority(current, priority) },
            onDismissMessage = viewModel::clearMessages
        )
        return
    }

    MainShell(
        state = state,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onOpenOrder = { selectedOrder = it },
        onRefresh = viewModel::restoreSession,
        onLogout = viewModel::signOut
    )
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Ink),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Color.White)
            Text("ABRINDO GADM MOBILE", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Text("Núcleo operacional", color = Color.White.copy(alpha = 0.62f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun LoginScreen(
    busy: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onDismissError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GADM", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Text("GESTOR OPERACIONAL MOBILE • v3.0.0", color = Color.White.copy(alpha = 0.62f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(30.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Acesso da equipe", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
                Text("Use o e-mail e a senha cadastrados no Firebase Authentication. O perfil em usuarios/{uid} precisa ser ADMIN ou GESTOR.", fontSize = 13.sp, color = Color(0xFF635E59), lineHeight = 18.sp)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("E-mail") },
                    singleLine = true,
                    enabled = !busy
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Senha") },
                    singleLine = true,
                    enabled = !busy,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error != null) ErrorPanel(error, onDismissError)
                Button(
                    onClick = { onLogin(email, password) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (busy) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text("ENTRAR NO GADM", fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Não use PIN local como segurança do negócio. O acesso real é controlado pelo Firebase Auth + perfil de função.", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(
    state: GadmUiState,
    selectedTab: GadmTab,
    onTabSelected: (GadmTab) -> Unit,
    onOpenOrder: (OperationalOrder) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.info) {
        val message = state.error ?: state.info
        if (!message.isNullOrBlank()) snack.showSnackbar(message)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snack) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GADM MOBILE", fontWeight = FontWeight.Black, letterSpacing = 0.3.sp)
                        Text("${state.currentUser?.name} • ${state.currentUser?.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, "Atualizar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomTabs(selectedTab, onTabSelected)
        }
    ) { padding ->
        when (selectedTab) {
            GadmTab.DASHBOARD -> DashboardScreen(state.orders, onOpenOrder, Modifier.padding(padding))
            GadmTab.PEDIDOS -> OrdersScreen(state.orders, onOpenOrder, Modifier.padding(padding))
            GadmTab.COZINHA -> KitchenScreen(state.orders, onOpenOrder, Modifier.padding(padding))
            GadmTab.TORRE -> DispatchScreen(state.orders, onOpenOrder, Modifier.padding(padding))
            GadmTab.AJUSTES -> SettingsScreen(state, onLogout, Modifier.padding(padding))
        }
    }
}

@Composable
private fun BottomTabs(selected: GadmTab, onSelected: (GadmTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabButton(GadmTab.DASHBOARD, selected, Icons.Outlined.Dashboard, onSelected)
        TabButton(GadmTab.PEDIDOS, selected, Icons.Outlined.Assignment, onSelected)
        TabButton(GadmTab.COZINHA, selected, Icons.Outlined.Kitchen, onSelected)
        TabButton(GadmTab.TORRE, selected, Icons.Outlined.LocalShipping, onSelected)
        TabButton(GadmTab.AJUSTES, selected, Icons.Outlined.Settings, onSelected)
    }
}

@Composable
private fun RowScope.TabButton(tab: GadmTab, selected: GadmTab, icon: androidx.compose.ui.graphics.vector.ImageVector, onSelected: (GadmTab) -> Unit) {
    val active = tab == selected
    Column(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { onSelected(tab) }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, tab.label, tint = if (active) Accent else Color(0xFF827B74))
        Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp, fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold, color = if (active) Accent else Color(0xFF827B74))
    }
}

@Composable
private fun DashboardScreen(orders: List<OperationalOrder>, onOpenOrder: (OperationalOrder) -> Unit, modifier: Modifier = Modifier) {
    val active = orders.filter { it.status !in setOf(OrderStatus.ENTREGUE, OrderStatus.CANCELADO) }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Operação agora", fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Dados reais da coleção pedidos. Sem pedidos de demonstração.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        item { StatusSummary(orders) }
        item { Text("Mais urgentes", fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp)) }
        if (active.isEmpty()) item { EmptyState("Nenhum pedido operacional no momento.", "Quando o Cliente criar um pedido no Firebase, ele aparece aqui em tempo real.") }
        items(active.take(8), key = { it.id }) { order -> OrderCard(order, onOpenOrder) }
    }
}

@Composable
private fun StatusSummary(orders: List<OperationalOrder>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Recebidos", orders.count { it.status == OrderStatus.RECEBIDO }, Accent, Modifier.weight(1f))
            SummaryCard("Preparo", orders.count { it.status == OrderStatus.EM_PREPARO }, Warning, Modifier.weight(1f))
            SummaryCard("Em rota", orders.count { it.status == OrderStatus.EM_ROTA }, Success, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Prontos", orders.count { it.status == OrderStatus.PRONTO }, Color(0xFF526EAE), Modifier.weight(1f))
            SummaryCard("Despacho", orders.count { it.status == OrderStatus.AGUARDANDO_ENTREGADOR }, Color(0xFF6B4CA1), Modifier.weight(1f))
            SummaryCard("Hoje", orders.count { it.status == OrderStatus.ENTREGUE }, Color(0xFF59635C), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Text(value.toString(), fontSize = 25.sp, fontWeight = FontWeight.Black, color = accent)
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF706A65))
        }
    }
}

@Composable
private fun OrdersScreen(orders: List<OperationalOrder>, onOpenOrder: (OperationalOrder) -> Unit, modifier: Modifier = Modifier) {
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    val visible = orders.filter { selectedStatus == null || it.status == selectedStatus }
    Column(modifier.fillMaxSize()) {
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Pedidos", fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("Controle por etapas. Nenhuma tela altera status sem regra.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedStatus == null, onClick = { selectedStatus = null }, label = { Text("Todos") })
                        FilterChip(selected = selectedStatus == OrderStatus.RECEBIDO, onClick = { selectedStatus = OrderStatus.RECEBIDO }, label = { Text("Recebidos") })
                        FilterChip(selected = selectedStatus == OrderStatus.EM_PREPARO, onClick = { selectedStatus = OrderStatus.EM_PREPARO }, label = { Text("Preparo") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedStatus == OrderStatus.PRONTO, onClick = { selectedStatus = OrderStatus.PRONTO }, label = { Text("Prontos") })
                        FilterChip(selected = selectedStatus == OrderStatus.AGUARDANDO_ENTREGADOR, onClick = { selectedStatus = OrderStatus.AGUARDANDO_ENTREGADOR }, label = { Text("Torre") })
                        FilterChip(selected = selectedStatus == OrderStatus.EM_ROTA, onClick = { selectedStatus = OrderStatus.EM_ROTA }, label = { Text("Em rota") })
                    }
                }
            }
            if (visible.isEmpty()) item { EmptyState("Nada nessa fila.", "Altere o filtro ou aguarde novos pedidos reais.") }
            items(visible, key = { it.id }) { OrderCard(it, onOpenOrder) }
        }
    }
}

@Composable
private fun KitchenScreen(orders: List<OperationalOrder>, onOpenOrder: (OperationalOrder) -> Unit, modifier: Modifier = Modifier) {
    val queue = orders.filter { it.status == OrderStatus.CONFIRMADO || it.status == OrderStatus.EM_PREPARO || it.status == OrderStatus.PRONTO }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Cozinha", fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Apenas pedidos confirmados, em preparo ou prontos.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        if (queue.isEmpty()) item { EmptyState("Cozinha livre.", "Nenhum pedido confirmado entrou na fila de produção.") }
        items(queue, key = { it.id }) { OrderCard(it, onOpenOrder) }
    }
}

@Composable
private fun DispatchScreen(orders: List<OperationalOrder>, onOpenOrder: (OperationalOrder) -> Unit, modifier: Modifier = Modifier) {
    val queue = orders.filter { it.status == OrderStatus.PRONTO || it.status == OrderStatus.AGUARDANDO_ENTREGADOR || it.status == OrderStatus.EM_ROTA }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Torre de despacho", fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Despache pedido pronto e acompanhe o que já saiu para entrega.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        if (queue.isEmpty()) item { EmptyState("Nenhuma corrida para organizar.", "Pedidos prontos aparecem aqui automaticamente.") }
        items(queue, key = { it.id }) { OrderCard(it, onOpenOrder) }
    }
}

@Composable
private fun SettingsScreen(state: GadmUiState, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Ajustes", fontSize = 26.sp, fontWeight = FontWeight.Black)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(state.currentUser?.name ?: "Gestor", fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(state.currentUser?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssistChip(onClick = {}, label = { Text("${state.currentUser?.role ?: ""}") })
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Base rígida desta versão", fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("• Login real Firebase Auth\n• Perfil ADMIN/GESTOR obrigatório\n• Pedidos em tempo real\n• Transições de status bloqueadas por regra\n• Histórico operacional salvo no pedido\n• Nenhum dado de exemplo embutido", lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Outlined.Logout, "Sair")
            Spacer(Modifier.width(8.dp))
            Text("ENCERRAR SESSÃO", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun OrderCard(order: OperationalOrder, onOpen: (OperationalOrder) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(order) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (order.priority) BorderStroke(1.dp, Accent.copy(alpha = 0.55f)) else null
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(order.code, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(order.customerName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StatusPill(order.status)
            }
            Text(order.items.take(2).joinToString(" • ") { "${it.quantity}x ${it.name}" }.ifBlank { "Itens não informados" }, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(order.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            HorizontalDivider(color = Color(0xFFF0ECE7))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(money(order.total), fontWeight = FontWeight.Black)
                Text(order.createdAt?.let { dateTime(it.toDate()) } ?: "Agora", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (order.priority) Text("PRIORIDADE", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
        }
    }
}

@Composable
private fun StatusPill(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.RECEBIDO -> Accent
        OrderStatus.CONFIRMADO -> Color(0xFF526EAE)
        OrderStatus.EM_PREPARO -> Warning
        OrderStatus.PRONTO -> Color(0xFF6B4CA1)
        OrderStatus.AGUARDANDO_ENTREGADOR -> Color(0xFF7655A7)
        OrderStatus.EM_ROTA -> Success
        OrderStatus.ENTREGUE -> Color(0xFF586D60)
        OrderStatus.CANCELADO -> Color(0xFF8E3430)
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(status.label.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun OrderDetailScreen(
    state: GadmUiState,
    order: OperationalOrder,
    onBack: () -> Unit,
    onStatus: (OrderStatus, String) -> Unit,
    onAssign: (String, String) -> Unit,
    onPriority: (Boolean) -> Unit,
    onDismissMessage: () -> Unit
) {
    var driverName by remember(order.id) { mutableStateOf(order.assignedDriverName) }
    var driverId by remember(order.id) { mutableStateOf(order.assignedDriverId) }
    var cancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(order.code, fontWeight = FontWeight.Black) },
                navigationIcon = { TextButton(onClick = onBack) { Text("VOLTAR", fontWeight = FontWeight.Black) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.error?.let { ErrorPanel(it, onDismissMessage) }
            state.info?.let { InfoPanel(it, onDismissMessage) }

            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(order.customerName, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        StatusPill(order.status)
                    }
                    if (order.customerPhone.isNotBlank()) Text(order.customerPhone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.address, fontWeight = FontWeight.SemiBold)
                    if (order.reference.isNotBlank()) Text("Referência: ${order.reference}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (order.notes.isNotBlank()) Text("Observação: ${order.notes}", fontSize = 13.sp, color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }

            SectionCard("Itens do pedido") {
                if (order.items.isEmpty()) Text("Itens não registrados nesse pedido.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                order.items.forEach { item -> ItemRow(item) }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Entrega", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(money(order.deliveryFee), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(money(order.total), fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
                Text("Pagamento: ${order.payment}${if (order.changeFor.isNotBlank()) " • troco para ${order.changeFor}" else ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (order.status == OrderStatus.PRONTO || order.status == OrderStatus.AGUARDANDO_ENTREGADOR) {
                SectionCard("Despacho") {
                    OutlinedTextField(value = driverName, onValueChange = { driverName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nome do entregador") }, singleLine = true, enabled = !state.busy)
                    OutlinedTextField(value = driverId, onValueChange = { driverId = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ID do entregador (opcional)") }, singleLine = true, enabled = !state.busy)
                    Button(onClick = { onAssign(driverName, driverId) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text("DEFINIR ENTREGADOR", fontWeight = FontWeight.Black)
                    }
                }
            }

            SectionCard("Ações permitidas") {
                OutlinedButton(onClick = { onPriority(!order.priority) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(if (order.priority) "REMOVER PRIORIDADE" else "MARCAR COMO PRIORIDADE", fontWeight = FontWeight.Black)
                }
                val transitions = GadmViewModel.allowedTransitions(order.status)
                transitions.filter { it != OrderStatus.CANCELADO }.forEach { target ->
                    Button(
                        onClick = { onStatus(target, "") },
                        enabled = !state.busy && (target != OrderStatus.EM_ROTA || order.assignedDriverName.isNotBlank()),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorForAction(target))
                    ) {
                        Text(actionLabel(target), fontWeight = FontWeight.Black)
                    }
                }
                if (OrderStatus.CANCELADO in transitions) {
                    OutlinedButton(onClick = { cancelDialog = true }, enabled = !state.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)) {
                        Text("CANCELAR PEDIDO", fontWeight = FontWeight.Black)
                    }
                }
                if (order.status == OrderStatus.EM_ROTA && order.assignedDriverName.isBlank()) {
                    Text("Para enviar à rota, primeiro defina o entregador na torre.", color = Accent, fontSize = 12.sp)
                }
                if (transitions.isEmpty()) Text("Este pedido está encerrado e não aceita novas mudanças.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (cancelDialog) {
        AlertDialog(
            onDismissRequest = { cancelDialog = false },
            title = { Text("Cancelar ${order.code}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("O cancelamento fica registrado no histórico operacional.")
                    OutlinedTextField(value = cancelReason, onValueChange = { cancelReason = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Motivo") }, minLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = { cancelDialog = false; onStatus(OrderStatus.CANCELADO, cancelReason) }) { Text("CONFIRMAR", color = Accent, fontWeight = FontWeight.Black) } },
            dismissButton = { TextButton(onClick = { cancelDialog = false }) { Text("VOLTAR") } }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black)
            content()
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text("${item.quantity}x", color = Accent, fontWeight = FontWeight.Black, modifier = Modifier.width(36.dp))
        Column {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            if (item.details.isNotBlank()) Text(item.details, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Black)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ErrorPanel(text: String, onDismiss: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8E5))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, modifier = Modifier.weight(1f), color = Color(0xFF8C211A), fontSize = 13.sp)
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    }
}

@Composable
private fun InfoPanel(text: String, onDismiss: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6EE))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, modifier = Modifier.weight(1f), color = Success, fontSize = 13.sp)
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    }
}

private fun actionLabel(target: OrderStatus): String = when (target) {
    OrderStatus.CONFIRMADO -> "CONFIRMAR PEDIDO"
    OrderStatus.EM_PREPARO -> "INICIAR PREPARO"
    OrderStatus.PRONTO -> "MARCAR COMO PRONTO"
    OrderStatus.AGUARDANDO_ENTREGADOR -> "ENVIAR PARA A TORRE"
    OrderStatus.EM_ROTA -> "MARCAR EM ROTA"
    OrderStatus.ENTREGUE -> "CONFIRMAR ENTREGA"
    else -> target.label.uppercase()
}

private fun colorForAction(target: OrderStatus): Color = when (target) {
    OrderStatus.CONFIRMADO -> Color(0xFF526EAE)
    OrderStatus.EM_PREPARO -> Warning
    OrderStatus.PRONTO, OrderStatus.AGUARDANDO_ENTREGADOR -> Color(0xFF6B4CA1)
    OrderStatus.EM_ROTA, OrderStatus.ENTREGUE -> Success
    else -> Accent
}

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
private fun dateTime(date: Date): String = SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(date)
