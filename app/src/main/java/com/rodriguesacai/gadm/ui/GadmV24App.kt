package com.rodriguesacai.gadm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.gadm.AppVersion
import com.rodriguesacai.gadm.data.AdminCategory
import com.rodriguesacai.gadm.data.AdminCoupon
import com.rodriguesacai.gadm.data.AdminNotice
import com.rodriguesacai.gadm.data.AdminBanner
import com.rodriguesacai.gadm.data.CouponDraft
import com.rodriguesacai.gadm.data.GadmCustomer
import com.rodriguesacai.gadm.data.GadmDriver
import com.rodriguesacai.gadm.data.GadmOrder
import com.rodriguesacai.gadm.data.GadmProduct
import com.rodriguesacai.gadm.data.PixChangeAdminRequest
import java.text.NumberFormat
import java.util.Locale

private val V24Purple = Color(0xFF4B0082)
private val V24PurpleSoft = Color(0xFFF4EDF8)
private val V24Green = Color(0xFF72B51B)
private val V24GreenSoft = Color(0xFFF0F8E4)
private val V24Text = Color(0xFF242934)
private val V24Muted = Color(0xFF77808D)
private val V24Surface = Color(0xFFF7F8FA)
private val V24Border = Color(0xFFE7E8EC)
private val V24Danger = Color(0xFFD94949)
private val V24Warning = Color(0xFFB97800)

private enum class V24Screen(val title: String, val subtitle: String, val icon: ImageVector) {
    HOME("Gestor Geral", "Administração Rodrigues Açaí e Cia", Icons.Rounded.Home),
    ORDERS("Pedidos", "Acompanhe e controle cada pedido", Icons.Rounded.ShoppingBag),
    PRODUCTS("Cardápio", "Produtos, preços e disponibilidade", Icons.Rounded.Inventory2),
    CATEGORIES("Categorias", "Organização visual do cardápio", Icons.Rounded.Category),
    COUPONS("Cupons", "Públicos e exclusivos por cliente", Icons.Rounded.LocalOffer),
    BANNERS("Banners", "Destaques e campanhas da home", Icons.Rounded.ViewCarousel),
    NOTICES("Avisos", "Mensagens e comunicados ao cliente", Icons.Rounded.Campaign),
    CUSTOMERS("Clientes", "Cadastros, histórico e relacionamento", Icons.Rounded.People),
    DRIVERS("Entregadores", "Cadastro e situação operacional", Icons.Rounded.DeliveryDining),
    FINANCE("Financeiro", "Movimentações e visão de valores", Icons.Rounded.AccountBalanceWallet),
    STORE("Loja", "Operação, pedidos e tempo estimado", Icons.Rounded.Storefront),
    PIX("Trocos Pix", "Liberação segura após dinheiro recebido", Icons.Rounded.CurrencyExchange),
    SETTINGS("Configurações", "Conta, segurança e arquitetura", Icons.Rounded.Settings),
    CATEGORY_EDITOR("Editar categoria", "Imagem, ícone, ordem e visibilidade", Icons.Rounded.Edit),
    COUPON_EDITOR("Novo cupom", "Configure as regras e o cliente", Icons.Rounded.Add),
    ORDER_DETAIL("Detalhes do pedido", "Ações operacionais do pedido", Icons.Rounded.ShoppingBag)
}

@Composable
fun GadmV24App(vm: GadmViewModel = viewModel()) {
    val state = vm.state
    if (state.user == null) {
        V24Login(state.message, vm::dismissMessage, vm::login)
        return
    }

    var screenName by rememberSaveable { mutableStateOf(V24Screen.HOME.name) }
    var selectedOrderId by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf("") }
    val screen = V24Screen.valueOf(screenName)

    val back: (() -> Unit)? = when (screen) {
        V24Screen.HOME -> null
        V24Screen.CATEGORY_EDITOR -> ({ screenName = V24Screen.CATEGORIES.name })
        V24Screen.COUPON_EDITOR -> ({ screenName = V24Screen.COUPONS.name })
        V24Screen.ORDER_DETAIL -> ({ screenName = V24Screen.ORDERS.name })
        else -> ({ screenName = V24Screen.HOME.name })
    }

    Box(Modifier.fillMaxSize().background(V24Surface)) {
        Column(Modifier.fillMaxSize()) {
            V24Header(screen, back) {
                vm.refreshAdminCatalog(showError = false)
                vm.refreshPixChanges(showError = false)
            }
            when (screen) {
                V24Screen.HOME -> V24Home(vm) { screenName = it.name }
                V24Screen.ORDERS -> V24Orders(vm) {
                    selectedOrderId = it.id
                    screenName = V24Screen.ORDER_DETAIL.name
                }
                V24Screen.ORDER_DETAIL -> state.orders.firstOrNull { it.id == selectedOrderId }?.let {
                    V24OrderDetail(vm, it)
                } ?: EmptyState("Pedido não encontrado")
                V24Screen.PRODUCTS -> V24Products(vm)
                V24Screen.CATEGORIES -> V24Categories(vm) {
                    selectedCategoryId = it?.id.orEmpty()
                    screenName = V24Screen.CATEGORY_EDITOR.name
                }
                V24Screen.CATEGORY_EDITOR -> V24CategoryEditor(
                    vm,
                    state.adminCatalog.categories.firstOrNull { it.id == selectedCategoryId },
                    onDone = { screenName = V24Screen.CATEGORIES.name }
                )
                V24Screen.COUPONS -> V24Coupons(vm) { screenName = V24Screen.COUPON_EDITOR.name }
                V24Screen.COUPON_EDITOR -> V24CouponEditor(vm) { screenName = V24Screen.COUPONS.name }
                V24Screen.BANNERS -> V24Banners(vm)
                V24Screen.NOTICES -> V24Notices(vm)
                V24Screen.CUSTOMERS -> V24Customers(vm)
                V24Screen.DRIVERS -> V24Drivers(vm)
                V24Screen.FINANCE -> V24Finance(vm)
                V24Screen.STORE -> V24Store(vm)
                V24Screen.PIX -> V24Pix(vm)
                V24Screen.SETTINGS -> V24Settings(vm)
            }
        }

        state.message?.let { message ->
            Card(
                colors = CardDefaults.cardColors(containerColor = V24Text),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 18.dp, vertical = 88.dp)
                    .clickable { vm.dismissMessage() }
            ) {
                Text(
                    message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun V24Login(message: String?, dismiss: () -> Unit, login: (String) -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().background(V24Surface).padding(26.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(66.dp).background(V24Purple, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("R", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(22.dp))
        Text("GADM V24", color = V24Purple, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Gestor Geral Rodrigues", color = V24Text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("Uma função por tela. Administração sem poluição visual.", color = V24Muted, fontSize = 13.sp)
        Spacer(Modifier.height(30.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(5) },
            label = { Text("PIN administrativo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { login(pin) },
            enabled = pin.length == 5,
            colors = ButtonDefaults.buttonColors(containerColor = V24Purple, contentColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) { Text("ENTRAR NO GESTOR", fontWeight = FontWeight.Black, fontSize = 14.sp) }
        message?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = V24Danger, fontSize = 12.sp, modifier = Modifier.clickable { dismiss() })
        }
        Spacer(Modifier.height(24.dp))
        Text("${AppVersion.NAME} • Firebase operacional + Supabase administrativo", color = V24Muted, fontSize = 10.sp)
    }
}

@Composable
private fun V24Header(screen: V24Screen, back: (() -> Unit)?, refresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (back != null) {
            IconButton(onClick = back) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = V24Purple) }
        } else {
            Box(Modifier.size(42.dp).background(V24PurpleSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(screen.icon, contentDescription = null, tint = V24Purple, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(screen.title, color = V24Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(screen.subtitle, color = V24Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (screen !in setOf(V24Screen.CATEGORY_EDITOR, V24Screen.COUPON_EDITOR, V24Screen.ORDER_DETAIL)) {
            IconButton(onClick = refresh) { Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar", tint = V24Muted) }
        }
    }
}

@Composable
private fun V24Home(vm: GadmViewModel, open: (V24Screen) -> Unit) {
    val s = vm.state
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = V24Purple),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("Bom trabalho, ${s.user?.name.orEmpty()}", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text("Escolha uma área. Cada função abre em sua própria tela.", color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiniMetric("Novos", s.orders.count { it.currentStage == "Novo" }.toString(), Modifier.weight(1f))
                        MiniMetric("Preparo", s.orders.count { it.currentStage == "Em preparo" }.toString(), Modifier.weight(1f))
                        MiniMetric("Pix", s.pixChanges.count { it.status == "PIX_PENDENTE" }.toString(), Modifier.weight(1f))
                    }
                }
            }
        }
        item { SectionTitle("Operação") }
        item {
            LauncherGrid(
                listOf(
                    Launcher(V24Screen.ORDERS, s.orders.count { it.currentStage !in setOf("Finalizado", "Cancelado") }.toString()),
                    Launcher(V24Screen.STORE, if (s.operation.open && s.operation.acceptOrders) "Aberta" else "Atenção"),
                    Launcher(V24Screen.DRIVERS, s.drivers.count { it.online && !it.blocked }.toString()),
                    Launcher(V24Screen.PIX, s.pixChanges.count { it.status == "PIX_PENDENTE" }.toString())
                ), open
            )
        }
        item { SectionTitle("Cardápio e comunicação") }
        item {
            LauncherGrid(
                listOf(
                    Launcher(V24Screen.PRODUCTS, s.products.size.toString()),
                    Launcher(V24Screen.CATEGORIES, s.adminCatalog.categories.size.toString()),
                    Launcher(V24Screen.COUPONS, s.adminCatalog.coupons.count { it.active }.toString()),
                    Launcher(V24Screen.BANNERS, s.adminCatalog.banners.count { it.active }.toString()),
                    Launcher(V24Screen.NOTICES, s.adminCatalog.notices.count { it.active }.toString())
                ), open
            )
        }
        item { SectionTitle("Gestão") }
        item {
            LauncherGrid(
                listOf(
                    Launcher(V24Screen.CUSTOMERS, s.customers.size.toString()),
                    Launcher(V24Screen.FINANCE, s.finance.size.toString()),
                    Launcher(V24Screen.SETTINGS, AppVersion.SHORT)
                ), open
            )
        }
    }
}

private data class Launcher(val screen: V24Screen, val value: String)

@Composable
private fun LauncherGrid(items: List<Launcher>, open: (V24Screen) -> Unit) {
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    ModuleCard(item, Modifier.weight(1f)) { open(item.screen) }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModuleCard(item: Launcher, modifier: Modifier = Modifier, open: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.height(142.dp).clickable { open() }
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(V24PurpleSoft, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                    Icon(item.screen.icon, contentDescription = null, tint = V24Purple, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = V24Muted)
            }
            Column {
                Text(item.screen.title, color = V24Text, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(item.value, color = V24Green, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color.White.copy(alpha = .12f), RoundedCornerShape(16.dp)).padding(12.dp)) {
        Text(label, color = Color.White.copy(alpha = .72f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun V24Orders(vm: GadmViewModel, open: (GadmOrder) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val all = vm.state.orders.sortedByDescending { it.createdAt }
    val filtered = all.filter {
        (filter == "Todos" || it.currentStage == filter) &&
            (query.isBlank() || listOf(it.code, it.customerName, it.customerPhone, it.address).any { value -> value.contains(query, true) })
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchBox(query, { query = it }, "Buscar pedido, cliente ou telefone") }
        item { ChoiceRow(listOf("Todos", "Novo", "Em preparo", "Pronto", "Em entrega", "Finalizado"), filter) { filter = it } }
        if (filtered.isEmpty()) item { EmptyState("Nenhum pedido encontrado") }
        items(filtered, key = { it.id }) { order ->
            BigCard(onClick = { open(order) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("#${order.code}", color = V24Purple, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(order.customerName, color = V24Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(order.itemsLabel, color = V24Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusPill(order.currentStage)
                        Spacer(Modifier.height(8.dp))
                        Text(money(order.total), color = V24Green, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun V24OrderDetail(vm: GadmViewModel, order: GadmOrder) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = V24Purple), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Pedido #${order.code}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text(order.customerName, color = Color.White.copy(alpha = .82f), fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(money(order.total), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(6.dp))
                    StatusPill(order.currentStage, light = true)
                }
            }
        }
        item { InfoPanel("Cliente", listOf(order.customerName, order.customerPhone, order.address).filter { it.isNotBlank() }) }
        item { InfoPanel("Itens", listOf(order.itemsLabel.ifBlank { "Itens não descritos" })) }
        item { InfoPanel("Pagamento", listOf(order.payment.ifBlank { "Não informado" }, "Entrega ${money(order.deliveryFee)}")) }
        if (order.driverName.isNotBlank()) item { InfoPanel("Entregador", listOf(order.driverName)) }
        item {
            when (order.currentStage) {
                "Novo" -> PrimaryAction("ACEITAR E INICIAR PREPARO") { vm.acceptOrder(order) }
                "Em preparo" -> PrimaryAction("MARCAR COMO PRONTO") { vm.finishKitchen(order) }
                "Pronto" -> PrimaryAction("ENVIAR PARA TORRE") { vm.sendToTower(order) }
                else -> Unit
            }
        }
    }
}

@Composable
private fun V24Products(vm: GadmViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val products = vm.state.products.filter {
        (filter == "Todos" || (filter == "Ativos" && !it.paused) || (filter == "Pausados" && it.paused)) &&
            (query.isBlank() || it.name.contains(query, true) || it.category.contains(query, true))
    }.sortedBy { it.name.lowercase() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchBox(query, { query = it }, "Buscar produto ou categoria") }
        item { ChoiceRow(listOf("Todos", "Ativos", "Pausados"), filter) { filter = it } }
        item { LargeStat("Produtos cadastrados", vm.state.products.size.toString(), "Catálogo vindo do Supabase") }
        if (products.isEmpty()) item { EmptyState("Nenhum produto encontrado") }
        items(products, key = { it.id }) { product -> ProductAdminCard(vm, product) }
    }
}

@Composable
private fun ProductAdminCard(vm: GadmViewModel, product: GadmProduct) {
    BigCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(if (product.paused) Color(0xFFFFEEEE) else V24GreenSoft, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Inventory2, contentDescription = null, tint = if (product.paused) V24Danger else V24Green)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, color = V24Text, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(product.category.ifBlank { "Sem categoria" }, color = V24Muted, fontSize = 11.sp)
                Text(money(product.price), color = V24Purple, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            FilledTonalButton(onClick = { vm.toggleProduct(product, !product.paused) }) {
                Text(if (product.paused) "Ativar" else "Pausar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun V24Categories(vm: GadmViewModel, edit: (AdminCategory?) -> Unit) {
    val categories = vm.state.adminCatalog.categories.sortedBy { it.order }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            PrimaryAction("NOVA CATEGORIA", icon = Icons.Rounded.Add) { edit(null) }
        }
        item { Text("Use uma imagem ou ícone próprio para representar cada categoria no Cliente.", color = V24Muted, fontSize = 12.sp) }
        if (categories.isEmpty()) item { EmptyState("Nenhuma categoria carregada") }
        items(categories, key = { it.id }) { category ->
            BigCard(onClick = { edit(category) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).background(V24PurpleSoft, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                        Text(category.icon.ifBlank { category.name.take(1).uppercase() }, color = V24Purple, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category.name, color = V24Text, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("Ordem ${category.order} • ${if (category.active) "Visível" else "Oculta"}", color = V24Muted, fontSize = 11.sp)
                        if (category.imageUrl.isNotBlank()) Text("Imagem personalizada definida", color = V24Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = V24Muted)
                }
            }
        }
    }
}

@Composable
private fun V24CategoryEditor(vm: GadmViewModel, category: AdminCategory?, onDone: () -> Unit) {
    var name by rememberSaveable(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var description by rememberSaveable(category?.id) { mutableStateOf(category?.description.orEmpty()) }
    var order by rememberSaveable(category?.id) { mutableStateOf((category?.order ?: 0).toString()) }
    var icon by rememberSaveable(category?.id) { mutableStateOf(category?.icon.orEmpty()) }
    var imageUrl by rememberSaveable(category?.id) { mutableStateOf(category?.imageUrl.orEmpty()) }
    var active by rememberSaveable(category?.id) { mutableStateOf(category?.active ?: true) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { LargeStat(if (category == null) "Nova categoria" else category.name, "Visual próprio", "Sem carrinho de supermercado genérico") }
        item { LargeField("Nome da categoria", name, { name = it }, "Ex.: Bebidas") }
        item { LargeField("Descrição", description, { description = it }, "Opcional") }
        item { LargeField("Ícone", icon, { icon = it.take(4) }, "Ex.: 🥤, 🍨, 🧃") }
        item { LargeField("URL da imagem", imageUrl, { imageUrl = it }, "Cloudinary ou imagem pública") }
        item { LargeField("Ordem", order, { order = it.filter(Char::isDigit).take(4) }, "0", KeyboardType.Number) }
        item { TogglePanel("Categoria visível", "Aparece para o cliente", active) { active = it } }
        item {
            PrimaryAction("SALVAR CATEGORIA") {
                if (name.isNotBlank()) vm.saveCategory(category, name, description, order.toIntOrNull() ?: 0, active, imageUrl, icon, onDone)
            }
        }
    }
}

@Composable
private fun V24Coupons(vm: GadmViewModel, create: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val coupons = vm.state.adminCatalog.coupons.filter {
        (filter == "Todos" || (filter == "Públicos" && !it.targeted) || (filter == "Exclusivos" && it.targeted) || (filter == "Ativos" && it.active)) &&
            (query.isBlank() || it.code.contains(query, true) || it.customerName.contains(query, true) || it.customerEmail.contains(query, true))
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { PrimaryAction("CRIAR CUPOM", icon = Icons.Rounded.Add, action = create) }
        item { SearchBox(query, { query = it }, "Buscar código ou cliente") }
        item { ChoiceRow(listOf("Todos", "Ativos", "Públicos", "Exclusivos"), filter) { filter = it } }
        if (coupons.isEmpty()) item { EmptyState("Nenhum cupom encontrado") }
        items(coupons, key = { it.id }) { coupon -> CouponCard(vm, coupon) }
    }
}

@Composable
private fun CouponCard(vm: GadmViewModel, coupon: AdminCoupon) {
    BigCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(52.dp).background(if (coupon.targeted) V24PurpleSoft else V24GreenSoft, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = if (coupon.targeted) V24Purple else V24Green)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(coupon.code, color = V24Text, fontSize = 18.sp, fontWeight = FontWeight.Black)
                val value = if (coupon.type.contains("percent", true)) "${coupon.value.toInt()}%" else if (coupon.type.contains("frete", true)) "Frete grátis" else money(coupon.value)
                Text(value, color = V24Purple, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(if (coupon.targeted) "Exclusivo • ${coupon.customerName.ifBlank { coupon.customerEmail.ifBlank { coupon.customerPhone } }}" else "Cupom público", color = if (coupon.targeted) V24Purple else V24Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Switch(checked = coupon.active, onCheckedChange = { vm.toggleCoupon(coupon) })
        }
    }
}

@Composable
private fun V24CouponEditor(vm: GadmViewModel, onDone: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("percentual") }
    var value by rememberSaveable { mutableStateOf("") }
    var minValue by rememberSaveable { mutableStateOf("0") }
    var targeted by rememberSaveable { mutableStateOf(false) }
    var customerQuery by rememberSaveable { mutableStateOf("") }
    var selectedCustomerId by rememberSaveable { mutableStateOf("") }
    val selected = vm.state.customers.firstOrNull { it.id == selectedCustomerId }
    val customers = vm.state.customers.filter {
        customerQuery.length >= 2 && listOf(it.name, it.phone, it.email).any { field -> field.contains(customerQuery, true) }
    }.take(6)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { LargeStat("Novo cupom", "Seguro por cliente", "Cupom exclusivo não aparece para outras pessoas") }
        item { LargeField("Código", code, { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == '-' }.take(24) }, "EX.: CLIENTE10") }
        item { LargeField("Descrição", description, { description = it }, "Opcional") }
        item { ChoiceRow(listOf("percentual", "fixo", "frete_gratis"), type) { type = it } }
        if (type != "frete_gratis") item { LargeField("Valor", value, { value = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }.take(10) }, if (type == "percentual") "10" else "5,00", KeyboardType.Decimal) }
        item { LargeField("Pedido mínimo", minValue, { minValue = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }.take(10) }, "0,00", KeyboardType.Decimal) }
        item { TogglePanel("Cupom para cliente específico", "Quando ativo, somente o cliente selecionado recebe o cupom", targeted) { targeted = it; if (!it) selectedCustomerId = "" } }
        if (targeted) {
            item { SearchBox(customerQuery, { customerQuery = it }, "Nome, telefone ou e-mail do cliente") }
            if (selected != null) item {
                Card(colors = CardDefaults.cardColors(containerColor = V24PurpleSoft), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Cliente selecionado", color = V24Purple, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(selected.name, color = V24Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(listOf(selected.phone, selected.email).filter { it.isNotBlank() }.joinToString(" • "), color = V24Muted, fontSize = 11.sp)
                    }
                }
            }
            items(customers, key = { it.id }) { customer ->
                if (customer.id != selectedCustomerId) {
                    BigCard(onClick = { selectedCustomerId = customer.id; customerQuery = customer.name }) {
                        Text(customer.name, color = V24Text, fontWeight = FontWeight.Black)
                        Text(listOf(customer.phone, customer.email).filter { it.isNotBlank() }.joinToString(" • "), color = V24Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            PrimaryAction("SALVAR CUPOM") {
                val parsedValue = value.replace(',', '.').toDoubleOrNull() ?: if (type == "frete_gratis") 0.0 else -1.0
                val parsedMin = minValue.replace(',', '.').toDoubleOrNull() ?: 0.0
                if (code.isNotBlank() && parsedValue >= 0 && (!targeted || selected != null)) {
                    vm.saveCoupon(
                        CouponDraft(
                            code = code,
                            description = description,
                            type = type,
                            value = parsedValue,
                            minValue = parsedMin,
                            customerUid = if (targeted) selected?.id.orEmpty() else "",
                            customerName = if (targeted) selected?.name.orEmpty() else "",
                            customerEmail = if (targeted) selected?.email.orEmpty() else "",
                            customerPhone = if (targeted) selected?.phone.orEmpty() else ""
                        ),
                        onDone
                    )
                }
            }
        }
    }
}

@Composable
private fun V24Banners(vm: GadmViewModel) {
    val banners = vm.state.adminCatalog.banners.sortedBy { it.order }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { LargeStat("Banners", banners.count { it.active }.toString(), "ativos na comunicação visual") }
        if (banners.isEmpty()) item { EmptyState("Nenhum banner carregado") }
        items(banners, key = { it.id }) { banner -> BannerCard(vm, banner) }
    }
}

@Composable
private fun BannerCard(vm: GadmViewModel, banner: AdminBanner) {
    BigCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).background(V24PurpleSoft, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ViewCarousel, contentDescription = null, tint = V24Purple)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(banner.title.ifBlank { "Banner sem título" }, color = V24Text, fontWeight = FontWeight.Black)
                Text("${banner.actionType.ifBlank { "Somente imagem" }} • Ordem ${banner.order}", color = V24Muted, fontSize = 11.sp)
                if (banner.imageUrl.isNotBlank()) Text("Imagem definida", color = V24Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Switch(checked = banner.active, onCheckedChange = { vm.toggleBanner(banner) })
        }
    }
}

@Composable
private fun V24Notices(vm: GadmViewModel) {
    val notices = vm.state.adminCatalog.notices.sortedBy { it.order }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { LargeStat("Avisos", notices.count { it.active }.toString(), "ativos para o cliente") }
        if (notices.isEmpty()) item { EmptyState("Nenhum aviso carregado") }
        items(notices, key = { it.id }) { notice -> NoticeCard(vm, notice) }
    }
}

@Composable
private fun NoticeCard(vm: GadmViewModel, notice: AdminNotice) {
    BigCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(52.dp).background(if (notice.severity.contains("urgent", true)) Color(0xFFFFECEC) else V24PurpleSoft, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Campaign, contentDescription = null, tint = if (notice.severity.contains("urgent", true)) V24Danger else V24Purple)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(notice.title.ifBlank { "Aviso" }, color = V24Text, fontWeight = FontWeight.Black)
                Text(notice.body, color = V24Muted, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(notice.audience, color = V24Purple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Switch(checked = notice.active, onCheckedChange = { vm.toggleNotice(notice) })
        }
    }
}

@Composable
private fun V24Customers(vm: GadmViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    val customers = vm.state.customers.filter { query.isBlank() || listOf(it.name, it.phone, it.email).any { field -> field.contains(query, true) } }
        .sortedBy { it.name.lowercase() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchBox(query, { query = it }, "Buscar nome, telefone ou e-mail") }
        item { LargeStat("Clientes", vm.state.customers.size.toString(), "cadastros disponíveis no gestor") }
        if (customers.isEmpty()) item { EmptyState("Nenhum cliente encontrado") }
        items(customers, key = { it.id }) { customer -> CustomerCard(customer) }
    }
}

@Composable
private fun CustomerCard(customer: GadmCustomer) {
    BigCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(V24PurpleSoft, CircleShape), contentAlignment = Alignment.Center) {
                Text(customer.name.take(1).uppercase(), color = V24Purple, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(customer.name, color = V24Text, fontWeight = FontWeight.Black)
                Text(listOf(customer.phone, customer.email).filter { it.isNotBlank() }.joinToString(" • "), color = V24Muted, fontSize = 11.sp)
                if (customer.ordersCount > 0 || customer.totalSpent > 0) Text("${customer.ordersCount} pedidos • ${money(customer.totalSpent)}", color = V24Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun V24Drivers(vm: GadmViewModel) {
    val drivers = vm.state.drivers.sortedWith(compareByDescending<GadmDriver> { it.online }.thenBy { it.name })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { LargeStat("Entregadores online", drivers.count { it.online && !it.blocked }.toString(), "de ${drivers.size} cadastrados") }
        if (drivers.isEmpty()) item { EmptyState("Nenhum entregador encontrado") }
        items(drivers, key = { it.id }) { driver ->
            BigCard {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(52.dp).background(if (driver.online && !driver.blocked) V24GreenSoft else V24PurpleSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.DeliveryDining, contentDescription = null, tint = if (driver.online && !driver.blocked) V24Green else V24Purple)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(driver.name, color = V24Text, fontWeight = FontWeight.Black)
                            Text(driver.availabilityLabel, color = if (driver.blocked) V24Danger else V24Muted, fontSize = 11.sp)
                            Text(listOf(driver.vehicle, driver.plate).filter { it.isNotBlank() }.joinToString(" • "), color = V24Muted, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!driver.approved) OutlinedButton(onClick = { vm.approveDriver(driver) }, modifier = Modifier.weight(1f)) { Text("Aprovar") }
                        if (driver.blocked) OutlinedButton(onClick = { vm.unblockDriver(driver) }, modifier = Modifier.weight(1f)) { Text("Desbloquear") }
                        if (!driver.blocked && (driver.currentOrderId.isNotBlank() || driver.currentRideId.isNotBlank() || driver.pendingOfferOrderId.isNotBlank())) OutlinedButton(onClick = { vm.releaseDriver(driver) }, modifier = Modifier.weight(1f)) { Text("Liberar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun V24Finance(vm: GadmViewModel) {
    val entries = vm.state.finance.sortedByDescending { it.createdAt }
    val total = entries.sumOf { it.amount }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { LargeStat("Movimentações", money(total), "${entries.size} registros disponíveis") }
        if (entries.isEmpty()) item { EmptyState("Nenhuma movimentação encontrada") }
        items(entries, key = { it.id }) { entry ->
            BigCard {
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.description, color = V24Text, fontWeight = FontWeight.Black)
                        Text("${entry.type} • ${entry.status}", color = V24Muted, fontSize = 11.sp)
                    }
                    Text(money(entry.amount), color = if (entry.amount >= 0) V24Green else V24Danger, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun V24Store(vm: GadmViewModel) {
    val op = vm.state.operation
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = if (op.open && op.acceptOrders) V24GreenSoft else Color(0xFFFFEEEE)), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text(if (op.open && op.acceptOrders) "LOJA ABERTA" else "OPERAÇÃO LIMITADA", color = if (op.open && op.acceptOrders) V24Green else V24Danger, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(if (op.open) "Rodrigues está aberta" else "Rodrigues está fechada", color = V24Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Tempo estimado ${op.estimatedMinutes} min", color = V24Muted, fontSize = 12.sp)
                }
            }
        }
        item { TogglePanel("Loja aberta", "Controla a disponibilidade geral da loja", op.open) { vm.updateOperation(op.copy(open = it)) } }
        item { TogglePanel("Aceitar novos pedidos", "Pode pausar pedidos sem fechar toda a loja", op.acceptOrders) { vm.updateOperation(op.copy(acceptOrders = it)) } }
        item { TogglePanel("Modo manutenção", "Use apenas quando o atendimento precisar ser interrompido", op.maintenance) { vm.updateOperation(op.copy(maintenance = it)) } }
        item { InfoPanel("Mensagem operacional", listOf(op.message.ifBlank { "Sem mensagem configurada" })) }
    }
}

@Composable
private fun V24Pix(vm: GadmViewModel) {
    val requests = vm.state.pixChanges.sortedByDescending { it.createdAt }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { LargeStat("Pix liberados", requests.count { it.status == "PIX_PENDENTE" }.toString(), "só ficam liberados após confirmação do dinheiro") }
        if (requests.isEmpty()) item { EmptyState("Nenhum troco Pix pendente") }
        items(requests, key = { it.id }) { request -> PixCard(vm, request) }
    }
}

@Composable
private fun PixCard(vm: GadmViewModel, request: PixChangeAdminRequest) {
    val released = request.status == "PIX_PENDENTE" || request.cashReceivedAt != null
    BigCard {
        Column {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido #${request.orderCode.ifBlank { request.orderId }}", color = V24Purple, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text(request.customerName, color = V24Text, fontWeight = FontWeight.Bold)
                }
                Text(money(request.amount), color = V24Green, fontSize = 19.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            InfoLine("Banco", request.bank.ifBlank { "Não informado" })
            InfoLine("Titular", request.recipientName.ifBlank { "Não informado" })
            InfoLine("Tipo de chave", request.pixKeyType)
            InfoLine("Chave Pix", request.pixKey)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = V24Border)
            Text(if (released) "DINHEIRO CONFIRMADO PELO ENTREGADOR" else "AGUARDANDO DINHEIRO DO CLIENTE", color = if (released) V24Green else V24Warning, fontSize = 10.sp, fontWeight = FontWeight.Black)
            request.cashReceivedAmount?.let { Text("Recebido: ${money(it)}", color = V24Muted, fontSize = 11.sp) }
            if (request.status == "PIX_ENVIADO" || request.pixSentAt != null) {
                Spacer(Modifier.height(8.dp))
                Text("PIX JÁ MARCADO COMO ENVIADO", color = V24Purple, fontWeight = FontWeight.Black, fontSize = 11.sp)
            } else {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { vm.markPixSent(request) },
                    enabled = released,
                    colors = ButtonDefaults.buttonColors(containerColor = V24Purple, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("CONFIRMAR PIX ENVIADO", fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun V24Settings(vm: GadmViewModel) {
    val s = vm.state
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { InfoPanel("Conta administrativa", listOf(s.user?.name.orEmpty(), "Perfil ${s.user?.role.orEmpty()}", AppVersion.NAME)) }
        item { InfoPanel("Arquitetura", listOf("Firebase: operação de pedidos e entregadores", "Supabase: catálogo, cupons, banners, avisos e administração", "Pix: chave protegida e liberação após dinheiro recebido")) }
        item { InfoPanel("Padrão visual V24", listOf("Uma função por tela", "Campos e botões grandes", "Roxo Rodrigues + verde de ação", "Interface pensada para celular")) }
        item { OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Text("SAIR DO GESTOR", color = V24Danger, fontWeight = FontWeight.Black) } }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = V24Text, fontSize = 14.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = V24Purple) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ChoiceRow(options: List<String>, selected: String, change: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilledTonalButton(
                onClick = { change(option) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (selected == option) V24Purple else Color.White, contentColor = if (selected == option) Color.White else V24Muted),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) { Text(option, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun BigCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun LargeStat(label: String, value: String, caption: String) {
    Card(colors = CardDefaults.cardColors(containerColor = V24PurpleSoft), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = V24Purple, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(value, color = V24Text, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(caption, color = V24Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LargeField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = V24Text, fontSize = 12.sp, fontWeight = FontWeight.Black)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TogglePanel(title: String, subtitle: String, checked: Boolean, change: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = V24Text, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = V24Muted, fontSize = 11.sp)
            }
            Switch(checked = checked, onCheckedChange = change)
        }
    }
}

@Composable
private fun InfoPanel(title: String, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(17.dp)) {
            Text(title, color = V24Purple, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            lines.forEach { line -> Text(line, color = V24Text, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp)) }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = V24Muted, fontSize = 11.sp)
        Text(value, color = V24Text, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun PrimaryAction(text: String, icon: ImageVector? = null, action: () -> Unit) {
    Button(
        onClick = action,
        colors = ButtonDefaults.buttonColors(containerColor = V24Purple, contentColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun StatusPill(status: String, light: Boolean = false) {
    val bg = when (status) {
        "Novo" -> if (light) Color.White.copy(alpha = .16f) else V24PurpleSoft
        "Em preparo" -> Color(0xFFFFF1DE)
        "Pronto" -> V24GreenSoft
        "Cancelado" -> Color(0xFFFFECEC)
        else -> if (light) Color.White.copy(alpha = .16f) else Color(0xFFEEF1F5)
    }
    val fg = if (light) Color.White else when (status) {
        "Novo" -> V24Purple
        "Em preparo" -> V24Warning
        "Pronto" -> V24Green
        "Cancelado" -> V24Danger
        else -> V24Muted
    }
    Box(Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(status, color = fg, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Text(text, color = V24Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
