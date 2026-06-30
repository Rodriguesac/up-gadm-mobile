package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.gadm.AppVersion
import com.rodriguesacai.gadm.R
import com.rodriguesacai.gadm.data.GadmCustomer
import com.rodriguesacai.gadm.data.GadmDriver
import com.rodriguesacai.gadm.data.GadmIncident
import com.rodriguesacai.gadm.data.GadmOrder
import com.rodriguesacai.gadm.data.GadmProduct
import com.rodriguesacai.gadm.data.StoreOperation
import com.rodriguesacai.gadm.data.dateTime
import com.rodriguesacai.gadm.data.money

private enum class GadmPage(val label: String, val icon: ImageVector) {
    DASHBOARD("Painel", Icons.Filled.Dashboard),
    ORDERS("Pedidos", Icons.Filled.ShoppingBag),
    KITCHEN("Cozinha", Icons.Filled.Kitchen),
    TOWER("Torre", Icons.Filled.DeliveryDining),
    DRIVERS("Entregadores", Icons.Filled.DeliveryDining),
    CUSTOMERS("Clientes", Icons.Filled.Group),
    INCIDENTS("Ocorrências", Icons.Filled.WarningAmber),
    FINANCE("Financeiro", Icons.Filled.AccountBalanceWallet),
    CATALOG("Cardápio", Icons.Filled.Inventory2),
    COMMUNICATIONS("Comunicados", Icons.Filled.Campaign),
    SETTINGS("Configurações", Icons.Filled.Settings),
    SECURITY("Segurança", Icons.Filled.Key),
    MORE("Mais", Icons.Filled.MoreHoriz)
}

@Composable
fun UpGestorApp(viewModel: GadmViewModel) {
    val state = viewModel.state
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    if (state.user == null) {
        GadmLoginScreen(onLogin = viewModel::login)
    } else {
        GadmShell(state = state, viewModel = viewModel, snackbar = snackbar)
    }
}

@Composable
private fun GadmLoginScreen(onLogin: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxSize(), color = GadmNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = GadmSurfaceStrong,
                border = BorderStroke(1.dp, GadmBlue.copy(alpha = .55f))
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = GadmNavy
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = GadmLime,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("UP GADM", color = GadmWhite, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("Comando da operação", color = GadmMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(26.dp))
                    Text("Acesso administrativo", color = GadmWhite, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 5 && it.all(Char::isDigit)) pin = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("PIN de 5 números") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = appTextFieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "ENTRAR NO GESTOR",
                        icon = Icons.Filled.LockOpen,
                        enabled = pin.length == 5,
                        onClick = { onLogin(pin) }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Primeiro acesso: PIN 12345. Troque em Segurança assim que entrar.", color = GadmMuted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(AppVersion.NAME, color = GadmMuted, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GadmShell(state: GadmUiState, viewModel: GadmViewModel, snackbar: SnackbarHostState) {
    var page by remember { mutableStateOf(GadmPage.DASHBOARD) }
    val bottomPages = listOf(GadmPage.DASHBOARD, GadmPage.ORDERS, GadmPage.DRIVERS, GadmPage.MORE)
    val canGoBack = page !in bottomPages

    Scaffold(
        containerColor = GadmNavy,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(page.label, color = GadmWhite, fontWeight = FontWeight.Black)
                        if (page == GadmPage.DASHBOARD) {
                            Text(
                                if (state.operation.open && state.operation.acceptOrders) "Operação aberta" else "Operação controlada",
                                color = if (state.operation.open) GadmSuccess else GadmYellow,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { page = GadmPage.DASHBOARD }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = GadmWhite)
                        }
                    }
                },
                actions = {
                    if (page != GadmPage.SECURITY) {
                        IconButton(onClick = { page = GadmPage.COMMUNICATIONS }) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = GadmLime)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = GadmNavy)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = GadmSurfaceStrong) {
                bottomPages.forEach { item ->
                    NavigationBarItem(
                        selected = page == item || (item == GadmPage.MORE && page !in bottomPages),
                        onClick = { page = item },
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = GadmNavy,
                            selectedTextColor = GadmLime,
                            indicatorColor = GadmLime,
                            unselectedIconColor = GadmMuted,
                            unselectedTextColor = GadmMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                GadmPage.DASHBOARD -> DashboardScreen(state, onNavigate = { page = it })
                GadmPage.ORDERS -> OrdersScreen(state, viewModel, onNavigate = { page = it })
                GadmPage.KITCHEN -> KitchenScreen(state, viewModel)
                GadmPage.TOWER -> TowerScreen(state, viewModel)
                GadmPage.DRIVERS -> DriversScreen(state, viewModel)
                GadmPage.CUSTOMERS -> CustomersScreen(state)
                GadmPage.INCIDENTS -> IncidentsScreen(state, viewModel)
                GadmPage.FINANCE -> FinanceScreen(state)
                GadmPage.CATALOG -> CatalogScreen(state, viewModel)
                GadmPage.COMMUNICATIONS -> CommunicationsScreen(viewModel)
                GadmPage.SETTINGS -> SettingsScreen(state.operation, viewModel)
                GadmPage.SECURITY -> SecurityScreen(state, viewModel)
                GadmPage.MORE -> MoreScreen(onNavigate = { page = it }, onLogout = viewModel::logout)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardScreen(state: GadmUiState, onNavigate: (GadmPage) -> Unit) {
    val newOrders = state.orders.count { it.currentStage == "Novo" }
    val preparing = state.orders.count { it.currentStage == "Em preparo" }
    val delivery = state.orders.count { it.currentStage == "Em entrega" }
    val availableDrivers = state.drivers.count { it.availabilityLabel == "Disponível" }
    val pendingOffers = state.drivers.count { it.availabilityLabel == "Oferta enviada" }
    val activeIncidents = state.incidents.count { !it.status.equals("RESOLVIDA", true) && !it.status.equals("CANCELADA", true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OperationBanner(operation = state.operation, onClick = { onNavigate(GadmPage.SETTINGS) })
        }
        item {
            Text("Visão da operação", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 19.sp)
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile("Novos pedidos", newOrders.toString(), Icons.Filled.ReceiptLong, GadmLime) { onNavigate(GadmPage.ORDERS) }
                SummaryTile("Em preparo", preparing.toString(), Icons.Filled.Kitchen, GadmBlue) { onNavigate(GadmPage.KITCHEN) }
                SummaryTile("Em entrega", delivery.toString(), Icons.Filled.DeliveryDining, GadmYellow) { onNavigate(GadmPage.TOWER) }
                SummaryTile("Disponíveis", availableDrivers.toString(), Icons.Filled.Person, GadmSuccess) { onNavigate(GadmPage.DRIVERS) }
                SummaryTile("Ofertas", pendingOffers.toString(), Icons.Filled.NotificationsActive, GadmYellow) { onNavigate(GadmPage.DRIVERS) }
            }
        }
        if (activeIncidents > 0) {
            item {
                AlertRow(
                    title = "$activeIncidents ocorrência${if (activeIncidents > 1) "s" else ""} precisa${if (activeIncidents > 1) "m" else ""} de decisão",
                    subtitle = "Resolva, cancele ou libere o entregador sem deixar corrida presa.",
                    onClick = { onNavigate(GadmPage.INCIDENTS) }
                )
            }
        }
        item { Text("Ações rápidas", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 19.sp) }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAction("Pedidos", "Aceitar e preparar", Icons.Filled.ShoppingBag) { onNavigate(GadmPage.ORDERS) }
                QuickAction("Cozinha", "Iniciar e finalizar", Icons.Filled.Kitchen) { onNavigate(GadmPage.KITCHEN) }
                QuickAction("Torre", "Enviar corrida", Icons.Filled.SyncAlt) { onNavigate(GadmPage.TOWER) }
                QuickAction("Entregadores", "Aprovar e liberar", Icons.Filled.Approval) { onNavigate(GadmPage.DRIVERS) }
                QuickAction("Clientes", "Dados e pedidos", Icons.Filled.Group) { onNavigate(GadmPage.CUSTOMERS) }
                QuickAction("Comunicados", "Controlar app", Icons.Filled.Campaign) { onNavigate(GadmPage.COMMUNICATIONS) }
            }
        }
        item { Text("Fila ao vivo", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 19.sp) }
        if (state.orders.isEmpty()) {
            item { EmptyCard("Ainda não há pedidos no Firebase.", "Quando o Cliente criar um pedido ele aparecerá aqui em tempo real.") }
        } else {
            items(state.orders.sortedByDescending { it.createdAt }.take(5), key = { it.id }) { order ->
                CompactOrderCard(order, onClick = { onNavigate(GadmPage.ORDERS) })
            }
        }
    }
}

@Composable
private fun OrdersScreen(state: GadmUiState, viewModel: GadmViewModel, onNavigate: (GadmPage) -> Unit) {
    var filter by remember { mutableStateOf("Todos") }
    var selected by remember { mutableStateOf<GadmOrder?>(null) }
    val options = listOf("Todos", "Novos", "Em preparo", "Pronto", "Na torre", "Em entrega", "Cancelado")
    val list = state.orders.filter { filter == "Todos" || it.currentStage == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pedidos em tempo real", color = GadmMuted, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option) })
                }
            }
        }
        if (list.isEmpty()) {
            item { EmptyCard("Nenhum pedido nesta fila.", "Use os filtros acima para navegar pelos status.") }
        } else {
            items(list.sortedByDescending { it.createdAt }, key = { it.id }) { order ->
                OrderCard(order = order, onClick = { selected = order }, onPrimary = {
                    when (order.currentStage) {
                        "Novo" -> viewModel.acceptOrder(order)
                        "Em preparo" -> viewModel.finishKitchen(order)
                        "Pronto" -> viewModel.sendToTower(order)
                        "Na torre" -> onNavigate(GadmPage.TOWER)
                        else -> selected = order
                    }
                })
            }
        }
    }
    selected?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selected = null },
            onAccept = { viewModel.acceptOrder(order); selected = null },
            onStartKitchen = { viewModel.startKitchen(order); selected = null },
            onFinishKitchen = { viewModel.finishKitchen(order); selected = null },
            onTower = { viewModel.sendToTower(order); selected = null; onNavigate(GadmPage.TOWER) },
            onCancel = { reason -> viewModel.rejectOrder(order, reason); selected = null }
        )
    }
}

@Composable
private fun KitchenScreen(state: GadmUiState, viewModel: GadmViewModel) {
    val kitchenOrders = state.orders.filter { it.currentStage in setOf("Novo", "Em preparo", "Pronto") && !it.orderStatus.equals("CANCELADO", true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Kitchen, null, tint = GadmLime)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Produção", color = GadmWhite, fontWeight = FontWeight.Black)
                        Text("Inicie somente pedidos confirmados e marque pronto ao finalizar.", color = GadmMuted, fontSize = 13.sp)
                    }
                }
            }
        }
        if (kitchenOrders.isEmpty()) item { EmptyCard("Cozinha livre.", "Pedidos confirmados aparecerão aqui automaticamente.") }
        items(kitchenOrders.sortedByDescending { it.createdAt }, key = { it.id }) { order ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(order.code, color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text(order.customerName, color = GadmMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(order.itemsLabel, color = GadmWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    StatusPill(order.currentStage)
                }
                Spacer(Modifier.height(12.dp))
                when (order.currentStage) {
                    "Novo" -> PrimaryButton("ACEITAR E INICIAR PREPARO", Icons.Filled.Approval) { viewModel.acceptOrder(order) }
                    "Em preparo" -> PrimaryButton("MARCAR COMO PRONTO", Icons.Filled.Restaurant) { viewModel.finishKitchen(order) }
                    "Pronto" -> SecondaryButton("ENVIAR PARA TORRE", Icons.Filled.DeliveryDining) { viewModel.sendToTower(order) }
                    else -> PrimaryButton("INICIAR PREPARO", Icons.Filled.Kitchen) { viewModel.startKitchen(order) }
                }
            }
        }
    }
}

@Composable
private fun TowerScreen(state: GadmUiState, viewModel: GadmViewModel) {
    var selectedOrder by remember { mutableStateOf<GadmOrder?>(null) }
    val readyOrders = state.orders.filter { it.currentStage == "Pronto" || it.currentStage == "Na torre" }
    val eligibleDrivers = state.drivers.filter {
        it.approved && !it.blocked &&
            it.currentOrderId.isBlank() && it.currentRideId.isBlank() &&
            it.pendingOfferOrderId.isBlank() && it.pendingOfferRideId.isBlank()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SyncAlt, null, tint = GadmLime)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Torre de despacho", color = GadmWhite, fontWeight = FontWeight.Black)
                        Text("${eligibleDrivers.size} entregador(es) apto(s) sem corrida ativa.", color = GadmMuted, fontSize = 13.sp)
                    }
                }
            }
        }
        if (readyOrders.isEmpty()) item { EmptyCard("Nenhum pedido aguardando despacho.", "Pedidos prontos ou enviados para a torre aparecerão aqui.") }
        items(readyOrders, key = { it.id }) { order ->
            AppCard {
                Text(order.code, color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(order.customerName, color = GadmMuted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text(order.address.ifBlank { "Endereço não informado" }, color = GadmWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(order.currentStage)
                    StatusPill(money(order.total), accent = GadmYellow)
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("ESCOLHER ENTREGADOR", Icons.Filled.DeliveryDining, enabled = eligibleDrivers.isNotEmpty()) { selectedOrder = order }
            }
        }
    }
    selectedOrder?.let { order ->
        AssignDriverDialog(order = order, drivers = eligibleDrivers, onDismiss = { selectedOrder = null }) { driver ->
            viewModel.assignDriver(order, driver)
            selectedOrder = null
        }
    }
}

@Composable
private fun DriversScreen(state: GadmUiState, viewModel: GadmViewModel) {
    var filter by remember { mutableStateOf("Todos") }
    var selected by remember { mutableStateOf<GadmDriver?>(null) }
    val options = listOf("Todos", "Em análise", "Disponível", "Oferta enviada", "Em corrida", "Bloqueado", "Offline")
    val drivers = state.drivers.filter { filter == "Todos" || it.availabilityLabel == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Cadastros, documentos e operação", color = GadmMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option -> FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option) }) }
            }
        }
        if (drivers.isEmpty()) item { EmptyCard("Nenhum entregador nesta fila.", "Cadastros aprovados e em análise aparecerão aqui.") }
        items(drivers.sortedByDescending { it.createdAt }, key = { it.id }) { driver ->
            DriverCard(driver, onClick = { selected = driver })
        }
    }
    selected?.let { driver ->
        DriverDetailDialog(
            driver = driver,
            onDismiss = { selected = null },
            onApprove = { viewModel.approveDriver(driver); selected = null },
            onCorrection = { reason -> viewModel.requestDriverCorrection(driver, reason); selected = null },
            onBlock = { reason -> viewModel.blockDriver(driver, reason); selected = null },
            onUnblock = { viewModel.unblockDriver(driver); selected = null },
            onCancelOffer = { viewModel.cancelPendingOffer(driver); selected = null },
            onRelease = { viewModel.releaseDriver(driver); selected = null }
        )
    }
}

@Composable
private fun CustomersScreen(state: GadmUiState) {
    var search by remember { mutableStateOf("") }
    val list = state.customers.filter { it.name.contains(search, true) || it.phone.contains(search) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Buscar cliente por nome ou telefone") }, singleLine = true, colors = appTextFieldColors())
        }
        if (list.isEmpty()) item { EmptyCard("Nenhum cliente encontrado.", "Os clientes do app/site aparecem nesta lista quando gravados em /clientes.") }
        items(list.sortedByDescending { it.lastOrderAt }, key = { it.id }) { customer -> CustomerCard(customer) }
    }
}

@Composable
private fun IncidentsScreen(state: GadmUiState, viewModel: GadmViewModel) {
    var selected by remember { mutableStateOf<GadmIncident?>(null) }
    val incidents = state.incidents.filter { !it.status.equals("RESOLVIDA", true) && !it.status.equals("CANCELADA", true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AlertRow("Ocorrência não é corrida ativa", "Ao resolver, o GADM pode liberar o entregador e limpar vínculos presos.", onClick = {})
        }
        if (incidents.isEmpty()) item { EmptyCard("Nenhuma ocorrência aberta.", "As ocorrências que chegarem do app entregador aparecerão aqui.") }
        items(incidents.sortedByDescending { it.createdAt }, key = { it.id }) { incident ->
            IncidentCard(incident, onClick = { selected = incident })
        }
    }
    selected?.let { incident ->
        IncidentDetailDialog(incident, onDismiss = { selected = null }) { release ->
            viewModel.resolveIncident(incident, release)
            selected = null
        }
    }
}

@Composable
private fun FinanceScreen(state: GadmUiState) {
    val income = state.finance.filter { it.type.uppercase() in setOf("ENTRADA", "RECEITA", "PEDIDO") }.sumOf { it.amount }
    val payout = state.finance.filter { it.type.uppercase() in setOf("REPASSE", "SAIDA", "PAGAMENTO") }.sumOf { it.amount }
    val pending = state.finance.filter { it.status.equals("PENDENTE", true) }.sumOf { it.amount }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Text("Financeiro operacional", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Taxa de entrega do cliente e repasse do entregador são campos separados.", color = GadmMuted, fontSize = 13.sp)
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("Entradas", money(income), Icons.Filled.ReceiptLong, GadmSuccess) {}
                SummaryTile("Repasses", money(payout), Icons.Filled.AccountBalanceWallet, GadmYellow) {}
                SummaryTile("Pendentes", money(pending), Icons.Filled.WarningAmber, GadmDanger) {}
            }
        }
        item { Text("Movimentos recentes", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 19.sp) }
        if (state.finance.isEmpty()) item { EmptyCard("Ainda não há movimentos financeiros.", "Registros em /financeiro_movimentos aparecerão aqui.") }
        items(state.finance.sortedByDescending { it.createdAt }.take(50), key = { it.id }) { entry ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.description, color = GadmWhite, fontWeight = FontWeight.Bold)
                        Text("${entry.type} • ${dateTime(entry.createdAt)}", color = GadmMuted, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(money(entry.amount), color = if (entry.type.uppercase() in setOf("SAIDA", "REPASSE")) GadmYellow else GadmSuccess, fontWeight = FontWeight.Black)
                        StatusPill(entry.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogScreen(state: GadmUiState, viewModel: GadmViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Text("Cardápio e estoque", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Pause itens sem apagar seu histórico de vendas.", color = GadmMuted, fontSize = 13.sp)
            }
        }
        if (state.products.isEmpty()) item { EmptyCard("Nenhum produto sincronizado.", "Produtos em /produtos aparecerão aqui para pausar ou liberar.") }
        items(state.products.sortedBy { it.name }, key = { it.id }) { product ->
            ProductCard(product, onToggle = { viewModel.toggleProduct(product, !product.paused) })
        }
    }
}

@Composable
private fun CommunicationsScreen(viewModel: GadmViewModel) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("COMUNICADO") }
    var action by remember { mutableStateOf("NOTIFICACOES") }
    val types = listOf("COMUNICADO", "PROMOCAO", "ALERTA", "MANUTENCAO")
    val actions = listOf("NOTIFICACOES", "COMPLETAR_CADASTRO", "ATIVAR_MAQUININHA", "CADASTRAR_PIX", "CARTEIRA", "SUPORTE")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Text("Comando do app entregador", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("O comunicado aparece no app e pode levar o entregador para a área certa.", color = GadmMuted, fontSize = 13.sp)
            }
        }
        item { OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Título") }, singleLine = true, colors = appTextFieldColors()) }
        item { OutlinedTextField(value = body, onValueChange = { body = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), label = { Text("Mensagem") }, colors = appTextFieldColors()) }
        item {
            Text("Tipo", color = GadmWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { option -> FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) }) }
            }
        }
        item {
            Text("Ação ao tocar", color = GadmWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { option -> FilterChip(selected = action == option, onClick = { action = option }, label = { Text(option.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) }) }
            }
        }
        item {
            PrimaryButton("ENVIAR COMUNICADO", Icons.Filled.Campaign, enabled = title.isNotBlank() && body.isNotBlank()) {
                viewModel.sendCommunication(title.trim(), body.trim(), type, action)
                title = ""
                body = ""
            }
        }
    }
}

@Composable
private fun SettingsScreen(operation: StoreOperation, viewModel: GadmViewModel) {
    var open by remember(operation) { mutableStateOf(operation.open) }
    var accept by remember(operation) { mutableStateOf(operation.acceptOrders) }
    var maintenance by remember(operation) { mutableStateOf(operation.maintenance) }
    var minutes by remember(operation) { mutableStateOf(operation.estimatedMinutes.toString()) }
    var message by remember(operation) { mutableStateOf(operation.message) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Text("Operação da loja", color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Estas chaves são o controle operacional central do GADM.", color = GadmMuted, fontSize = 13.sp)
            }
        }
        item { SwitchRow("Loja aberta", "Permite o atendimento geral.", open) { open = it } }
        item { SwitchRow("Aceitar pedidos", "Cliente pode concluir novos pedidos.", accept) { accept = it } }
        item { SwitchRow("Modo manutenção", "Exibe aviso e bloqueia fluxos definidos pela loja.", maintenance) { maintenance = it } }
        item { OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("Tempo estimado em minutos") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = appTextFieldColors()) }
        item { OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("Mensagem operacional") }, colors = appTextFieldColors()) }
        item {
            PrimaryButton("SALVAR OPERAÇÃO", Icons.Filled.Settings) {
                viewModel.updateOperation(StoreOperation(open, accept, maintenance, message, minutes.toIntOrNull() ?: 45))
            }
        }
    }
}

@Composable
private fun SecurityScreen(state: GadmUiState, viewModel: GadmViewModel) {
    var pin by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Key, null, tint = GadmLime)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Segurança do gestor", color = GadmWhite, fontWeight = FontWeight.Black)
                        Text("Acesso atual: ${state.user?.name ?: "Gestor"} • ${state.user?.role ?: "ADMIN"}", color = GadmMuted, fontSize = 13.sp)
                    }
                }
            }
        }
        item { Text("Trocar PIN administrativo", color = GadmWhite, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = pin, onValueChange = { value -> if (value.length <= 5 && value.all(Char::isDigit)) pin = value }, modifier = Modifier.fillMaxWidth(), label = { Text("Novo PIN com 5 números") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, colors = appTextFieldColors()) }
        item { PrimaryButton("ATUALIZAR PIN", Icons.Filled.Key, enabled = pin.length == 5) { viewModel.changePin(pin); pin = "" } }
        item { EmptyCard("Controle de equipe", "Os próximos usuários administrativos são cadastrados em /usuarios_gadm com nome, perfil, ativo e pinHash.") }
    }
}

@Composable
private fun MoreScreen(onNavigate: (GadmPage) -> Unit, onLogout: () -> Unit) {
    val entries = listOf(
        Triple("Cozinha", "Produção, tempos e fila", GadmPage.KITCHEN),
        Triple("Torre de despacho", "Atribuir pedido a entregador", GadmPage.TOWER),
        Triple("Clientes", "Dados e histórico", GadmPage.CUSTOMERS),
        Triple("Ocorrências", "Resolver e liberar", GadmPage.INCIDENTS),
        Triple("Financeiro", "Movimentos e repasses", GadmPage.FINANCE),
        Triple("Cardápio e estoque", "Pausar produtos", GadmPage.CATALOG),
        Triple("Comunicados", "Controlar o app entregador", GadmPage.COMMUNICATIONS),
        Triple("Configurações", "Loja, pedidos e manutenção", GadmPage.SETTINGS),
        Triple("Segurança", "Trocar PIN administrativo", GadmPage.SECURITY)
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries) { (title, subtitle, target) ->
            MenuRow(title, subtitle, target.icon) { onNavigate(target) }
        }
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, GadmDanger.copy(alpha = .8f))) {
                Icon(Icons.Filled.PersonOff, null, tint = GadmDanger)
                Spacer(Modifier.width(8.dp))
                Text("SAIR DO GESTOR", color = GadmDanger, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OperationBanner(operation: StoreOperation, onClick: () -> Unit) {
    val active = operation.open && operation.acceptOrders && !operation.maintenance
    AppCard(onClick = onClick, borderColor = if (active) GadmLime.copy(alpha = .48f) else GadmYellow.copy(alpha = .55f)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(shape = CircleShape, color = if (active) GadmLime else GadmYellow, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(if (active) Icons.Filled.Store else Icons.Filled.WarningAmber, null, tint = GadmNavy) }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (active) "Loja aberta e recebendo pedidos" else "Operação precisa de atenção", color = GadmWhite, fontWeight = FontWeight.Black)
                    Text(operation.message.ifBlank { "Tempo estimado: ${operation.estimatedMinutes} min" }, color = GadmMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = GadmMuted)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryTile(title: String, value: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.width(164.dp),
        onClick = onClick,
        borderColor = tint.copy(alpha = .26f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = .17f), modifier = Modifier.size(38.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)) }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(value, color = GadmWhite, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(title, color = GadmMuted, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    AppCard(modifier = Modifier.width(166.dp), onClick = onClick) {
        Surface(shape = RoundedCornerShape(14.dp), color = GadmBlue.copy(alpha = .18f), modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = GadmLime) }
        }
        Spacer(Modifier.height(10.dp))
        Text(title, color = GadmWhite, fontWeight = FontWeight.Black, maxLines = 1)
        Text(subtitle, color = GadmMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CompactOrderCard(order: GadmOrder, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(order.code, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(order.customerName, color = GadmMuted, fontSize = 13.sp)
                Text(order.address.ifBlank { "Endereço não informado" }, color = GadmMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(order.currentStage)
                Spacer(Modifier.height(6.dp))
                Text(money(order.total), color = GadmLime, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OrderCard(order: GadmOrder, onClick: () -> Unit, onPrimary: () -> Unit) {
    AppCard(onClick = onClick, borderColor = if (order.priority) GadmYellow.copy(alpha = .75f) else null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.code, color = GadmWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    if (order.priority) {
                        Spacer(Modifier.width(8.dp)); StatusPill("Prioridade", accent = GadmYellow)
                    }
                }
                Text(order.customerName, color = GadmMuted, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(order.itemsLabel, color = GadmWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(order.currentStage)
                Spacer(Modifier.height(7.dp))
                Text(money(order.total), color = GadmLime, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = GadmMuted.copy(alpha = .18f))
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${order.payment} • ${dateTime(order.createdAt)}", color = GadmMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            TextButton(onClick = onPrimary) {
                Text(orderPrimaryText(order), color = GadmLime, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

private fun orderPrimaryText(order: GadmOrder): String = when (order.currentStage) {
    "Novo" -> "ACEITAR + PREPARAR"
    "Em preparo" -> "PRONTO"
    "Pronto" -> "TORRE"
    "Na torre" -> "DESPACHAR"
    else -> "DETALHES"
}

@Composable
private fun DriverCard(driver: GadmDriver, onClick: () -> Unit) {
    AppCard(onClick = onClick, borderColor = when { driver.blocked -> GadmDanger.copy(alpha = .62f); !driver.approved -> GadmYellow.copy(alpha = .58f); else -> null }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = GadmBlue.copy(alpha = .22f)) {
                    Box(contentAlignment = Alignment.Center) { Text(driver.name.take(1).uppercase(), color = GadmLime, fontWeight = FontWeight.Black, fontSize = 19.sp) }
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text(driver.name, color = GadmWhite, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(listOf(driver.vehicle, driver.plate).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { driver.phone }, color = GadmMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            StatusPill(driver.availabilityLabel, accent = driverStatusColor(driver))
        }
        Spacer(Modifier.height(10.dp))
        Text(if (driver.currentOrderId.isNotBlank()) "Pedido atual: ${driver.currentOrderId}" else "Toque para abrir os comandos", color = GadmMuted, fontSize = 12.sp)
    }
}

@Composable
private fun CustomerCard(customer: GadmCustomer) {
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(customer.phone.ifBlank { customer.email.ifBlank { "Sem contato registrado" } }, color = GadmMuted, fontSize = 13.sp)
                if (customer.address.isNotBlank()) Text(customer.address, color = GadmMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${customer.ordersCount} pedidos", color = GadmLime, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text(money(customer.totalSpent), color = GadmWhite, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun IncidentCard(incident: GadmIncident, onClick: () -> Unit) {
    AppCard(onClick = onClick, borderColor = severityColor(incident.severity).copy(alpha = .68f)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(incident.title, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(incident.description.ifBlank { "Sem descrição." }, color = GadmMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Text(listOf(incident.driverName, incident.orderId).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Sem vínculo de corrida" }, color = GadmMuted, fontSize = 12.sp)
            }
            StatusPill(incident.severity, accent = severityColor(incident.severity))
        }
    }
}

@Composable
private fun ProductCard(product: GadmProduct, onToggle: () -> Unit) {
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, color = GadmWhite, fontWeight = FontWeight.Black)
                Text("${product.category.ifBlank { "Sem categoria" }} • Estoque: ${product.stock}", color = GadmMuted, fontSize = 12.sp)
                Text(money(product.price), color = GadmLime, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(if (product.paused) "Pausado" else "Ativo", accent = if (product.paused) GadmYellow else GadmSuccess)
                TextButton(onClick = onToggle) { Text(if (product.paused) "LIBERAR" else "PAUSAR", color = if (product.paused) GadmLime else GadmYellow, fontWeight = FontWeight.Black, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = GadmBlue.copy(alpha = .19f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = GadmLime) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(subtitle, color = GadmMuted, fontSize = 12.sp)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = GadmMuted)
        }
    }
}

@Composable
private fun AlertRow(title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(onClick = onClick, borderColor = GadmYellow.copy(alpha = .60f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.WarningAmber, null, tint = GadmYellow, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(subtitle, color = GadmMuted, fontSize = 13.sp)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = GadmMuted)
        }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    AppCard(borderColor = GadmBlue.copy(alpha = .36f)) {
        Icon(Icons.Filled.Inventory2, null, tint = GadmBlue, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = GadmWhite, fontWeight = FontWeight.Black)
        Text(subtitle, color = GadmMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = GadmWhite, fontWeight = FontWeight.Black)
                Text(subtitle, color = GadmMuted, fontSize = 12.sp)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Color = statusColor(text)) {
    Surface(shape = RoundedCornerShape(100.dp), color = accent.copy(alpha = .17f), border = BorderStroke(1.dp, accent.copy(alpha = .40f))) {
        Text(text, color = accent, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), maxLines = 1)
    }
}

private fun statusColor(text: String): Color = when (text.uppercase()) {
    "NOVO", "RECEBIDO", "DISPONÍVEL", "ATIVO", "CONFIRMADO" -> GadmLime
    "EM PREPARO", "EM ENTREGA", "NA TORRE", "EM CORRIDA" -> GadmBlue
    "OFERTA ENVIADA", "PRONTO", "PRIORIDADE", "PAUSADO", "OFFLINE", "PENDENTE" -> GadmYellow
    "CANCELADO", "BLOQUEADO", "ALTA" -> GadmDanger
    else -> GadmMuted
}

private fun driverStatusColor(driver: GadmDriver): Color = when {
    driver.blocked -> GadmDanger
    !driver.approved -> GadmYellow
    driver.availabilityLabel == "Disponível" -> GadmSuccess
    driver.availabilityLabel == "Oferta enviada" -> GadmYellow
    driver.availabilityLabel == "Em corrida" -> GadmBlue
    else -> GadmMuted
}

private fun severityColor(severity: String): Color = when (severity.uppercase()) {
    "ALTA", "CRITICA", "CRÍTICA" -> GadmDanger
    "MEDIA", "MÉDIA" -> GadmYellow
    else -> GadmBlue
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val click = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Card(
        modifier = modifier.then(click),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GadmSurface),
        border = borderColor?.let { BorderStroke(1.dp, it) } ?: BorderStroke(1.dp, GadmMuted.copy(alpha = .10f))
    ) {
        Column(modifier = Modifier.padding(15.dp), content = content)
    }
}

@Composable
private fun PrimaryButton(text: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy, disabledContainerColor = GadmMuted.copy(alpha = .30f), disabledContentColor = GadmNavy.copy(alpha = .5f)),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun SecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, GadmLime.copy(alpha = .65f)),
        contentPadding = PaddingValues(vertical = 13.dp)
    ) {
        Icon(icon, null, tint = GadmLime, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = GadmLime, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GadmWhite,
    unfocusedTextColor = GadmWhite,
    focusedBorderColor = GadmLime,
    unfocusedBorderColor = GadmMuted.copy(alpha = .45f),
    focusedLabelColor = GadmLime,
    unfocusedLabelColor = GadmMuted,
    cursorColor = GadmLime,
    focusedContainerColor = GadmSurface,
    unfocusedContainerColor = GadmSurface
)

@Composable
private fun OrderDetailDialog(
    order: GadmOrder,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onStartKitchen: () -> Unit,
    onFinishKitchen: () -> Unit,
    onTower: () -> Unit,
    onCancel: (String) -> Unit
) {
    var cancelMode by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GadmSurfaceStrong,
        titleContentColor = GadmWhite,
        textContentColor = GadmMuted,
        title = { Text(order.code, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(order.customerName, color = GadmWhite, fontWeight = FontWeight.Bold)
                Text(order.itemsLabel)
                Text(order.address.ifBlank { "Endereço não informado" })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatusPill(order.currentStage); StatusPill(money(order.total), accent = GadmLime) }
                if (cancelMode) OutlinedTextField(value = reason, onValueChange = { reason = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Motivo do cancelamento") }, colors = appTextFieldColors())
            }
        },
        confirmButton = {
            when {
                cancelMode -> TextButton(onClick = { onCancel(reason.ifBlank { "Cancelado pelo gestor" }) }) { Text("CONFIRMAR CANCELAMENTO", color = GadmDanger, fontWeight = FontWeight.Black) }
                order.currentStage == "Novo" -> TextButton(onClick = onAccept) { Text("ACEITAR E PREPARAR", color = GadmLime, fontWeight = FontWeight.Black) }
                order.currentStage != "Em preparo" && order.currentStage != "Pronto" -> TextButton(onClick = onStartKitchen) { Text("INICIAR PREPARO", color = GadmLime, fontWeight = FontWeight.Black) }
                order.currentStage == "Em preparo" -> TextButton(onClick = onFinishKitchen) { Text("MARCAR PRONTO", color = GadmLime, fontWeight = FontWeight.Black) }
                else -> TextButton(onClick = onTower) { Text("ENVIAR PARA TORRE", color = GadmLime, fontWeight = FontWeight.Black) }
            }
        },
        dismissButton = {
            Row {
                if (!cancelMode) TextButton(onClick = { cancelMode = true }) { Text("CANCELAR", color = GadmDanger) }
                TextButton(onClick = onDismiss) { Text("FECHAR", color = GadmMuted) }
            }
        }
    )
}

@Composable
private fun AssignDriverDialog(order: GadmOrder, drivers: List<GadmDriver>, onDismiss: () -> Unit, onAssign: (GadmDriver) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GadmSurfaceStrong,
        titleContentColor = GadmWhite,
        textContentColor = GadmMuted,
        title = { Text("Escolher entregador", fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text("${order.code} • ${order.customerName}")
                Spacer(Modifier.height(10.dp))
                if (drivers.isEmpty()) Text("Nenhum entregador apto sem corrida ativa.", color = GadmYellow)
                else LazyColumn(modifier = Modifier.heightIn(max = 330.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(drivers, key = { it.id }) { driver ->
                        AppCard(onClick = { onAssign(driver) }, borderColor = GadmLime.copy(alpha = .34f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = GadmBlue.copy(alpha = .2f), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Text(driver.name.take(1).uppercase(), color = GadmLime, fontWeight = FontWeight.Black) } }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(driver.name, color = GadmWhite, fontWeight = FontWeight.Black)
                                    Text(listOf(driver.vehicle, driver.plate).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { driver.phone }, color = GadmMuted, fontSize = 12.sp)
                                }
                                Icon(Icons.Filled.ChevronRight, null, tint = GadmLime)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("FECHAR", color = GadmMuted) } }
    )
}

@Composable
private fun DriverDetailDialog(
    driver: GadmDriver,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onCorrection: (String) -> Unit,
    onBlock: (String) -> Unit,
    onUnblock: () -> Unit,
    onCancelOffer: () -> Unit,
    onRelease: () -> Unit
) {
    var mode by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GadmSurfaceStrong,
        titleContentColor = GadmWhite,
        textContentColor = GadmMuted,
        title = { Text(driver.name, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("${driver.vehicle} ${driver.plate}".trim().ifBlank { driver.phone }, color = GadmMuted)
                StatusPill(driver.availabilityLabel, accent = driverStatusColor(driver))
                Text("PIX: ${driver.pix.ifBlank { "Não informado" }}", color = GadmMuted, fontSize = 12.sp)
                if (driver.currentOrderId.isNotBlank() || driver.currentRideId.isNotBlank()) Text("Corrida ativa: ${driver.currentOrderId.ifBlank { driver.currentRideId }}", color = GadmYellow, fontSize = 12.sp)
                if (driver.pendingOfferOrderId.isNotBlank() || driver.pendingOfferRideId.isNotBlank()) Text("Oferta pendente: ${driver.pendingOfferOrderId.ifBlank { driver.pendingOfferRideId }}", color = GadmYellow, fontSize = 12.sp)
                if (mode.isNotBlank()) OutlinedTextField(value = reason, onValueChange = { reason = it }, modifier = Modifier.fillMaxWidth(), label = { Text(if (mode == "block") "Motivo do bloqueio" else "O que precisa corrigir?") }, colors = appTextFieldColors())
            }
        },
        confirmButton = {
            when {
                mode == "block" -> TextButton(onClick = { onBlock(reason.ifBlank { "Bloqueado pelo gestor" }) }) { Text("CONFIRMAR BLOQUEIO", color = GadmDanger, fontWeight = FontWeight.Black) }
                mode == "correction" -> TextButton(onClick = { onCorrection(reason.ifBlank { "Revise seus dados cadastrais" }) }) { Text("ENVIAR SOLICITAÇÃO", color = GadmYellow, fontWeight = FontWeight.Black) }
                driver.blocked -> TextButton(onClick = onUnblock) { Text("DESBLOQUEAR", color = GadmLime, fontWeight = FontWeight.Black) }
                !driver.approved -> TextButton(onClick = onApprove) { Text("APROVAR", color = GadmLime, fontWeight = FontWeight.Black) }
                driver.pendingOfferOrderId.isNotBlank() || driver.pendingOfferRideId.isNotBlank() -> TextButton(onClick = onCancelOffer) { Text("CANCELAR OFERTA", color = GadmYellow, fontWeight = FontWeight.Black) }
                else -> TextButton(onClick = onRelease) { Text("LIBERAR / DESTRAVAR", color = GadmLime, fontWeight = FontWeight.Black) }
            }
        },
        dismissButton = {
            Row {
                if (mode.isBlank() && !driver.blocked) {
                    TextButton(onClick = { mode = if (!driver.approved) "correction" else "block" }) { Text(if (!driver.approved) "PEDIR CORREÇÃO" else "BLOQUEAR", color = if (!driver.approved) GadmYellow else GadmDanger) }
                }
                TextButton(onClick = onDismiss) { Text("FECHAR", color = GadmMuted) }
            }
        }
    )
}

@Composable
private fun IncidentDetailDialog(incident: GadmIncident, onDismiss: () -> Unit, onResolve: (Boolean) -> Unit) {
    var release by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GadmSurfaceStrong,
        titleContentColor = GadmWhite,
        textContentColor = GadmMuted,
        title = { Text(incident.title, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(incident.description.ifBlank { "Sem descrição registrada." })
                Text("Pedido: ${incident.orderId.ifBlank { "não vinculado" }}", color = GadmMuted, fontSize = 12.sp)
                Text("Entregador: ${incident.driverName.ifBlank { incident.driverId.ifBlank { "não vinculado" } }}", color = GadmMuted, fontSize = 12.sp)
                SwitchRow("Liberar entregador", "Limpa os vínculos de missão e deixa disponível após resolver.", release) { release = it }
            }
        },
        confirmButton = { TextButton(onClick = { onResolve(release) }) { Text("RESOLVER OCORRÊNCIA", color = GadmLime, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("FECHAR", color = GadmMuted) } }
    )
}
