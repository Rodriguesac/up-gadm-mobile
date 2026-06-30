package com.rodriguesacai.gadm.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rodriguesacai.gadm.data.GadmCustomer
import com.rodriguesacai.gadm.data.GadmDriver
import com.rodriguesacai.gadm.data.GadmFinanceEntry
import com.rodriguesacai.gadm.data.GadmIncident
import com.rodriguesacai.gadm.data.GadmOrder
import com.rodriguesacai.gadm.data.GadmProduct
import com.rodriguesacai.gadm.data.GadmRepository
import com.rodriguesacai.gadm.data.GadmUser
import com.rodriguesacai.gadm.data.StoreOperation
import kotlinx.coroutines.launch

data class GadmUiState(
    val user: GadmUser? = null,
    val orders: List<GadmOrder> = emptyList(),
    val drivers: List<GadmDriver> = emptyList(),
    val customers: List<GadmCustomer> = emptyList(),
    val incidents: List<GadmIncident> = emptyList(),
    val products: List<GadmProduct> = emptyList(),
    val finance: List<GadmFinanceEntry> = emptyList(),
    val operation: StoreOperation = StoreOperation(),
    val loading: Boolean = true,
    val message: String? = null
)

class GadmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GadmRepository()

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
    }

    fun dismissMessage() { state = state.copy(message = null) }

    fun login(pin: String) {
        viewModelScope.launch {
            repository.signIn(pin)
                .onSuccess { state = state.copy(user = it, message = "Acesso liberado: ${it.name}") }
                .onFailure { state = state.copy(message = it.message ?: "Não foi possível entrar.") }
        }
    }

    fun logout() { state = state.copy(user = null, message = "Sessão encerrada.") }

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
