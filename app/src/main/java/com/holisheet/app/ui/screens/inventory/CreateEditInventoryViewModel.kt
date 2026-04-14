package com.holisheet.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEditInventoryViewModel @Inject constructor(
    private val repo: InventoryRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _colorHex = MutableStateFlow("#6650A4")
    val colorHex = _colorHex.asStateFlow()

    private val _emoji = MutableStateFlow("")
    val emoji = _emoji.asStateFlow()

    private var editingInventory: Inventory? = null

    fun onNameChange(v: String) { _name.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onColorChange(v: String) { _colorHex.value = v }
    fun onEmojiChange(v: String) { _emoji.value = v.take(2) }

    fun loadInventory(id: Long) = viewModelScope.launch {
        repo.getAllInventories().collect { list ->
            list.find { it.id == id }?.let { inv ->
                editingInventory = inv
                _name.value = inv.name
                _description.value = inv.description
                _colorHex.value = inv.colorHex
                _emoji.value = inv.emoji
                return@collect
            }
        }
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val existing = editingInventory
        if (existing != null) {
            repo.update(existing.copy(name = _name.value.trim(), description = _description.value.trim(), colorHex = _colorHex.value, emoji = _emoji.value))
        } else {
            repo.insert(Inventory(name = _name.value.trim(), description = _description.value.trim(), colorHex = _colorHex.value, emoji = _emoji.value))
        }
        onDone()
    }
}
