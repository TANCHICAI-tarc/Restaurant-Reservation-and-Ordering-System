package com.example.yumyumrestaurant.StaffUpdate

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.OrderProcess.OrderedMenuUi
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.Menu.MenuDataSource
import com.example.yumyumrestaurant.data.Order.Order
import com.example.yumyumrestaurant.data.Order.OrderDataSource
import com.example.yumyumrestaurant.data.Order.OrderRepository
import com.example.yumyumrestaurant.data.Order.Payment
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class StaffOperationViewModel(
    private val reservationRepository: ReservationRepository = ReservationRepository(),
    private val orderRepository: OrderRepository = OrderRepository(orderDataSource = OrderDataSource(), menuDataSource = MenuDataSource()),
    private val tableRepository: TableRepository = TableRepository(),
): ViewModel() {
    private val _reservations = MutableStateFlow<List<ReservationEntity>>(emptyList())
    val reservations: StateFlow<List<ReservationEntity>> = _reservations.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _selectedReservation = MutableStateFlow<ReservationEntity?>(null)
    val selectedReservation: StateFlow<ReservationEntity?> = _selectedReservation.asStateFlow()

    private val _selectedOrder  = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    private val _orderedMenus = MutableStateFlow<List<OrderedMenuUi>>(emptyList())
    val orderedMenus = _orderedMenus.asStateFlow()

    private val _selectedPayment = MutableStateFlow<Payment?>(null)
    val selectedPayment: StateFlow<Payment?> = _selectedPayment.asStateFlow()

    private val _reservedTables = MutableStateFlow<List<TableEntity>>(emptyList())
    val reservedTables = _reservedTables.asStateFlow()

    private val _filterStatus = MutableStateFlow<String?>(null)
    private val _toastMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val toastMessage = _toastMessage.asSharedFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _removedIds = MutableStateFlow<List<String>>(emptyList())

    init {
        fetchReservations()
        fetchOrders()
    }

    val filteredReservations: StateFlow<List<ReservationEntity>> =
        combine(_reservations, _filterStatus, _removedIds) { reservations, filterStatus, removed ->
            reservations
                .filter { it.reservationId !in removed }
                .filter { reservation ->
                    filterStatus?.let { reservation.reservationStatus == it } ?: true
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val filterLabel: StateFlow<String> = _filterStatus
        .map { status -> status ?: "All" }
        .stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            "All"
        )

    fun setFilter(status: String?) {
        _filterStatus.value = status
    }

    fun clearFilter() {
        _filterStatus.value = null
    }

    fun fetchReservations() {
        viewModelScope.launch {
            try {
                val list = reservationRepository.getAllReservationsFromFirebase()
                _reservations.value = list
            } catch (e: Exception) {
                Log.e("StaffVM", "Error fetching reservations", e)
            }
        }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val allOrders = orderRepository.getAllOrders()
                _orders.value = allOrders
            } catch (e: Exception) {
                Log.e("StaffVM", "Error fetching orders", e)
            }
        }
    }

    fun selectReservation(reservationID: String) {
        viewModelScope.launch {
            // Wait for reservations to be loaded if they are empty
            val found = _reservations.value.firstOrNull { it.reservationId == reservationID }

            if (found != null) {
                _selectedReservation.value = found
                loadRelatedData(found)
            } else {
                // 2. Only if the list is empty (e.g., deep link), then wait and fetch
                viewModelScope.launch {
                    _reservations.first { it.isNotEmpty() }
                    val delayedFound = _reservations.value.firstOrNull { it.reservationId == reservationID }
                    _selectedReservation.value = delayedFound
                    delayedFound?.let { loadRelatedData(it) }
                }
            }
        }
    }

    suspend fun loadRelatedData(reservation: ReservationEntity) {
        val resID = reservation.reservationId

        // Ensure orders are loaded
        viewModelScope.launch {
            try {
                _reservedTables.value = tableRepository.getTableForReservation(resID)
            } catch (e: Exception) {
                Log.e("StaffVM", "Error loading tables", e)
            }
        }

        viewModelScope.launch {
            try {
                val customer = reservationRepository.getCustomerById(reservation.userId)
                _selectedCustomer.value = customer
            } catch (e: Exception) {
                Log.e("StaffVM", "Error loading customer", e)
                _selectedCustomer.value = null
            }
        }
        // Task B: Load Orders, Payments, and Menus
        viewModelScope.launch {
            try {
                // If orders aren't in memory yet, wait for them
                if (_orders.value.isEmpty()) {
                    _orders.first { it.isNotEmpty() }
                }

                val order = _orders.value.firstOrNull { it.reservationID == resID }
                _selectedOrder.value = order

                if (order != null) {
                    // Launch these together as well
                    launch {
                        _selectedPayment.value = orderRepository.getPayment(order.paymentID)
                    }
                    launch {
                        loadOrderedMenus(order.orderID)
                    }
                } else {
                    _selectedPayment.value = null
                    _orderedMenus.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("StaffVM", "Error loading order-related data", e)
            }
        }
    }

    private suspend fun loadOrderedMenus(orderID: String) {
        try {
            val menuOrders = orderRepository.getMenuOrders(orderID)
            Log.d("StaffVM", "Menu orders count: ${menuOrders.size} for order ${orderID}")
            val result = menuOrders.mapNotNull { menuOrder ->
                val menu = orderRepository.getMenuItemById(menuOrder.menuItemID)
                if (menu == null) return@mapNotNull null
                
                OrderedMenuUi(
                    menuItemID = menu.menuID,
                    foodName = menu.foodName,
                    price = menu.price,
                    quantity = menuOrder.quantity,
                    remark = menuOrder.remark
                )
            }
            _orderedMenus.value = result
        } catch (e: Exception) {
            Log.e("StaffVM", "Error loading ordered menus", e)
        }
    }

    fun updateReservationStatus(reservationID: String, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) { // Move to IO thread
            try {
                val current = _selectedReservation.value
                if (current != null && current.reservationId == reservationID) {
                    val updated = current.copy(reservationStatus = newStatus)

                    // Perform network/DB update
                    reservationRepository.insertTable(updated)

                    // Switch back to Main to update UI state
                    withContext(Dispatchers.Main) {
                        _selectedReservation.value = updated
                        _reservations.value = _reservations.value.map {
                            if (it.reservationId == reservationID) updated else it
                        }
                        _toastMessage.emit("Status updated to $newStatus")
                    }
                }
            } catch (e: Exception) {
                Log.e("StaffVM", "Error updating status", e)
            }
        }
    }

    fun removeReservation(reservationID: String) {
        viewModelScope.launch {
            try {
                _removedIds.value = _removedIds.value + reservationID
                _toastMessage.emit("Reservation removed successfully!")
            } catch (e: Exception) {
                _toastMessage.emit("Error removing reservation: ${e.message}")
            }
        }
    }
}
