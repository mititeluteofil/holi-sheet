package com.holisheet.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.holisheet.app.data.db.dao.InventoryDao
import com.holisheet.app.data.db.dao.InventoryItemDao
import com.holisheet.app.data.db.dao.LabelDao
import com.holisheet.app.data.model.Inventory
import com.holisheet.app.data.model.InventoryItem
import com.holisheet.app.data.model.ItemLabelCrossRef
import com.holisheet.app.data.model.Label

@Database(
    entities = [Inventory::class, InventoryItem::class, Label::class, ItemLabelCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun labelDao(): LabelDao
}
