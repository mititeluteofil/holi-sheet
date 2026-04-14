package com.holisheet.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.model.InventoryItem
import com.holisheet.app.data.model.ItemWithLabels
import com.holisheet.app.data.repository.InventoryRepository
import com.holisheet.app.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DATE_DESC, NAME_ASC, QUANTITY_DESC }

@HiltViewModel
class InventoryDetailViewModel @Inject constructor(
    private val inventoryRepo: InventoryRepository,
    private val itemRepo: ItemRepository
) : ViewModel() {

    private val _inventoryId = MutableStateFlow(0L)

    val inventory: StateFlow<Inventory?> = _inventoryId
        .flatMapLatest { id ->
            if (id == 0L) flowOf(null)
            else inventoryRepo.getInventoryWithItems(id).map { it?.inventory }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val items: StateFlow<List<ItemWithLabels>> = combine(
        _inventoryId.flatMapLatest { id ->
            if (id == 0L) flowOf(emptyList()) else itemRepo.getItemsWithLabels(id)
        },
        _sortOrder,
        _searchQuery
    ) { items, sort, query ->
        val filtered = if (query.isBlank()) items
        else items.filter { it.item.name.contains(query, ignoreCase = true) || it.item.ocrText.contains(query, ignoreCase = true) }

        when (sort) {
            SortOrder.DATE_DESC      -> filtered.sortedByDescending { it.item.createdAt }
            SortOrder.NAME_ASC       -> filtered.sortedBy { it.item.name.lowercase() }
            SortOrder.QUANTITY_DESC  -> filtered.sortedByDescending { it.item.quantity }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setInventoryId(id: Long) { _inventoryId.value = id }
    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun onSortOrderChange(order: SortOrder) { _sortOrder.value = order }

    fun deleteItem(item: InventoryItem) = viewModelScope.launch { itemRepo.delete(item) }
}
