package com.endlessbum.twitchpro.ui.following.channels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.endlessbum.twitchpro.TwitchProApp
import com.endlessbum.twitchpro.model.ui.ChannelSort
import com.endlessbum.twitchpro.repository.BookmarksRepository
import com.endlessbum.twitchpro.repository.ChannelSortRepository
import com.endlessbum.twitchpro.repository.GraphQLRepository
import com.endlessbum.twitchpro.repository.HelixRepository
import com.endlessbum.twitchpro.repository.LocalChannelFollowsRepository
import com.endlessbum.twitchpro.repository.OfflineVideosRepository
import com.endlessbum.twitchpro.repository.datasource.FollowedChannelsDataSource
import com.endlessbum.twitchpro.util.C
import com.endlessbum.twitchpro.util.TwitchApiHelper
import com.endlessbum.twitchpro.util.prefs
import com.endlessbum.twitchpro.util.tokenPrefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class FollowedChannelsViewModel(
    private val applicationContext: Context,
    private val channelSortRepository: ChannelSortRepository,
    private val localChannelFollowsRepository: LocalChannelFollowsRepository,
    private val offlineVideosRepository: OfflineVideosRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val graphQLRepository: GraphQLRepository,
    private val helixRepository: HelixRepository,
) : ViewModel() {

    val filter = MutableStateFlow<Filter?>(null)
    val sortText = MutableStateFlow<CharSequence?>(null)

    val sort: String
        get() = filter.value?.sort ?: FollowedChannelsSortDialog.SORT_LAST_BROADCAST
    val order: String
        get() = filter.value?.order ?: FollowedChannelsSortDialog.ORDER_DESC

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow = filter.flatMapLatest {
        Pager(
            PagingConfig(pageSize = 15, prefetchDistance = 5, initialLoadSize = 15)
        ) {
            FollowedChannelsDataSource(
                userId = applicationContext.tokenPrefs().getString(C.USER_ID, null),
                sort = when (sort) {
                    FollowedChannelsSortDialog.SORT_FOLLOWED_AT -> "created_at"
                    FollowedChannelsSortDialog.SORT_ALPHABETICALLY -> "login"
                    FollowedChannelsSortDialog.SORT_LAST_BROADCAST -> "last_broadcast"
                    else -> "last_broadcast"
                },
                order = when (order) {
                    FollowedChannelsSortDialog.ORDER_DESC -> "desc"
                    FollowedChannelsSortDialog.ORDER_ASC -> "asc"
                    else -> "desc"
                },
                localChannelFollowsRepository = localChannelFollowsRepository,
                offlineVideosRepository = offlineVideosRepository,
                bookmarksRepository = bookmarksRepository,
                gqlHeaders = TwitchApiHelper.getGQLHeaders(applicationContext, true),
                graphQLRepository = graphQLRepository,
                helixHeaders = TwitchApiHelper.getHelixHeaders(applicationContext),
                helixRepository = helixRepository,
                enableIntegrity = applicationContext.prefs().getBoolean(C.ENABLE_INTEGRITY, false),
                networkLibrary = applicationContext.prefs().getString(C.NETWORK_LIBRARY, C.OKHTTP),
                liveFirst = com.endlessbum.twitchpro.ui.view.TvUi.isTv(applicationContext),
                favorites = com.endlessbum.twitchpro.ui.tv.TvFavorites.run {
                    load(applicationContext)
                    items.value.map { it.login }
                },
            )
        }.flow
    }.cachedIn(viewModelScope)

    suspend fun getChannelSort(id: String): ChannelSort? {
        return channelSortRepository.getById(id)
    }

    suspend fun saveChannelSort(item: ChannelSort) {
        channelSortRepository.save(item)
    }

    fun setFilter(sort: String?, order: String?) {
        filter.value = Filter(sort, order)
    }

    class Filter(
        val sort: String?,
        val order: String?,
    )

    companion object {
        val FollowedChannelsViewModelFactory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TwitchProApp)
                val twitchProModule = application.twitchProModule
                FollowedChannelsViewModel(application.applicationContext, twitchProModule.channelSortRepository, twitchProModule.localChannelFollowsRepository, twitchProModule.offlineVideosRepository, twitchProModule.bookmarksRepository, twitchProModule.graphQLRepository, twitchProModule.helixRepository)
            }
        }
    }
}
