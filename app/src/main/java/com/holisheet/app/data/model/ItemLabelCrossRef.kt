package com.holisheet.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_label_cross_refs",
    primaryKeys = ["itemId", "labelId"],
    foreignKeys = [
        ForeignKey(entity = InventoryItem::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Label::class,         parentColumns = ["id"], childColumns = ["labelId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("labelId")]
)
data class ItemLabelCrossRef(
    val itemId: Long,
    val labelId: Long
)
