package com.holisheet.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InventoryWithItems(
    @Embedded val inventory: Inventory,
    @Relation(parentColumn = "id", entityColumn = "inventoryId")
    val items: List<InventoryItem>
)
