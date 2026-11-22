package com.endlessbum.twitchpro.ui.channel.suggestions

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.endlessbum.twitchpro.TwitchProApp
import com.endlessbum.twitchpro.repository.GraphQLRepository
import com.endlessbum.twitchpro.repository.datasource.ChannelSuggestionsDataSource
import com.endlessbum.twitchpro.ui.channel.ChannelPagerFragmentArgs
import com.endlessbum.twitchpro.util.C
import com.endlessbum.twitchpro.util.TwitchApiHelper
import com.endlessbum.twitchpro.util.prefs

class ChannelSuggestionsViewModel(
    applicationContext: Context,
    private val graphQLRepository: GraphQLRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = ChannelPagerFragmentArgs.fromSavedStateHandle(savedStateHandle)

    val flow = Pager(
        PagingConfig(pageSize = 30, prefetchDistance = 10, initialLoadSize = 30)
    ) {
        ChannelSuggestionsDataSource(
            channelLogin = args.channelLogin,
            gqlHeaders = TwitchApiHelper.getGQLHeaders(applicationContext, true),
            graphQLRepository = graphQLRepository,
            enableIntegrity = applicationContext.prefs().getBoolean(C.ENABLE_INTEGRITY, false),
            networkLibrary = applicationContext.prefs().getString(C.NETWORK_LIBRARY, C.OKHTTP),
        )
    }.flow.cachedIn(viewModelScope)

    companion object {
        val ChannelSuggestionsViewModelFactory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val application = (this[APPLICATION_KEY] as TwitchProApp)
                val twitchProModule = application.twitchProModule
                ChannelSuggestionsViewModel(application.applicationContext, twitchProModule.graphQLRepository, savedStateHandle)
            }
        }
    }
}