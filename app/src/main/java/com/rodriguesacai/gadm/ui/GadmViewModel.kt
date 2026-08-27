package com.rodriguesacai.gadm.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rodriguesacai.gadm.data.AdminCatalogSnapshot
import com.rodriguesacai.gadm.data.AdminCoupon
import com.rodriguesacai.gadm.data.AdminNotice
import com.rodriguesacai.gadm.data.AdminBanner
import com.rodriguesacai.gadm.data.AdminCategory
import com.rodriguesacai.gadm.data.CouponDraft
import com.rodriguesacai.gadm.data.GadmCustomer
import com.rodriguesacai.gadm.data.GadmDriver
import com.rodriguesacai.gadm.data.GadmFinanceEntry
import com.rodriguesacai.gadm.data.GadmIncident
import com.rodriguesacai.gadm.data.GadmOrder
import com.rodriguesacai.gadm.data.GadmProduct
import com.rodriguesacai.gadm.data.GadmRepository
import com.rodriguesacai.gadm.data.GadmUser
import com.rodriguesacai.gadm.data.PixChangeAdminRequest
import com.rodriguesacai.gadm.data.StoreOperation
import com.rodriguesacai.gadm.data.SupabaseCatalogClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GadmUiState(
    val user: GadmUser? = null,
    val orders: List<GadmOrder> = emptyList(),
    val drivers: List<GadmDriver> = emptyList(),
    val customers: List<GadmCustomer> = emptyList(),
    val incidents: List<GadmIncident> = emptyList(),
    val products: List<GadmProduct> = emptyList(),
    val finance: List<GadmFinanceEntry> = emptyList(),
    val pixChanges: List<PixChangeAdminRequest> = emptyList(),
    val adminCatalog: AdminCatalogSnapshot = AdminCatalogSnapshot(),
    val operation: StoreOperation = StoreOperation(),
    val loading: Boolean = true,
    val message: String? = null
)

class GadmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GadmRepository()
    private val adminClient = SupabaseCatalogClient()

    var state by mutableStateOf(GadmUiState())
        private set

    init {
        viewModelScope.launch { repository.observeOrders().collect { state = state.copy(orders = it, loading = false) } }
        viewModelScope.launch { repository.observeDrivers().collect { state = state.copy(drivers = it) } }
        viewModelScope.launch { repository.observeCustomers().collect { state = state.copy(customers = it) } }
        viewModelScope.launch { repository.observeIncidents().collect { state = state.copy(incidents = it) } }
        viewModelScope.launch { repository.observeProducts().collect { state = state.copy(products = it) } }
        viewModelScope.launch { repository.observeFinance().collect { state = state.copy(finance = it) } }
        viewModelScope.launch { repository.observeStoreOperation().collect { state = state.copy(operation = it) } }
        viewModelScope.launch {
            while (isActive) {
                if (state.user != null) {
                    refreshPixChangesInternal(showError = false)
                    refreshAdminCatalogInternal(showError = false)
                }
                delay(12_000)
            }
        }
    }

    fun dismissMessage() { state = state.copy(message = null) }

    fun login(pin: String) {
        viewModelScope.launch {
            repository.signIn(pin)
                .onSuccess {
                    state = state.copy(user = it, message = "Acesso liberado: ${it.name}")
                    refreshPixChangesInternal(showError = false)
                    refreshAdminCatalogInternal(showError = false)
                }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível entrar.") }
        }
    }

    fun logout() {
        adminClient.clearSession()
        state = state.copy(
            user = null,
            pixChanges = emptyList(),
            adminCatalog = AdminCatalogSnapshot(),
            message = "Sessão encerrada."
        )
    }

    fun refreshPixChanges(showError: Boolean = true) {
        viewModelScope.launch { refreshPixChangesInternal(showError) }
    }

    private suspend fun refreshPixChangesInternal(showError: Boolean) {
        runCatching { adminClient.listPixChanges() }
            .onSuccess { state = state.copy(pixChanges = it) }
            .onFailure { if (showError) state = state.copy(message = it.message ?: "Não foi possível atualizar os trocos Pix.") }
    }

    fun refreshAdminCatalog(showError: Boolean = true) {
        viewModelScope.launch { refreshAdminCatalogInternal(showError) }
    }

    private suspend fun refreshAdminCatalogInternal(showError: Boolean) {
        runCatching { adminClient.loadAdminCatalog() }
            .onSuccess { state = state.copy(adminCatalog = it) }
            .onFailure { if (showError) state = state.copy(message = it.message ?: "Não foi possível atualizar os módulos administrativos.") }
    }

    fun markPixSent(request: PixChangeAdminRequest, reference: String = "") {
        viewModelScope.launch {
            runCatching { adminClient.markPixChangeSent(request.id, reference) }
                .onSuccess {
                    state = state.copy(message = "Troco Pix do pedido #${request.orderCode.ifBlank { request.orderId }} marcado como enviado.")
                    refreshPixChangesInternal(showError = false)
                }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível confirmar o envio do Pix.") }
        }
    }

    fun saveCoupon(draft: CouponDraft, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching { adminClient.saveCoupon(draft) }
                .onSuccess {
                    refreshAdminCatalogInternal(showError = false)
                    state = state.copy(message = if (draft.customerUid.isNotBlank() || draft.customerEmail.isNotBlank() || draft.customerPhone.isNotBlank()) "Cupom exclusivo salvo para ${draft.customerName.ifBlank { "o cliente selecionado" }}." else "Cupom público salvo.")
                    onDone?.invoke()
                }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível salvar o cupom.") }
        }
    }

    fun toggleCoupon(coupon: AdminCoupon) {
        viewModelScope.launch {
            runCatching { adminClient.toggleCoupon(coupon.id, !coupon.active) }
                .onSuccess { refreshAdminCatalogInternal(false) }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível alterar o cupom.") }
        }
    }

    fun toggleBanner(banner: AdminBanner) {
        viewModelScope.launch {
            runCatching { adminClient.toggleBanner(banner.id, !banner.active) }
                .onSuccess { refreshAdminCatalogInternal(false) }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível alterar o banner.") }
        }
    }

    fun toggleNotice(notice: AdminNotice) {
        viewModelScope.launch {
            runCatching { adminClient.toggleNotice(notice.id, !notice.active) }
                .onSuccess { refreshAdminCatalogInternal(false) }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível alterar o aviso.") }
        }
    }

    fun saveCategory(
        category: AdminCategory? = null,
        name: String,
        description: String,
        order: Int,
        active: Boolean,
        imageUrl: String,
        icon: String,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            runCatching {
                adminClient.saveCategory(
                    id = category?.id.orEmpty(),
                    name = name,
                    description = description,
                    order = order,
                    active = active,
                    imageUrl = imageUrl,
                    icon = icon
                )
            }.onSuccess {
                refreshAdminCatalogInternal(false)
                state = state.copy(message = "Categoria salva.")
                onDone?.invoke()
            }.onFailure { state = state.copy(message = it.message ?: "Não foi possível salvar a categoria.") }
        }
    }

    fun changePin(pin: String) {
        val user = state.user
        if (user == null) {
            state = state.copy(message = "Entre novamente para concluir essa ação.")
            return
        }
        run("PIN atualizado.") { repository.changePin(user.id, pin) }
    }

    fun acceptOrder(order: GadmOrder) = run("Pedido aceito e enviado para preparo.") {
        repository.acceptAndStartPreparation(order)
    }

    fun rejectOrder(order: GadmOrder, reason: String) = run("Pedido cancelado.") { repository.cancelOrder(order, reason) }

    fun startKitchen(order: GadmOrder) = run("Pedido enviado para preparo.") {
        repository.updateOrderStatus(order, statusPedido = "CONFIRMADO", statusProducao = "EM_PREPARO", note = "Preparo iniciado")
    }

    fun finishKitchen(order: GadmOrder) = run("Pedido marcado como pronto.") {
        repository.updateOrderStatus(order, statusProducao = "PRONTO", note = "Produção finalizada")
    }

    fun sendToTower(order: GadmOrder) = run("Pedido enviado para a torre.") {
        repository.updateOrderStatus(order, statusEntrega = "AGUARDANDO_ENTREGADOR", note = "Aguardando atribuição")
    }

    fun assignDriver(order: GadmOrder, driver: GadmDriver) = run("Oferta enviada ao entregador.") {
        repository.assignDriver(order, driver)
    }

    fun approveDriver(driver: GadmDriver) = run("Entregador aprovado.") { repository.approveDriver(driver) }
    fun requestDriverCorrection(driver: GadmDriver, reason: String) = run("Correção solicitada ao entregador.") { repository.requestDriverCorrection(driver, reason) }
    fun blockDriver(driver: GadmDriver, reason: String) = run("Entregador bloqueado.") { repository.setDriverBlocked(driver, true, reason) }
    fun unblockDriver(driver: GadmDriver) = run("Entregador desbloqueado.") { repository.setDriverBlocked(driver, false) }
    fun cancelPendingOffer(driver: GadmDriver) = run("Oferta cancelada e entregador liberado.") { repository.cancelPendingOffer(driver) }
    fun releaseDriver(driver: GadmDriver) = run("Entregador liberado. Pedido, corrida e rota foram limpos.") { repository.releaseDriver(driver) }

    fun resolveIncident(incident: GadmIncident, releaseDriver: Boolean) = run("Ocorrência resolvida.") {
        repository.resolveIncident(incident, releaseDriver)
    }

    fun updateOperation(operation: StoreOperation) = run("Operação da loja atualizada.") { repository.updateStoreOperation(operation) }
    fun toggleProduct(product: GadmProduct, paused: Boolean) = run(if (paused) "Produto pausado." else "Produto liberado.") { repository.toggleProduct(product, paused) }

    fun sendCommunication(title: String, message: String, type: String, action: String) = run("Comunicado enviado ao app do entregador.") {
        repository.sendGlobalCommunication(title, message, type, action, true)
    }

    private fun run(success: String, action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            action().onSuccess { state = state.copy(message = success) }
                .onFailure { state = state.copy(message = it.message ?: "Ação não concluída.") }
        }
    }
}
