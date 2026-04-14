package com.holisheet.app.data.db.dao

import androidx.room.*
import com.holisheet.app.data.model.InventoryItem
import com.holisheet.app.data.model.ItemLabelCrossRef
import com.holisheet.app.data.model.ItemWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    @Transaction
    @Query("SELECT * FROM items WHERE inventoryId = :inventoryId ORDER BY createdAt DESC")
    fun getItemsWithLabels(inventoryId: Long): Flow<List<ItemWithLabels>>

    @Transaction
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemWithLabels(id: Long): Flow<ItemWithLabels?>

    @Transaction
    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR ocrText LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchItems(query: String): Flow<List<ItemWithLabels>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemLabelCrossRef(crossRef: ItemLabelCrossRef)

    @Delete
    suspend fun deleteItemLabelCrossRef(crossRef: ItemLabelCrossRef)

    @Query("DELETE FROM item_label_cross_refs WHERE itemId = :itemId")
    suspend fun deleteAllLabelsForItem(itemId: Long)
}
