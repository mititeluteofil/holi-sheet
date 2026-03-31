package com.holisheet.app.data.repository

import com.holisheet.app.data.db.dao.InventoryDao
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.model.InventoryWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(private val dao: InventoryDao) {

    fun getAllInventories(): Flow<List<Inventory>> = dao.getAllInventories()

    fun searchInventories(query: String): Flow<List<Inventory>> =
        if (query.isBlank()) dao.getAllInventories() else dao.searchInventories(query)

    fun getInventoryWithItems(id: Long): Flow<InventoryWithItems?> = dao.getInventoryWithItems(id)

    fun getItemCount(inventoryId: Long): Flow<Int> = dao.getItemCount(inventoryId)

    suspend fun insert(inventory: Inventory): Long = dao.insertInventory(inventory)

    suspend fun update(inventory: Inventory) =
        dao.updateInventory(inventory.copy(updatedAt = System.currentTimeMillis()))

    suspend fun delete(inventory: Inventory) = dao.deleteInventory(inventory)
}
