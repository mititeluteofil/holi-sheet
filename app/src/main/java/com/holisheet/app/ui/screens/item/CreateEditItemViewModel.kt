package com.holisheet.app.ui.screens.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holisheet.app.data.model.InventoryItem
import com.holisheet.app.data.model.Label
import com.holisheet.app.data.repository.ItemRepository
import com.holisheet.app.data.repository.LabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEditItemViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val labelRepo: LabelRepository
) : ViewModel() {

    // Form state
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity = _quantity.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _ocrText = MutableStateFlow("")
    val ocrText = _ocrText.asStateFlow()

    private val _photoPath = MutableStateFlow("")
    val photoPath = _photoPath.asStateFlow()

    private val _attachedLabels = MutableStateFlow<List<Label>>(emptyList())
    val attachedLabels = _attachedLabels.asStateFlow()

    // Inline label creation
    private val _newLabelText = MutableStateFlow("")
    val newLabelText = _newLabelText.asStateFlow()

    private val _allLabels = MutableStateFlow<List<Label>>(emptyList())
    val allLabels = _allLabels.asStateFlow()

    private val _showLabelPicker = MutableStateFlow(false)
    val showLabelPicker = _showLabelPicker.asStateFlow()

    private var editingItemId: Long? = null
    private var inventoryId: Long = 0L

    init {
        viewModelScope.launch {
            labelRepo.getAllLabels().collect { _allLabels.value = it }
        }
    }

    fun initCreate(
        invId: Long,
        prefillName: String,
        prefillOcrText: String,
        prefillPhotoPath: String,
        prefillLabels: String
    ) {
        inventoryId = invId
        _name.value = prefillName
        _ocrText.value = prefillOcrText
        _photoPath.value = prefillPhotoPath

        if (prefillLabels.isNotBlank()) {
            viewModelScope.launch {
                val labels = prefillLabels.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { labelRepo.getOrCreate(it) }
                _attachedLabels.value = labels
            }
        }
    }

    fun initEdit(itemId: Long) {
        editingItemId = itemId
        viewModelScope.launch {
            itemRepo.getItemWithLabels(itemId).firstOrNull()?.let { iwl ->
                inventoryId = iwl.item.inventoryId
                _name.value = iwl.item.name
                _description.value = iwl.item.description
                _quantity.value = iwl.item.quantity
                _notes.value = iwl.item.notes
                _ocrText.value = iwl.item.ocrText
                _photoPath.value = iwl.item.photoPath
                _attachedLabels.value = iwl.labels
            }
        }
    }

    fun onNameChange(v: String) { _name.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onNotesChange(v: String) { _notes.value = v }
    fun onQuantityChange(v: Int) { _quantity.value = v.coerceAtLeast(1) }
    fun onPhotoPathChange(v: String) { _photoPath.value = v }
    fun onNewLabelTextChange(v: String) { _newLabelText.value = v }
    fun toggleLabelPicker() { _showLabelPicker.value = !_showLabelPicker.value }

    fun addExistingLabel(label: Label) {
        if (_attachedLabels.value.none { it.id == label.id }) {
            _attachedLabels.value = _attachedLabels.value + label
        }
    }

    fun removeLabel(label: Label) {
        _attachedLabels.value = _attachedLabels.value.filter { it.id != label.id }
    }

    fun createAndAddLabel() = viewModelScope.launch {
        val text = _newLabelText.value.trim()
        if (text.isBlank()) return@launch
        val label = labelRepo.getOrCreate(text)
        addExistingLabel(label)
        _newLabelText.value = ""
        _showLabelPicker.value = false
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val labelIds = _attachedLabels.value.map { it.id }
        val existing = editingItemId
        if (existing != null) {
            val item = InventoryItem(
                id = existing,
                inventoryId = inventoryId,
                name = _name.value.trim(),
                description = _description.value.trim(),
                quantity = _quantity.value,
                ocrText = _ocrText.value,
                photoPath = _photoPath.value,
                notes = _notes.value.trim()
            )
            itemRepo.update(item, labelIds)
        } else {
            val item = InventoryItem(
                inventoryId = inventoryId,
                name = _name.value.trim(),
                description = _description.value.trim(),
                quantity = _quantity.value,
                ocrText = _ocrText.value,
                photoPath = _photoPath.value,
                notes = _notes.value.trim()
            )
            itemRepo.insert(item, labelIds)
        }
        onDone()
    }
}
