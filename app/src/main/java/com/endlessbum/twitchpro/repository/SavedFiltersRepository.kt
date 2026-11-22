package com.endlessbum.twitchpro.repository

import com.endlessbum.twitchpro.db.SavedFiltersDao
import com.endlessbum.twitchpro.model.ui.SavedFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedFiltersRepository(
    private val savedFiltersDao: SavedFiltersDao,
) {

    fun getAll() = savedFiltersDao.getAll()

    suspend fun save(item: SavedFilter) = withContext(Dispatchers.IO) {
        savedFiltersDao.insert(item)
    }

    suspend fun delete(item: SavedFilter) = withContext(Dispatchers.IO) {
        savedFiltersDao.delete(item)
    }
}
