package com.holisheet.app.data.repository

import com.holisheet.app.data.db.dao.InventoryItemDao
import com.holisheet.app.data.model.InventoryItem
import com.holisheet.app.data.model.ItemLabelCrossRef
import com.holisheet.app.data.model.ItemWithLabels
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val dao: InventoryItemDao) {

    fun getItemsWithLabels(inventoryId: Long): Flow<List<ItemWithLabels>> =
        dao.getItemsWithLabels(inventoryId)

    fun getItemWithLabels(id: Long): Flow<ItemWithLabels?> = dao.getItemWithLabels(id)

    fun searchItems(query: String): Flow<List<ItemWithLabels>> = dao.searchItems(query)

    suspend fun insert(item: InventoryItem, labelIds: List<Long>): Long {
        val id = dao.insertItem(item)
        labelIds.forEach { dao.insertItemLabelCrossRef(ItemLabelCrossRef(id, it)) }
        return id
    }

    suspend fun update(item: InventoryItem, labelIds: List<Long>) {
        dao.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
        dao.deleteAllLabelsForItem(item.id)
        labelIds.forEach { dao.insertItemLabelCrossRef(ItemLabelCrossRef(item.id, it)) }
    }

    suspend fun delete(item: InventoryItem) = dao.deleteItem(item)
}
