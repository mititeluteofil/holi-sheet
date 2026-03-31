package com.holisheet.app.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ItemWithLabels(
    @Embedded val item: InventoryItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemLabelCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "labelId"
        )
    )
    val labels: List<Label>
)
