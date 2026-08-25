package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
        LoginReal(state.message, vm::dismissMessage, vm::login)
        return
    }

    var sectionName by rememberSaveable { mutableStateOf(GeralSection.HOME.name) }
    var selectedOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    val section = GeralSection.valueOf(sectionName)
    val selected = state.orders.firstOrNull { it.id == selectedOrderId }

    if (selected != null) {
        OrderReal(
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
                Header(section.label)
                when (section) {
                    GeralSection.HOME -> Dashboard(vm)
                    GeralSection.ORDERS -> Orders(vm) { selectedOrderId = it.id }
                    GeralSection.PRODUCTS -> Products(vm)
                    GeralSection.STORE -> Store(vm)
                    GeralSection.MORE -> More(vm)
                }
            }
            state.message?.let { msg ->
                Text(
                    msg,
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
private fun LoginReal(message: String?, dismiss: () -> Unit, login: (String) -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().background(GadmSurface).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GESTOR GERAL", color = GadmNavy, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Rodrigues Açaí e Cia", color = GadmLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Administração geral do ecossistema", color = GadmMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(5) },
            label = { Text("PIN administrativo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { login(pin) },
            enabled = pin.length == 5,
            colors = ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("ENTRAR", fontWeight = FontWeight.Black) }
        message?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = GadmDanger, fontSize = 12.sp, modifier = Modifier.clickable { dismiss() })
        }
        Spacer(Modifier.height(28.dp))
        Text("Versão ${AppVersion.SHORT}", color = GadmMuted, fontSize = 11.sp)
    }
}

@Composable
private fun Header(title: String) {
    Column(Modifier.fillMaxWidth().background(GadmWhite).padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text(title, color = GadmNavy, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Rodrigues Açaí e Cia • Gestor Geral", color = GadmMuted, fontSize = 11.sp)
    }
}

@Composable
private fun Dashboard(vm: GadmViewModel) {
    val s = vm.state
    val active = s.orders.filter { it.currentStage !in setOf("Finalizado", "Cancelado") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Metric("Novos", active.count { it.currentStage == "Novo" }.toString()) }
        item { Metric("Em preparo", active.count { it.currentStage == "Em preparo" }.toString()) }
        item { Metric("Prontos", active.count { it.currentStage == "Pronto" }.toString()) }
        item { Metric("Produtos no Supabase", s.products.size.toString()) }
        item { Metric("Entregadores online", s.drivers.count { it.online && !it.blocked }.toString()) }
        item { Metric("Loja", if (s.operation.open && s.operation.acceptOrders) "Aberta" else "Fechada") }
        item { Title("Pedidos que exigem atenção") }
        if (active.isEmpty()) item { Info("Nenhum pedido operacional no momento.") }
        items(active.take(8), key = { it.id }) { CompactOrder(it) }
    }
}

@Composable
private fun Orders(vm: GadmViewModel, open: (GadmOrder) -> Unit) {
    val orders = vm.state.orders.sortedByDescending { it.createdAt }
    if (vm.state.loading && orders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GadmLime) }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (orders.isEmpty()) item { Info("Nenhum pedido recebido do Firestore.") }
        items(orders, key = { it.id }) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GadmWhite),
                modifier = Modifier.fillMaxWidth().clickable { open(order) }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${order.code}", color = GadmNavy, fontWeight = FontWeight.Black)
                        Status(order.currentStage)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(order.customerName, color = GadmNavy, fontWeight = FontWeight.Bold)
                    if (order.itemsLabel.isNotBlank()) {
                        Text(order.itemsLabel, color = GadmMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(money(order.total), color = GadmSuccess, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun OrderReal(order: GadmOrder, onBack: () -> Unit, onAdvance: () -> Unit) {
    val action = when (order.currentStage) {
        "Novo" -> "ACEITAR E INICIAR PREPARO"
        "Em preparo" -> "MARCAR COMO PRONTO"
        "Pronto" -> "ENVIAR PARA TORRE"
        else -> null
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GadmSurface),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedButton(onClick = onBack) { Text("← Voltar") }
            Spacer(Modifier.height(10.dp))
            Text("Pedido #${order.code}", color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(order.currentStage, color = GadmMuted, fontSize = 12.sp)
        }
        item { Detail("Cliente", listOf(order.customerName, order.customerPhone, order.address).filter { it.isNotBlank() }) }
        item { Detail("Itens", listOf(order.itemsLabel.ifBlank { "Itens não descritos" })) }
        item { Detail("Pagamento", listOf(order.payment.ifBlank { "Não informado" }, "Total ${money(order.total)}", "Entrega ${money(order.deliveryFee)}")) }
        if (order.driverName.isNotBlank()) item { Detail("Entregador", listOf(order.driverName)) }
        action?.let { text ->
            item {
                Button(
                    onClick = onAdvance,
                    colors = ButtonDefaults.buttonColors(containerColor = GadmLime, contentColor = GadmNavy),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text(text, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun Products(vm: GadmViewModel) {
    val products = vm.state.products.sortedBy { it.name.lowercase() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item { Text("Catálogo Supabase", color = GadmMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        if (products.isEmpty()) item { Info("Aguardando catálogo do Supabase. Entre novamente se a sessão expirou.") }
        items(products, key = { it.id }) { product -> Product(product) { vm.toggleProduct(product, !product.paused) } }
    }
}

@Composable
private fun Product(product: GadmProduct, toggle: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp)) {
            Text(product.name, color = GadmNavy, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(listOf(product.category, money(product.price)).filter { it.isNotBlank() }.joinToString(" • "), color = GadmMuted, fontSize = 11.sp)
            Spacer(Modifier.height(9.dp))
            OutlinedButton(onClick = toggle) { Text(if (product.paused) "Ativar produto" else "Pausar produto") }
        }
    }
}

@Composable
private fun Store(vm: GadmViewModel) {
    val op = vm.state.operation
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Detail(
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
            Button(
                onClick = { vm.updateOperation(op.copy(open = !op.open)) },
                colors = ButtonDefaults.buttonColors(containerColor = if (op.open) GadmDanger else GadmLime, contentColor = if (op.open) GadmWhite else GadmNavy),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (op.open) "Fechar loja" else "Abrir loja", fontWeight = FontWeight.Bold) }
        }
        item {
            OutlinedButton(
                onClick = { vm.updateOperation(op.copy(acceptOrders = !op.acceptOrders)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (op.acceptOrders) "Pausar novos pedidos" else "Retomar novos pedidos") }
        }
    }
}

@Composable
private fun More(vm: GadmViewModel) {
    val s = vm.state
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Detail("Conta", listOf(s.user?.name.orEmpty(), "Perfil ${s.user?.role.orEmpty()}", "Versão ${AppVersion.SHORT}")) }
        item { Detail("Apps oficiais", listOf("Cliente: rodriguesacaiecia.netlify.app", "Rodrigues Gestor: operação da loja", "Gestor Geral: administração completa")) }
        item { Detail("Arquitetura", listOf("Pedidos/operação: Firebase e Firestore", "Catálogo/PIN administrativo: Supabase")) }
        item { OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth()) { Text("Sair do Gestor Geral") } }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = GadmMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = GadmNavy, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CompactOrder(order: GadmOrder) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp)) {
            Text("#${order.code} • ${order.customerName}", color = GadmNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(order.itemsLabel.ifBlank { order.address }, color = GadmMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Status(order.currentStage)
        }
    }
}

@Composable
private fun Status(status: String) {
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
private fun Detail(title: String, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = GadmWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Spacer(Modifier.height(7.dp))
            lines.forEach { Text(it, color = GadmMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp)) }
        }
    }
}

@Composable
private fun Title(text: String) = Text(text, color = GadmNavy, fontWeight = FontWeight.Black, fontSize = 15.sp)

@Composable
private fun Info(text: String) = Detail("Informação", listOf(text))

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
