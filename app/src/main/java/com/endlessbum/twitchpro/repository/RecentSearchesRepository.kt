package com.endlessbum.twitchpro.repository

import com.endlessbum.twitchpro.db.RecentSearchesDao
import com.endlessbum.twitchpro.model.ui.RecentSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentSearchesRepository(
    private val recentSearchesDao: RecentSearchesDao,
) {

    fun getAll(type: String) = recentSearchesDao.getAll(type)

    suspend fun getItem(query: String, type: String) = withContext(Dispatchers.IO) {
        recentSearchesDao.getItem(query, type)
    }

    suspend fun save(item: RecentSearch) = withContext(Dispatchers.IO) {
        recentSearchesDao.ensureMaxSizeAndInsert(item)
    }

    suspend fun delete(item: RecentSearch) = withContext(Dispatchers.IO) {
        recentSearchesDao.delete(item)
    }

    /** The search is removed from the history of every tab, not only the one it was removed in. */
    suspend fun deleteQuery(query: String) = withContext(Dispatchers.IO) {
        recentSearchesDao.deleteQuery(query)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        recentSearchesDao.deleteAll()
    }
}