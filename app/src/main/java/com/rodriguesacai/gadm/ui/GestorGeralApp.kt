package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.gadm.AppVersion
import com.rodriguesacai.gadm.data.GadmOrder
import com.rodriguesacai.gadm.data.GadmProduct
import java.text.NumberFormat
import java.util.Locale

private enum class GeralSection(val label: String, val glyph: String) {
    HOME("Visão", "⌂"),
    ORDERS("Pedidos", "▤"),
    PRODUCTS("Produtos", "◫"),
    STORE("Loja", "▣"),
    MORE("Mais", "⋯")
}

@Composable
fun GestorGeralApp(vm: GadmViewModel = viewModel()) {
    val state = vm.state
    if (state.user == null) {
        GestorGeralLogin(
            message = state.message,
            onDismissMessage = vm::dismissMessage,
            onLogin = vm::login
        )
        return
    }

    var sectionName by rememberSaveable { mutableStateOf(GeralSection.HOME.name) }
    var selectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val section = GeralSection.valueOf(sectionName)
    val selected = state.orders.firstOrNull { it.id == selectedOrderId }

    if (selected != null) {
        RealOrderScreen(
            order = selected,
            onBack = { selectedOrderId = null },
            onAdvance = {
                when (selected.currentStage) {
                    "Novo" -> vm.acceptOrder(selected)
                    "Em preparo" -> vm.finishKitchen(selected)
                    "Pronto" -> vm.sendToTower(selected)
                }
            }
        )
        return
    }

    Scaffold(
        containerColor = GadmSurface,
        bottomBar = {
            NavigationBar(containerColor = GadmWhite) {
                GeralSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { sectionName = item.name },
                        icon = { Text(item.glyph, fontSize = 18.sp) },
                        label = { Text(item.label, fontSize = 10.sp, maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                GeneralHeader(
                    title = section.label,
                    subtitle = "Rodrigues Açaí e Cia • Gestor Geral"
                )
                when (section) {
                    GeralSection.HOME -> DashboardReal(vm)
                    GeralSection.ORDERS -> OrdersReal(vm) { selectedOrderId = it.id }
                    GeralSection.PRODUCTS -> ProductsReal(vm)
                    GeralSection.STORE -> StoreReal(vm)
                    GeralSection.MORE -> MoreReal(vm)
                }
            }
            state.message?.let { message ->
                Text(
                    text = message,
                    color = GadmWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .background(GadmNavy, RoundedCornerShape(14.dp))
                        .clickable { vm.dismissMessage() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun GestorGeralLogin(
    message: String?,
    onDismissMessage: () -> Unit,
    onLogin: (String) -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().background(GadmSurface).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GESTOR GERAL", color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 30.sp)
        Text("Rodrigues Açaí e Cia", color = GadmLime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("Administração geral do ecossistema", color = GadmMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { value -> pin = value.filter(Char::isDigit).take(5) },
            label = { Text("PIN administrativo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onLogin(pin) },
            enabled = pin.length == 5,
            colors = ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("ENTRAR", fontWeight = FontWeight.Black) }
        message?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = GadmDanger, fontSize = 12.sp, modifier = Modifier.clickable { onDismissMessage() })
        }
        Spacer(Modifier.height(28.dp))
        Text("Versão ${AppVersion.SHORT}", color = GadmMuted, fontSize = 11.sp)
    }
}

@Composable
private fun GeneralHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().background(GadmWhite).padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text(title, color = GadmNavy, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = GadmMuted, fontSize = 11.sp)
    }
}

@Composable
private fun DashboardReal(vm: GadmViewModel) {
    val s = vm.state
    val activeOrders = s.orders.filter { it.currentStage !in setOf("Finalizado", "Cancelado") }
    val newCount = activeOrders.count { it.currentStage == "Novo" }
    val prepCount = activeOrders.count { it.currentStage == "Em preparo" }
    val readyCount = activeOrders.count { it.currentStage == "Pronto" }
    val onlineDrivers = s.drivers.count { it.online && !it.blocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Novos", newCount.toString(), Modifier.weight(1f))
                MetricCard("Preparo", prepCount.toString(), Modifier.weight(1f))
                MetricCard("Prontos", readyCount.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Produtos", s.products.size.toString(), Modifier.weight(1f))
                MetricCard("Entregadores", onlineDrivers.toString(), Modifier.weight(1f))
                MetricCard("Loja", if (s.operation.open && s.operation.acceptOrders) "Aberta" else "Fechada", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Pedidos que exigem atenção") }
        if (activeOrders.isEmpty()) item { EmptyCard("Nenhum pedido operacional no momento.") }
        items(activeOrders.take(8), key = { it.id }) { order -> OrderCompact(order) }
    }
}

@Composable
private fun OrdersReal(vm: GadmViewModel, onOpen: (GadmOrder) -> Unit) {
    val orders = vm.state.orders.sortedByDescending { it.createdAt }
    if (vm.state.loading && orders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GadmLime) }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (orders.isEmpty()) item { EmptyCard("Nenhum pedido recebido do Firestore.") }
        items(orders, key = { it.id }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GadmWhite),
                modifier = Modifier.fillMaxWidth().clickable { onOpen(order) }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${order.code}", fontWeight = FontWeight.Black, color = GadmNavy)
                        StatusChip(order.currentStage)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(order.customerName, fontWeight = FontWeight.Bold, color = GadmNavy)
                    if (order.itemsLabel.isNotBlank()) Text(order.itemsLabel, color = GadmMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    Text(money(order.total), color = GadmSuccess, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun RealOrderScreen(order: GadmOrder, onBack: () -> Unit, onAdvance: () -> Unit) {
    val action = when (order.currentStage) {
        "Novo" -> "ACEITAR E INICIAR PREPARO"
        "Em preparo" -> "MARCAR COMO PRONTO"
        "Pronto" -> "ENVIAR PARA TORRE"
        else -> null
    }
    Column(Modifier.fillMaxSize().background(GadmSurface)) {
        Row(Modifier.fillMaxWidth().background(GadmWhite).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack) { Text("Voltar") }
            Spacer(Modifier.size(12.dp))
            Column {
                Text("Pedido #${order.code}", fontWeight = FontWeight.Black, color = GadmNavy, fontSize = 20.sp)
                Text(order.currentStage, color = GadmMuted, fontSize = 12.sp)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { DetailCard("Cliente", listOf(order.customerName, order.customerPhone, order.address).filter { it.isNotBlank() }) }
            item { DetailCard("Itens", listOf(order.itemsLabel.ifBlank { "Itens não descritos" })) }
            item { DetailCard("Pagamento", listOf(order.payment.ifBlank { "Não informado" }, "Total ${money(order.total)}", "Entrega ${money(order.deliveryFee)}")) }
            if (order.driverName.isNotBlank()) item { DetailCard("Entregador", listOf(order.driverName)) }
        }
        action?.let {
            Button(
                onClick = onAdvance,
                colors = ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy),
                modifier = Modifier.fillMaxWidth().padding(14.dp).height(54.dp)
            ) { Text(it, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun ProductsReal(vm: GadmViewModel) {
    val products = vm.state.products.sortedBy { it.name.lowercase() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item { Text("Catálogo Supabase", color = GadmMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        if (products.isEmpty()) item { EmptyCard("Aguardando catálogo do Supabase. Entre novamente se a sessão expirou.") }
        items(products, key = { it.id }) { product -> ProductRow(product) { vm.toggleProduct(product, !product.paused) } }
    }
}

@Composable
private fun ProductRow(product: GadmProduct, onToggle: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(product.name, color = GadmNavy, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(listOf(product.category, money(product.price)).filter { it.isNotBlank() }.joinToString(" • "), color = GadmMuted, fontSize = 11.sp)
            }
            OutlinedButton(onClick = onToggle) { Text(if (product.paused) "Ativar" else "Pausar") }
        }
    }
}

@Composable
private fun StoreReal(vm: GadmViewModel) {
    val op = vm.state.operation
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailCard(
                "Operação da loja",
                listOf(
                    if (op.open) "Loja aberta" else "Loja fechada",
                    if (op.acceptOrders) "Aceitando pedidos" else "Pedidos pausados",
                    "Tempo estimado: ${op.estimatedMinutes} min",
                    op.message.takeIf { it.isNotBlank() } ?: "Sem mensagem operacional"
                )
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { vm.updateOperation(op.copy(open = !op.open)) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (op.open) GadmDanger else GadmLime, contentColor = if (op.open) GadmWhite else GadmNavy),
                    modifier = Modifier.weight(1f)
                ) { Text(if (op.open) "Fechar loja" else "Abrir loja", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { vm.updateOperation(op.copy(acceptOrders = !op.acceptOrders)) }, modifier = Modifier.weight(1f)) {
                    Text(if (op.acceptOrders) "Pausar pedidos" else "Retomar pedidos")
                }
            }
        }
    }
}

@Composable
private fun MoreReal(vm: GadmViewModel) {
    val s = vm.state
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { DetailCard("Conta", listOf(s.user?.name.orEmpty(), "Perfil ${s.user?.role.orEmpty()}", "Versão ${AppVersion.SHORT}")) }
        item { DetailCard("Ecossistema", listOf("Cliente: rodriguesacaiecia.netlify.app", "Rodrigues Gestor: operação da loja", "Gestor Geral: administração completa")) }
        item { DetailCard("Dados", listOf("Pedidos e operação: Firebase/Firestore", "Catálogo e PIN administrativo: Supabase")) }
        item { OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth()) { Text("Sair do Gestor Geral") } }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, color = GadmNavy, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(label, color = GadmMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun OrderCompact(order: GadmOrder) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("#${order.code} • ${order.customerName}", fontWeight = FontWeight.Bold, color = GadmNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(order.itemsLabel.ifBlank { order.address }, color = GadmMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            StatusChip(order.currentStage)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        "Novo" -> GadmBlue
        "Em preparo" -> GadmYellow
        "Pronto" -> GadmSuccess
        "Cancelado" -> GadmDanger
        else -> GadmMuted
    }
    Text(status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun DetailCard(title: String, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Spacer(Modifier.height(7.dp))
            lines.forEach { Text(it, color = GadmMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) = Text(text, color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 15.sp)

@Composable
private fun EmptyCard(text: String) = DetailCard("Informação", listOf(text))

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
