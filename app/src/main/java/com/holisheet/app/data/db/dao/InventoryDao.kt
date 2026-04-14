package com.holisheet.app.data.db.dao

import androidx.room.*
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.model.InventoryWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventories ORDER BY updatedAt DESC")
    fun getAllInventories(): Flow<List<Inventory>>

    @Query("SELECT * FROM inventories WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchInventories(query: String): Flow<List<Inventory>>

    @Query("SELECT * FROM inventories WHERE id = :id")
    suspend fun getInventoryById(id: Long): Inventory?

    @Transaction
    @Query("SELECT * FROM inventories WHERE id = :id")
    fun getInventoryWithItems(id: Long): Flow<InventoryWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: Inventory): Long

    @Update
    suspend fun updateInventory(inventory: Inventory)

    @Delete
    suspend fun deleteInventory(inventory: Inventory)

    @Query("SELECT COUNT(*) FROM items WHERE inventoryId = :inventoryId")
    fun getItemCount(inventoryId: Long): Flow<Int>
}
