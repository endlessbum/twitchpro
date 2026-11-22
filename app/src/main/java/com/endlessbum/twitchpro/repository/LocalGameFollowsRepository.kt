package com.endlessbum.twitchpro.repository

import com.endlessbum.twitchpro.db.LocalGameFollowsDao
import com.endlessbum.twitchpro.model.ui.LocalGameFollow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalGameFollowsRepository(
    private val localGameFollowsDao: LocalGameFollowsDao,
) {

    suspend fun getAll() = withContext(Dispatchers.IO) {
        localGameFollowsDao.getAll()
    }

    suspend fun getById(id: String) = withContext(Dispatchers.IO) {
        localGameFollowsDao.getById(id)
    }

    suspend fun save(item: LocalGameFollow) = withContext(Dispatchers.IO) {
        localGameFollowsDao.insert(item)
    }

    suspend fun delete(item: LocalGameFollow) = withContext(Dispatchers.IO) {
        item.boxArt?.let {
            if (it.isNotBlank()) {
                File(it).delete()
            }
        }
        localGameFollowsDao.delete(item)
    }

    suspend fun update(item: LocalGameFollow) = withContext(Dispatchers.IO) {
        localGameFollowsDao.update(item)
    }
}
