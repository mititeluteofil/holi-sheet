package com.holisheet.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventories")
data class Inventory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6650A4",
    val emoji: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
