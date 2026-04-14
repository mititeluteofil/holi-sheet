package com.holisheet.app.ui.screens.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.data.model.ItemWithLabels
import com.holisheet.app.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepo: ItemRepository
) : ViewModel() {

    private val _itemId = MutableStateFlow(0L)

    val item: StateFlow<ItemWithLabels?> = _itemId
        .flatMapLatest { id -> if (id == 0L) flowOf(null) else itemRepo.getItemWithLabels(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setItemId(id: Long) { _itemId.value = id }

    fun deleteItem(onDone: () -> Unit) = viewModelScope.launch {
        item.value?.let { itemRepo.delete(it.item) }
        onDone()
    }
}
