package com.rodriguesacai.gadm.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.rodriguesacai.gadm.data.FirebaseGateway
import com.rodriguesacai.gadm.data.GadmUiState
import com.rodriguesacai.gadm.data.GadmUser
import com.rodriguesacai.gadm.data.OperationalOrder
import com.rodriguesacai.gadm.data.OrderStatus

class GadmViewModel : ViewModel() {
    private val gateway = FirebaseGateway()
    private var ordersListener: ListenerRegistration? = null

    var state by mutableStateOf(GadmUiState())
        private set

    init {
        restoreSession()
    }

    fun restoreSession() {
        state = state.copy(loading = true, error = null)
        gateway.restoreSession { result ->
            result.fold(
                onSuccess = { user ->
                    state = state.copy(loading = false, currentUser = user, error = null)
                    if (user != null) startOrdersListener()
                },
                onFailure = { error ->
                    state = state.copy(loading = false, currentUser = null, error = error.message ?: "Não foi possível iniciar o GADM.")
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        state = state.copy(busy = true, error = null, info = null)
        gateway.signIn(email, password) { result ->
            result.fold(
                onSuccess = { user ->
                    state = state.copy(busy = false, currentUser = user, error = null, info = "Acesso liberado para ${user.name}.")
                    startOrdersListener()
                },
                onFailure = { error ->
                    state = state.copy(busy = false, error = error.message ?: "Falha ao entrar.")
                }
            )
        }
    }

    fun signOut() {
        ordersListener?.remove()
        ordersListener = null
        gateway.signOut()
        state = GadmUiState(loading = false, info = "Sessão encerrada.")
    }

    fun updateStatus(order: OperationalOrder, target: OrderStatus, note: String = "") {
        val user = state.currentUser ?: return
        if (target !in allowedTransitions(order.status)) {
            state = state.copy(error = "Essa mudança de status não é permitida para ${order.status.label}.")
            return
        }
        state = state.copy(busy = true, error = null, info = null)
        gateway.updateStatus(order, target, user, note) { result ->
            result.fold(
                onSuccess = { state = state.copy(busy = false, info = "Pedido ${order.code} atualizado para ${target.label}.") },
                onFailure = { error -> state = state.copy(busy = false, error = error.message ?: "Não foi possível atualizar o pedido.") }
            )
        }
    }

    fun assignDriver(order: OperationalOrder, driverName: String, driverId: String) {
        val user = state.currentUser ?: return
        if (order.status != OrderStatus.PRONTO && order.status != OrderStatus.AGUARDANDO_ENTREGADOR) {
            state = state.copy(error = "Só é possível despachar pedido pronto.")
            return
        }
        state = state.copy(busy = true, error = null, info = null)
        gateway.assignDriver(order, driverName, driverId, user) { result ->
            result.fold(
                onSuccess = { state = state.copy(busy = false, info = "Entregador definido para ${order.code}.") },
                onFailure = { error -> state = state.copy(busy = false, error = error.message ?: "Não foi possível definir o entregador.") }
            )
        }
    }

    fun setPriority(order: OperationalOrder, priority: Boolean) {
        val user = state.currentUser ?: return
        state = state.copy(busy = true, error = null, info = null)
        gateway.setPriority(order, priority, user) { result ->
            result.fold(
                onSuccess = { state = state.copy(busy = false, info = if (priority) "Prioridade ativada." else "Prioridade removida.") },
                onFailure = { error -> state = state.copy(busy = false, error = error.message ?: "Não foi possível alterar a prioridade.") }
            )
        }
    }

    fun clearMessages() {
        state = state.copy(error = null, info = null)
    }

    private fun startOrdersListener() {
        ordersListener?.remove()
        ordersListener = gateway.listenOrders(
            onOrders = { orders -> state = state.copy(orders = orders, loading = false, error = null) },
            onError = { error -> state = state.copy(loading = false, error = error.message ?: "Não foi possível sincronizar pedidos.") }
        )
    }

    override fun onCleared() {
        ordersListener?.remove()
        super.onCleared()
    }

    companion object {
        fun allowedTransitions(status: OrderStatus): List<OrderStatus> = when (status) {
            OrderStatus.RECEBIDO -> listOf(OrderStatus.CONFIRMADO, OrderStatus.CANCELADO)
            OrderStatus.CONFIRMADO -> listOf(OrderStatus.EM_PREPARO, OrderStatus.CANCELADO)
            OrderStatus.EM_PREPARO -> listOf(OrderStatus.PRONTO)
            OrderStatus.PRONTO -> listOf(OrderStatus.AGUARDANDO_ENTREGADOR)
            OrderStatus.AGUARDANDO_ENTREGADOR -> listOf(OrderStatus.EM_ROTA)
            OrderStatus.EM_ROTA -> listOf(OrderStatus.ENTREGUE)
            OrderStatus.ENTREGUE, OrderStatus.CANCELADO -> emptyList()
        }
    }
}
