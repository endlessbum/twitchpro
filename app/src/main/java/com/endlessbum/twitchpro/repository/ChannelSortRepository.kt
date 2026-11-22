package com.endlessbum.twitchpro.repository

import com.endlessbum.twitchpro.db.ChannelSortDao
import com.endlessbum.twitchpro.model.ui.ChannelSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelSortRepository(
    private val channelSortDao: ChannelSortDao,
) {

    suspend fun getById(id: String) = withContext(Dispatchers.IO) {
        channelSortDao.getById(id)
    }

    suspend fun save(item: ChannelSort) = withContext(Dispatchers.IO) {
        channelSortDao.insert(item)
    }

    suspend fun delete(item: ChannelSort) = withContext(Dispatchers.IO) {
        channelSortDao.delete(item)
    }
}
