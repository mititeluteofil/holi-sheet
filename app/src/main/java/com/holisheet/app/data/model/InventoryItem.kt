package com.holisheet.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [ForeignKey(
        entity = Inventory::class,
        parentColumns = ["id"],
        childColumns = ["inventoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("inventoryId")]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inventoryId: Long,
    val name: String,
    val description: String = "",
    val quantity: Int = 1,
    val ocrText: String = "",
    val photoPath: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
