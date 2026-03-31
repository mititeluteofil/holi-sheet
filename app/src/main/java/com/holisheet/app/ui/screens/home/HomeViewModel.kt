package com.holisheet.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val inventories: StateFlow<List<Inventory>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query -> inventoryRepo.searchInventories(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun deleteInventory(inventory: Inventory) = viewModelScope.launch {
        inventoryRepo.delete(inventory)
    }
}
