package com.endlessbum.twitchpro.ui.following.streams

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.endlessbum.twitchpro.TwitchProApp
import com.endlessbum.twitchpro.repository.GraphQLRepository
import com.endlessbum.twitchpro.repository.HelixRepository
import com.endlessbum.twitchpro.repository.LocalChannelFollowsRepository
import com.endlessbum.twitchpro.repository.datasource.FollowedStreamsDataSource
import com.endlessbum.twitchpro.util.C
import com.endlessbum.twitchpro.util.TwitchApiHelper
import com.endlessbum.twitchpro.util.prefs
import com.endlessbum.twitchpro.util.tokenPrefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class FollowedStreamsViewModel(
    private val applicationContext: Context,
    private val localChannelFollowsRepository: LocalChannelFollowsRepository,
    private val graphQLRepository: GraphQLRepository,
    private val helixRepository: HelixRepository,
) : ViewModel() {

    val sortState = MutableStateFlow(applicationContext.prefs().getString(C.TV_FOLLOWED_LIVE_SORT, "viewers") ?: "viewers")
    val sortText = MutableStateFlow<CharSequence?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow = sortState.flatMapLatest {
        Pager(
            if (applicationContext.prefs().getString(C.COMPACT_STREAMS, "disabled") != "disabled") {
                PagingConfig(pageSize = 30, prefetchDistance = 10, initialLoadSize = 30)
            } else {
                PagingConfig(pageSize = 30, prefetchDistance = 3, initialLoadSize = 30)
            }
        ) {
            createSource()
        }.flow
    }.cachedIn(viewModelScope)

    fun setSort(sort: String) {
        applicationContext.prefs().edit {
            putString(C.TV_FOLLOWED_LIVE_SORT, sort)
        }
        sortState.value = sort
    }

    /** Source of the list, also used by the player to load the list again (the other streams, left / right). */
    fun createSource() = FollowedStreamsDataSource(
        userId = applicationContext.tokenPrefs().getString(C.USER_ID, null),
        localChannelFollowsRepository = localChannelFollowsRepository,
        gqlHeaders = TwitchApiHelper.getGQLHeaders(applicationContext, true),
        graphQLRepository = graphQLRepository,
        helixHeaders = TwitchApiHelper.getHelixHeaders(applicationContext),
        helixRepository = helixRepository,
        enableIntegrity = applicationContext.prefs().getBoolean(C.ENABLE_INTEGRITY, false),
        networkLibrary = applicationContext.prefs().getString(C.NETWORK_LIBRARY, C.OKHTTP),
        sort = sortState.value,
    )

    companion object {
        val FollowedStreamsViewModelFactory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TwitchProApp)
                val twitchProModule = application.twitchProModule
                FollowedStreamsViewModel(application.applicationContext, twitchProModule.localChannelFollowsRepository, twitchProModule.graphQLRepository, twitchProModule.helixRepository)
            }
        }
    }
}