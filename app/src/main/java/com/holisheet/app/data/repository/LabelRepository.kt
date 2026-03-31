package com.holisheet.app.data.repository

import com.holisheet.app.data.db.dao.LabelDao
import com.holisheet.app.data.model.Label
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelRepository @Inject constructor(private val dao: LabelDao) {

    fun getAllLabels(): Flow<List<Label>> = dao.getAllLabels()

    fun searchLabels(query: String): Flow<List<Label>> =
        if (query.isBlank()) dao.getAllLabels() else dao.searchLabels(query)

    suspend fun getOrCreate(name: String, colorHex: String = "#6650A4"): Label {
        return dao.getLabelByName(name.trim()) ?: run {
            val id = dao.insertLabel(Label(name = name.trim(), colorHex = colorHex))
            Label(id = id, name = name.trim(), colorHex = colorHex)
        }
    }

    suspend fun delete(label: Label) = dao.deleteLabel(label)
}
