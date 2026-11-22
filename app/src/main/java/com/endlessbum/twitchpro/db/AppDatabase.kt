package com.endlessbum.twitchpro.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.endlessbum.twitchpro.model.NotificationUser
import com.endlessbum.twitchpro.model.PlaybackState
import com.endlessbum.twitchpro.model.ShownNotification
import com.endlessbum.twitchpro.model.VideoPosition
import com.endlessbum.twitchpro.model.chat.RecentEmote
import com.endlessbum.twitchpro.model.ui.Bookmark
import com.endlessbum.twitchpro.model.ui.BookmarkIgnoredUser
import com.endlessbum.twitchpro.model.ui.ChannelSort
import com.endlessbum.twitchpro.model.ui.CustomProxy
import com.endlessbum.twitchpro.model.ui.GameSort
import com.endlessbum.twitchpro.model.ui.LocalChannelFollow
import com.endlessbum.twitchpro.model.ui.LocalGameFollow
import com.endlessbum.twitchpro.model.ui.OfflineVideo
import com.endlessbum.twitchpro.model.ui.RecentSearch
import com.endlessbum.twitchpro.model.ui.SavedFilter
import com.endlessbum.twitchpro.model.ui.StreamProxy
import com.endlessbum.twitchpro.model.ui.TranslatedChannel
import com.endlessbum.twitchpro.model.ui.VideoSwap

@Database(
    entities = [
        OfflineVideo::class,
        RecentEmote::class,
        VideoPosition::class,
        LocalChannelFollow::class,
        LocalGameFollow::class,
        Bookmark::class,
        BookmarkIgnoredUser::class,
        ChannelSort::class,
        GameSort::class,
        ShownNotification::class,
        NotificationUser::class,
        TranslatedChannel::class,
        SavedFilter::class,
        RecentSearch::class,
        PlaybackState::class,
        CustomProxy::class,
        StreamProxy::class,
        VideoSwap::class,
    ],
    version = 41,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun offlineVideos(): OfflineVideosDao
    abstract fun recentEmotes(): RecentEmotesDao
    abstract fun videoPositions(): VideoPositionsDao
    abstract fun localChannelFollows(): LocalChannelFollowsDao
    abstract fun localGameFollows(): LocalGameFollowsDao
    abstract fun bookmarks(): BookmarksDao
    abstract fun bookmarkIgnoredUsers(): BookmarkIgnoredUsersDao
    abstract fun channelSort(): ChannelSortDao
    abstract fun gameSort(): GameSortDao
    abstract fun shownNotifications(): ShownNotificationsDao
    abstract fun notificationUsers(): NotificationUsersDao
    abstract fun translatedChannels(): TranslatedChannelsDao
    abstract fun savedFilters(): SavedFiltersDao
    abstract fun recentSearches(): RecentSearchesDao
    abstract fun playbackStates(): PlaybackStatesDao
    abstract fun customProxies(): CustomProxiesDao
    abstract fun streamProxies(): StreamProxiesDao
    abstract fun videoSwap(): VideoSwapDao
}